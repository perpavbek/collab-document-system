import { documentsApi } from '../../../api/documents';
import { usersApi } from '../../../api/users';
import type { UserProfile } from '../../../types/auth';
import type { EditingSession } from '../../../types/documents';
import type { EditOperation } from '../../../types/operations';
import { applyOperationToContent } from '../../../utils/operations';

export const buildOptimisticEditingSession = (
  documentId: string | undefined,
  currentUser: UserProfile | null,
  isSocketConnected: boolean,
): EditingSession | null => {
  if (!documentId || !currentUser || !isSocketConnected) {
    return null;
  }

  const now = new Date().toISOString();

  return {
    documentId,
    userId: currentUser.id,
    connectedAt: now,
    lastActivityAt: now,
  };
};

export const getDocumentContentAtSequence = async (
  documentId: string,
  sequenceNumber: number,
  operations: EditOperation[],
) => {
  try {
    const versionResponse = await documentsApi.getVersion(documentId, sequenceNumber);
    return versionResponse.content;
  } catch {
    const relevantOperations = operations.filter((operation) => operation.sequenceNumber <= sequenceNumber);

    return relevantOperations
      .slice()
      .sort((left, right) => left.sequenceNumber - right.sequenceNumber)
      .reduce((documentContent, operation) => applyOperationToContent(documentContent, operation), '');
  }
};

export const resolveUserProfiles = async (userIds: string[]) => {
  const uniqueIds = Array.from(new Set(userIds.filter(Boolean)));

  const results = await Promise.all(
    uniqueIds.map(async (userId) => {
      try {
        return await usersApi.getById(userId);
      } catch {
        return null;
      }
    }),
  );

  return results.filter((entry): entry is UserProfile => Boolean(entry));
};
