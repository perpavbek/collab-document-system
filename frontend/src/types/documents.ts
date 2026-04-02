export type DocumentRole = 'OWNER' | 'EDITOR' | 'VIEWER' | string;

export interface DocumentCollaborator {
  userId: string;
  role: DocumentRole;
}

export interface DocumentInvitation {
  id: string;
  invitedUserId: string;
  invitedEmail: string;
  role: DocumentRole;
  createdAt: string;
}

export interface DocumentDto {
  id: string;
  title: string;
  ownerId: string;
  versionSequenceNumber: number;
  createdAt: string;
  updatedAt: string;
  collaborators: DocumentCollaborator[];
  pendingInvitations: DocumentInvitation[];
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

export interface DocumentInvitationDetailsResponse {
  documentId: string;
  documentTitle: string;
  ownerId: string;
  invitedUserId: string;
  invitedEmail: string;
  role: DocumentRole;
}

export interface EditingSession {
  documentId: string;
  userId: string;
  connectedAt: string;
  lastActivityAt: string;
}
