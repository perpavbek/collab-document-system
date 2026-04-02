import type { EditOperation } from '../types/operations';

export const applyOperationToContent = (content: string, operation: Pick<EditOperation, 'type' | 'position' | 'length' | 'content'>) => {
  const before = content.slice(0, operation.position);
  const after = content.slice(operation.position + operation.length);

  if (operation.type === 'DELETE') {
    return `${before}${after}`;
  }

  return `${before}${operation.content}${after}`;
};

export const operationSignature = (operation: Pick<EditOperation, 'type' | 'position' | 'length' | 'content'>) =>
  `${operation.type}:${operation.position}:${operation.length}:${operation.content}`;

export const createOperationsFromDiff = (previousContent: string, nextContent: string) => {
  if (previousContent === nextContent) {
    return [];
  }

  let start = 0;
  const maxPrefixLength = Math.min(previousContent.length, nextContent.length);

  while (start < maxPrefixLength && previousContent[start] === nextContent[start]) {
    start += 1;
  }

  let previousEnd = previousContent.length - 1;
  let nextEnd = nextContent.length - 1;

  while (previousEnd >= start && nextEnd >= start && previousContent[previousEnd] === nextContent[nextEnd]) {
    previousEnd -= 1;
    nextEnd -= 1;
  }

  const deletedLength = previousEnd >= start ? previousEnd - start + 1 : 0;
  const insertedContent = nextEnd >= start ? nextContent.slice(start, nextEnd + 1) : '';
  const operations: Array<Pick<EditOperation, 'type' | 'position' | 'length' | 'content'>> = [];

  if (deletedLength > 0) {
    operations.push({
      position: start,
      length: deletedLength,
      content: '',
      type: 'DELETE',
    });
  }

  if (insertedContent.length > 0) {
    operations.push({
      position: start,
      length: 0,
      content: insertedContent,
      type: 'INSERT',
    });
  }

  return operations;
};

export const reconstructContentFromOperations = (operations: EditOperation[]) =>
  operations
    .slice()
    .sort((left, right) => left.sequenceNumber - right.sequenceNumber)
    .reduce((content, operation) => applyOperationToContent(content, operation), '');
