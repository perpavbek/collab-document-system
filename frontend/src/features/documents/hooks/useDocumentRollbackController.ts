import { useEffect, useRef, useState } from 'react';
import { App } from 'antd';

import { documentsApi } from '../../../api/documents';
import { versionsApi } from '../../../api/versions';
import type { DocumentVersionResponse } from '../../../types/documents';
import type { EditOperation } from '../../../types/operations';
import { normalizeRollbackTargetSequence } from '../utils/editorHelpers';
import { buildRollbackTimeline, type RollbackTimelineEntry } from '../utils/rollbackHelpers';
import { getDocumentContentAtSequence } from '../utils/workspaceHelpers';

interface UseDocumentRollbackControllerOptions {
  currentSequenceNumber: number;
  documentId: string | undefined;
  onAfterRollback: () => Promise<void>;
  onBeforeRollback: () => void;
}

export const useDocumentRollbackController = ({
  currentSequenceNumber,
  documentId,
  onAfterRollback,
  onBeforeRollback,
}: UseDocumentRollbackControllerOptions) => {
  const { message } = App.useApp();
  const [rollbackOpen, setRollbackOpen] = useState(false);
  const [rollbackTimeline, setRollbackTimeline] = useState<RollbackTimelineEntry[]>([]);
  const [rollbackTargetSequence, setRollbackTargetSequence] = useState<number | null>(null);
  const [rollbackPreview, setRollbackPreview] = useState<DocumentVersionResponse | null>(null);
  const [rollbackOperations, setRollbackOperations] = useState<EditOperation[]>([]);
  const [isLoadingRollbackHistory, setIsLoadingRollbackHistory] = useState(false);
  const [isLoadingRollbackPreview, setIsLoadingRollbackPreview] = useState(false);
  const [isRollingBack, setIsRollingBack] = useState(false);
  const previewCacheRef = useRef(new Map<number, DocumentVersionResponse>());
  const previewRequestIdRef = useRef(0);

  useEffect(() => {
    setRollbackOpen(false);
    setRollbackTimeline([]);
    setRollbackTargetSequence(null);
    setRollbackPreview(null);
    setRollbackOperations([]);
    setIsLoadingRollbackHistory(false);
    setIsLoadingRollbackPreview(false);
    setIsRollingBack(false);
    previewCacheRef.current = new Map();
    previewRequestIdRef.current = 0;
  }, [documentId]);

  const isRollbackTargetInvalid =
    rollbackTargetSequence === null ||
    rollbackTargetSequence < 0 ||
    rollbackTargetSequence >= currentSequenceNumber ||
    rollbackPreview?.sequenceNumber !== rollbackTargetSequence;

  const loadRollbackPreview = async (sequenceNumber: number, operations: EditOperation[]) => {
    if (!documentId) {
      return;
    }

    const normalizedSequence = normalizeRollbackTargetSequence(currentSequenceNumber, sequenceNumber);

    if (normalizedSequence === null || normalizedSequence >= currentSequenceNumber) {
      return;
    }

    setRollbackTargetSequence(normalizedSequence);
    const cachedPreview = previewCacheRef.current.get(normalizedSequence);

    if (cachedPreview) {
      setRollbackPreview(cachedPreview);
      return;
    }

    const requestId = previewRequestIdRef.current + 1;
    previewRequestIdRef.current = requestId;
    setRollbackPreview(null);
    setIsLoadingRollbackPreview(true);

    try {
      const content = await getDocumentContentAtSequence(documentId, normalizedSequence, operations);
      const nextPreview = {
        content,
        sequenceNumber: normalizedSequence,
      };

      if (previewRequestIdRef.current !== requestId) {
        return;
      }

      previewCacheRef.current.set(normalizedSequence, nextPreview);
      setRollbackPreview(nextPreview);
    } catch {
      if (previewRequestIdRef.current === requestId) {
        message.error('Failed to load preview for the selected version.');
      }
    } finally {
      if (previewRequestIdRef.current === requestId) {
        setIsLoadingRollbackPreview(false);
      }
    }
  };

  useEffect(() => {
    if (!rollbackOpen || !documentId) {
      return;
    }

    let isActive = true;
    const defaultTarget = normalizeRollbackTargetSequence(currentSequenceNumber, Math.max(currentSequenceNumber - 1, 0));

    previewCacheRef.current = new Map();
    setRollbackTimeline([]);
    setRollbackPreview(null);
    setRollbackTargetSequence(defaultTarget);
    setIsLoadingRollbackHistory(true);

    const loadRollbackHistory = async () => {
      try {
        const operations = await versionsApi.getOperations(documentId, 0);

        if (!isActive) {
          return;
        }

        setRollbackOperations(operations);
        setRollbackTimeline(buildRollbackTimeline(operations, currentSequenceNumber));

        if (defaultTarget !== null && defaultTarget < currentSequenceNumber) {
          await loadRollbackPreview(defaultTarget, operations);
        }
      } catch {
        if (isActive) {
          message.error('Failed to load document versions.');
        }
      } finally {
        if (isActive) {
          setIsLoadingRollbackHistory(false);
        }
      }
    };

    void loadRollbackHistory();

    return () => {
      isActive = false;
    };
  }, [rollbackOpen, documentId, currentSequenceNumber]);

  const handleOpenRollback = () => {
    setRollbackOpen(true);
  };

  const handleRollbackCancel = () => {
    setRollbackOpen(false);
    setRollbackPreview(null);
  };

  const handleSelectRollbackVersion = async (sequenceNumber: number) => {
    const normalizedSequence = normalizeRollbackTargetSequence(currentSequenceNumber, sequenceNumber);

    if (normalizedSequence === null || normalizedSequence >= currentSequenceNumber) {
      message.warning(`Choose a version below #${currentSequenceNumber}.`);
      return;
    }

    await loadRollbackPreview(normalizedSequence, rollbackOperations);
  };

  const handleRollback = async () => {
    if (!documentId || isRollbackTargetInvalid || rollbackTargetSequence === null) {
      message.warning(`Rollback target must be below the current sequence #${currentSequenceNumber}.`);
      return;
    }

    setIsRollingBack(true);

    try {
      onBeforeRollback();
      await documentsApi.rollback(documentId, { targetSequence: rollbackTargetSequence });
      await onAfterRollback();
      setRollbackOpen(false);
      setRollbackPreview(null);
      message.success(`Document rolled back to sequence #${rollbackTargetSequence}`);
    } catch {
      message.error('Failed to roll back the document.');
    } finally {
      setIsRollingBack(false);
    }
  };

  return {
    handleOpenRollback,
    handleRollback,
    handleRollbackCancel,
    handleSelectRollbackVersion,
    isLoadingRollbackHistory,
    isLoadingRollbackPreview,
    isRollbackTargetInvalid,
    isRollingBack,
    rollbackOpen,
    rollbackPreview,
    rollbackTargetSequence,
    rollbackTimeline,
  };
};
