export type DocumentRole = 'OWNER' | 'EDITOR' | 'VIEWER' | string;

export interface DocumentCollaborator {
  userId: string;
  role: DocumentRole;
}

export interface DocumentDto {
  id: string;
  title: string;
  ownerId: string;
  versionSequenceNumber: number;
  createdAt: string;
  updatedAt: string;
  collaborators: DocumentCollaborator[];
}

export interface CreateDocumentRequest {
  title: string;
  collaboratorIds: string[];
}

export interface UpdateDocumentRequest {
  title?: string;
  collaboratorIds: string[];
}

export interface DocumentRollbackRequest {
  targetSequence: number;
}

export interface DocumentVersionResponse {
  content: string;
  sequenceNumber: number;
}

export interface DocumentPermissionResponse {
  role: DocumentRole;
}

export interface EditingSession {
  documentId: string;
  userId: string;
  connectedAt: string;
  lastActivityAt: string;
}
