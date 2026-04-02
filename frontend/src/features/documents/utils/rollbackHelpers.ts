import type { EditOperation } from '../../../types/operations';
import { applyOperationToContent, createOperationsFromDiff } from '../../../utils/operations';

export interface RollbackTimelineEntry {
  sequenceNumber: number;
  userId: string | null;
  changePreview: string;
  contentPreview: string;
  summary: string;
  targetVisibleLength: number;
  type: 'INITIAL' | 'INSERT' | 'DELETE';
}

export interface RollbackComparisonSummary {
  formattingChanged: boolean;
  hasChanges: boolean;
  targetVisibleLength: number;
  visibleDelta: number;
}

const EMPTY_VERSION_PREVIEW = 'This version has no visible text yet.';
const truncatePreview = (value: string, maxLength: number) =>
  value.length <= maxLength ? value : `${value.slice(0, Math.max(0, maxLength - 3))}...`;

export const stripHtmlToText = (value: string) => value.replace(/<[^>]+>/g, ' ').replace(/\s+/g, ' ').trim();

const summarizeOperation = (operation: EditOperation) => {
  if (operation.type === 'DELETE') {
    return `Removed ${operation.length} chars at position ${operation.position}`;
  }

  const insertedText = stripHtmlToText(operation.content || '');

  if (insertedText.length > 0) {
    return `Added ${insertedText.length} chars at position ${operation.position}`;
  }

  return `Updated formatting at position ${operation.position}`;
};

const describeOperationPreview = (operation: EditOperation) => {
  if (operation.type === 'DELETE') {
    return `${operation.length} chars removed`;
  }

  const insertedText = stripHtmlToText(operation.content || '');

  return truncatePreview(insertedText || 'Formatting or structure updated', 88);
};

const describeVersionPreview = (content: string) => {
  const textContent = stripHtmlToText(content);

  if (!textContent) {
    return EMPTY_VERSION_PREVIEW;
  }

  return truncatePreview(textContent, 120);
};

export const buildRollbackTimeline = (operations: EditOperation[], currentSequenceNumber: number): RollbackTimelineEntry[] => {
  const orderedOperations = operations.slice().sort((left, right) => left.sequenceNumber - right.sequenceNumber);
  const timeline: RollbackTimelineEntry[] = [
    {
      sequenceNumber: 0,
      userId: null,
      changePreview: 'Initial empty state',
      contentPreview: EMPTY_VERSION_PREVIEW,
      summary: 'Document before any edits',
      targetVisibleLength: 0,
      type: 'INITIAL',
    },
  ];

  let currentContent = '';

  for (const operation of orderedOperations) {
    currentContent = applyOperationToContent(currentContent, operation);

    timeline.push({
      sequenceNumber: operation.sequenceNumber,
      userId: operation.userId,
      changePreview: describeOperationPreview(operation),
      contentPreview: describeVersionPreview(currentContent),
      summary: summarizeOperation(operation),
      targetVisibleLength: stripHtmlToText(currentContent).length,
      type: operation.type === 'DELETE' ? 'DELETE' : 'INSERT',
    });
  }

  return timeline
    .filter((entry) => entry.sequenceNumber < currentSequenceNumber)
    .sort((left, right) => right.sequenceNumber - left.sequenceNumber);
};

export const buildRollbackComparison = (currentContent: string, targetContent: string): RollbackComparisonSummary => {
  const currentText = stripHtmlToText(currentContent);
  const targetText = stripHtmlToText(targetContent);

  return {
    formattingChanged: currentText === targetText && currentContent !== targetContent,
    hasChanges: currentContent !== targetContent,
    targetVisibleLength: targetText.length,
    visibleDelta: targetText.length - currentText.length,
  };
};

export const hasRollbackDiff = (currentContent: string, targetContent: string) =>
  createOperationsFromDiff(currentContent, targetContent).length > 0;
