import { create } from 'zustand';

import type { UserProfile } from '../types/auth';
import type { DocumentDto, DocumentRole, EditingSession } from '../types/documents';
import type { EditOperation } from '../types/operations';
import { applyOperationToContent } from '../utils/operations';

interface WorkspaceState {
  document: DocumentDto | null;
  content: string;
  permission: DocumentRole | null;
  sessions: EditingSession[];
  recentOperations: EditOperation[];
  participants: Record<string, UserProfile>;
  currentSequenceNumber: number;
  isLoading: boolean;
  setLoading: (value: boolean) => void;
  reset: () => void;
  setWorkspace: (payload: {
    document: DocumentDto;
    content: string;
    permission: DocumentRole;
    sessions: EditingSession[];
    operations: EditOperation[];
    participants: UserProfile[];
  }) => void;
  setContent: (value: string) => void;
  setDocument: (document: DocumentDto) => void;
  setSessions: (sessions: EditingSession[]) => void;
  setParticipants: (users: UserProfile[]) => void;
  addParticipant: (user: UserProfile) => void;
  pushOperation: (operation: EditOperation, shouldApplyToContent: boolean) => void;
}

const initialState = {
  document: null,
  content: '',
  permission: null,
  sessions: [],
  recentOperations: [],
  participants: {},
  currentSequenceNumber: 0,
  isLoading: false,
};

export const useWorkspaceStore = create<WorkspaceState>((set) => ({
  ...initialState,

  setLoading(value) {
    set({ isLoading: value });
  },

  reset() {
    set(initialState);
  },

  setWorkspace(payload) {
    const participantMap = payload.participants.reduce<Record<string, UserProfile>>((accumulator, participant) => {
      accumulator[participant.id] = participant;
      return accumulator;
    }, {});

    const highestSequence = payload.operations.reduce(
      (maxValue, operation) => Math.max(maxValue, operation.sequenceNumber),
      payload.document.versionSequenceNumber,
    );

    set({
      document: payload.document,
      content: payload.content,
      permission: payload.permission,
      sessions: payload.sessions,
      recentOperations: payload.operations
        .slice()
        .sort((left, right) => right.sequenceNumber - left.sequenceNumber)
        .slice(0, 30),
      participants: participantMap,
      currentSequenceNumber: highestSequence,
      isLoading: false,
    });
  },

  setContent(value) {
    set({ content: value });
  },

  setDocument(document) {
    set({ document });
  },

  setSessions(sessions) {
    set({ sessions });
  },

  setParticipants(users) {
    set((state) => {
      const nextParticipants = { ...state.participants };

      users.forEach((user) => {
        nextParticipants[user.id] = user;
      });

      return { participants: nextParticipants };
    });
  },

  addParticipant(user) {
    set((state) => ({
      participants: {
        ...state.participants,
        [user.id]: user,
      },
    }));
  },

  pushOperation(operation, shouldApplyToContent) {
    set((state) => {
      if (state.recentOperations.some((entry) => entry.sequenceNumber === operation.sequenceNumber)) {
        return {
          currentSequenceNumber: Math.max(state.currentSequenceNumber, operation.sequenceNumber),
        };
      }

      return {
        content: shouldApplyToContent ? applyOperationToContent(state.content, operation) : state.content,
        currentSequenceNumber: Math.max(state.currentSequenceNumber, operation.sequenceNumber),
        recentOperations: [operation, ...state.recentOperations].slice(0, 30),
      };
    });
  },
}));
