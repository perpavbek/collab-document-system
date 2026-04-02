import { useEffect, useRef, useState } from 'react';
import { App } from 'antd';
import { useNavigate } from 'react-router-dom';

import { documentsApi } from '../../../api/documents';
import { versionsApi } from '../../../api/versions';
import { collaborationSocket } from '../../../services/collaborationSocket';
import { useWorkspaceStore } from '../../../stores/workspaceStore';
import type { UserProfile } from '../../../types/auth';
import type { DocumentDto } from '../../../types/documents';
import type { EditOperation, EditOperationRequest, RealtimeOperationMessage } from '../../../types/operations';
import { applyOperationToContent, createOperationsFromDiff, operationSignature } from '../../../utils/operations';
import { SAVE_DEBOUNCE_MS, isEditOperationMessage, isOperationEventMessage } from '../utils/editorHelpers';
import {
  buildOptimisticEditingSession,
  getDocumentContentAtSequence,
  resolveUserProfiles,
} from '../utils/workspaceHelpers';

interface UseDocumentWorkspaceSyncOptions {
  currentUser: UserProfile | null;
  documentId: string | undefined;
}

export const useDocumentWorkspaceSync = ({ currentUser, documentId }: UseDocumentWorkspaceSyncOptions) => {
  const { message } = App.useApp();
  const navigate = useNavigate();

  const {
    document,
    content,
    permission,
    sessions,
    participants,
    recentOperations,
    isLoading,
    currentSequenceNumber,
    setLoading,
    reset,
    setWorkspace,
    setSessions,
    setDocument,
    setContent,
    setParticipants,
    addParticipant,
    pushOperation,
  } = useWorkspaceStore();

  const [isResyncing, setIsResyncing] = useState(false);
  const [saveState, setSaveState] = useState<'saved' | 'pending' | 'saving'>('saved');

  const requestQueueRef = useRef<Promise<unknown>>(Promise.resolve());
  const pendingSignaturesRef = useRef<string[]>([]);
  const knownOperationSequencesRef = useRef(new Set<number>());
  const resolvingUsersRef = useRef(new Set<string>());
  const warnedSocketRef = useRef(false);
  const saveTimeoutRef = useRef<number | null>(null);
  const latestContentRef = useRef('');
  const queuedContentRef = useRef('');

  const clearSaveTimeout = () => {
    if (saveTimeoutRef.current !== null) {
      window.clearTimeout(saveTimeoutRef.current);
      saveTimeoutRef.current = null;
    }
  };

  const clearPendingEdits = () => {
    clearSaveTimeout();
    pendingSignaturesRef.current = [];
    setSaveState('saved');
  };

  const resyncWorkspace = async ({
    showSuccessMessage,
    trackResyncState = false,
  }: {
    showSuccessMessage?: string;
    trackResyncState?: boolean;
  } = {}) => {
    if (trackResyncState) {
      setIsResyncing(true);
    }

    clearPendingEdits();

    try {
      await loadWorkspace(false);

      if (showSuccessMessage) {
        message.success(showSuccessMessage);
      }
    } finally {
      if (trackResyncState) {
        setIsResyncing(false);
      }
    }
  };

  const hydrateParticipants = async (userIds: string[]) => {
    const profiles = await resolveUserProfiles(userIds);
    setParticipants(profiles);
    return profiles;
  };

  const ensureParticipant = async (userId: string) => {
    const existingParticipants = useWorkspaceStore.getState().participants;

    if (existingParticipants[userId] || resolvingUsersRef.current.has(userId)) {
      return;
    }

    resolvingUsersRef.current.add(userId);

    try {
      const [profile] = await resolveUserProfiles([userId]);

      if (profile) {
        addParticipant(profile);
      }
    } finally {
      resolvingUsersRef.current.delete(userId);
    }
  };

  const processIncomingOperation = async (operation: EditOperation) => {
    const currentKnownSequence = useWorkspaceStore.getState().currentSequenceNumber;

    if (operation.sequenceNumber < currentKnownSequence) {
      await resyncWorkspace({ trackResyncState: true });
      return;
    }

    if (knownOperationSequencesRef.current.has(operation.sequenceNumber)) {
      return;
    }

    knownOperationSequencesRef.current.add(operation.sequenceNumber);
    await ensureParticipant(operation.userId);

    const signature = operationSignature(operation);
    const pendingIndex = pendingSignaturesRef.current.findIndex((entry) => entry === signature);
    const isLocalEcho = operation.userId === currentUser?.id && pendingIndex !== -1;

    if (isLocalEcho) {
      pendingSignaturesRef.current.splice(pendingIndex, 1);
    } else {
      latestContentRef.current = applyOperationToContent(latestContentRef.current, operation);
      queuedContentRef.current = applyOperationToContent(queuedContentRef.current, operation);
    }

    pushOperation(operation, !isLocalEcho);
  };

  const syncMissedOperations = async () => {
    if (!documentId) {
      return;
    }

    try {
      const after = useWorkspaceStore.getState().currentSequenceNumber;
      const response = await versionsApi.getOperations(documentId, after);
      const orderedOperations = response.slice().sort((left, right) => left.sequenceNumber - right.sequenceNumber);

      for (const operation of orderedOperations) {
        await processIncomingOperation(operation);
      }
    } catch {
      return;
    }
  };

  const loadWorkspace = async (withLoader = true) => {
    if (!documentId) {
      return;
    }

    if (withLoader) {
      setLoading(true);
    }

    try {
      const [documentResponse, permissionResponse, operationsResponse] = await Promise.all([
        documentsApi.getById(documentId),
        documentsApi.getPermission(documentId),
        versionsApi.getOperations(documentId, 0),
      ]);
      const sessionsResponse = await documentsApi.getSessions(documentId).catch(() => []);
      const contentResponse = await getDocumentContentAtSequence(
        documentId,
        documentResponse.versionSequenceNumber,
        operationsResponse,
      );

      const relatedUserIds = [
        documentResponse.ownerId,
        ...documentResponse.collaborators.map((entry) => entry.userId),
        ...sessionsResponse.map((entry) => entry.userId),
        ...operationsResponse.map((entry) => entry.userId),
      ];

      const relatedProfiles = await resolveUserProfiles(relatedUserIds);
      clearSaveTimeout();
      pendingSignaturesRef.current = [];
      requestQueueRef.current = Promise.resolve();
      knownOperationSequencesRef.current = new Set(operationsResponse.map((operation) => operation.sequenceNumber));
      latestContentRef.current = contentResponse;
      queuedContentRef.current = contentResponse;
      setSaveState('saved');

      setWorkspace({
        document: documentResponse,
        permission: permissionResponse.role,
        content: contentResponse,
        operations: operationsResponse,
        sessions: sessionsResponse,
        participants: relatedProfiles,
      });
    } catch {
      message.error('Failed to load the document workspace.');
      navigate('/documents', { replace: true });
    } finally {
      if (withLoader) {
        setLoading(false);
      }
    }
  };

  const refreshSessions = async () => {
    if (!documentId) {
      return;
    }

    try {
      const response = await documentsApi.getSessions(documentId);
      const optimisticSession = buildOptimisticEditingSession(
        documentId,
        currentUser,
        collaborationSocket.isConnected(),
      );
      const hasCurrentUserSession =
        optimisticSession && response.some((session) => session.userId === optimisticSession.userId);
      const nextSessions = optimisticSession && !hasCurrentUserSession ? [optimisticSession, ...response] : response;

      setSessions(nextSessions);
      await Promise.all(nextSessions.map((session) => ensureParticipant(session.userId)));
    } catch {
      const optimisticSession = buildOptimisticEditingSession(
        documentId,
        currentUser,
        collaborationSocket.isConnected(),
      );

      if (optimisticSession) {
        setSessions([optimisticSession]);
        await ensureParticipant(optimisticSession.userId);
      }
    }
  };

  const processRealtimeMessage = async (messagePayload: RealtimeOperationMessage) => {
    if (isEditOperationMessage(messagePayload)) {
      await processIncomingOperation(messagePayload);
      return;
    }

    if (!isOperationEventMessage(messagePayload)) {
      return;
    }

    if (messagePayload.type !== 'EDIT') {
      await resyncWorkspace({ trackResyncState: true });
      return;
    }

    await syncMissedOperations();
  };

  useEffect(() => {
    if (!documentId) {
      navigate('/documents', { replace: true });
      return undefined;
    }

    void loadWorkspace(true);

    return () => {
      clearSaveTimeout();
      knownOperationSequencesRef.current = new Set();
      collaborationSocket.disconnect();
      reset();
    };
  }, [documentId]);

  useEffect(() => {
    if (!documentId) {
      return undefined;
    }

    warnedSocketRef.current = false;
    collaborationSocket.connect(
      documentId,
      (messagePayload) => {
        void processRealtimeMessage(messagePayload);
      },
      (socketError) => {
        if (!warnedSocketRef.current) {
          message.warning(`${socketError} Falling back to periodic sync.`);
          warnedSocketRef.current = true;
        }
      },
      () => {
        void refreshSessions();
      },
    );

    return () => {
      collaborationSocket.disconnect();
    };
  }, [documentId, currentUser?.id]);

  useEffect(() => {
    if (!documentId) {
      return undefined;
    }

    void refreshSessions();
    const intervalId = window.setInterval(() => {
      void refreshSessions();
    }, 5000);

    return () => {
      window.clearInterval(intervalId);
    };
  }, [documentId]);

  useEffect(() => {
    if (!documentId) {
      return undefined;
    }

    const intervalId = window.setInterval(() => {
      void syncMissedOperations();
    }, 7000);

    return () => {
      window.clearInterval(intervalId);
    };
  }, [documentId, currentUser?.id]);

  const submitOperation = (payload: EditOperationRequest) => {
    requestQueueRef.current = requestQueueRef.current
      .then(async () => {
        const response = await documentsApi.applyEdit(payload);
        await processIncomingOperation(response);
      })
      .catch(async () => {
        clearSaveTimeout();
        pendingSignaturesRef.current = [];
        message.error('An edit operation failed. Resynchronizing document state.');
        await loadWorkspace(false);
      });

    return requestQueueRef.current;
  };

  const flushPendingChanges = () => {
    if (!documentId || permission === 'VIEWER') {
      return;
    }

    saveTimeoutRef.current = null;
    const operations = createOperationsFromDiff(queuedContentRef.current, latestContentRef.current);

    if (operations.length === 0) {
      setSaveState('saved');
      return;
    }

    operations.forEach((operation) => {
      pendingSignaturesRef.current.push(operationSignature(operation));
    });

    queuedContentRef.current = latestContentRef.current;
    setSaveState('saving');

    operations.forEach((operation) => {
      void submitOperation({
        documentId,
        ...operation,
      });
    });

    void requestQueueRef.current.finally(() => {
      if (latestContentRef.current === queuedContentRef.current) {
        setSaveState('saved');
      }
    });
  };

  const handleEditorChange = (nextValue: string) => {
    if (permission === 'VIEWER') {
      return;
    }

    latestContentRef.current = nextValue;
    setContent(nextValue);
    setSaveState('pending');
    clearSaveTimeout();
    saveTimeoutRef.current = window.setTimeout(() => {
      flushPendingChanges();
    }, SAVE_DEBOUNCE_MS);
  };

  const handleResync = async () => {
    await resyncWorkspace({
      showSuccessMessage: 'Document resynchronized',
      trackResyncState: true,
    });
  };

  const canEditContent = permission !== 'VIEWER';
  const participantList = Object.values(participants);
  const seedUsers = document ? participantList.filter((entry) => entry.id !== document.ownerId) : [];

  const updateDocumentDetails = (nextDocument: DocumentDto) => {
    setDocument(nextDocument);
  };

  return {
    canEditContent,
    clearPendingEdits,
    content,
    currentSequenceNumber,
    document,
    handleEditorChange,
    handleResync,
    hydrateParticipants,
    isLoading,
    isResyncing,
    participants,
    permission,
    recentOperations,
    reloadWorkspace: () => loadWorkspace(false),
    saveState,
    seedUsers,
    sessions,
    updateDocumentDetails,
  };
};
