import type { EditOperation, OperationEvent, RealtimeOperationMessage } from '../../../types/operations';

export const permissionColorMap: Record<string, string> = {
  OWNER: 'gold',
  EDITOR: 'blue',
  VIEWER: 'default',
};

export const SAVE_DEBOUNCE_MS = 800;
export const PREVIEW_EMPTY_HTML = '<p class="text-slate-400">No content at this version.</p>';

export const normalizeRollbackTargetSequence = (
  currentSequenceNumber: number,
  value: number | null | undefined,
) => {
  if (value === null || value === undefined || Number.isNaN(value)) {
    return null;
  }

  return Math.min(currentSequenceNumber, Math.max(0, Math.trunc(value)));
};

export const isEditOperationMessage = (messagePayload: RealtimeOperationMessage): messagePayload is EditOperation =>
  'position' in messagePayload && 'userId' in messagePayload;

export const isOperationEventMessage = (
  messagePayload: RealtimeOperationMessage,
): messagePayload is OperationEvent => !('position' in messagePayload) && 'type' in messagePayload;
