export type OperationType = 'INSERT' | 'DELETE' | string;
export type OperationEventType = 'EDIT' | 'ROLLBACK' | 'CREATE' | string;

export interface EditOperationRequest {
  documentId: string;
  position: number;
  content: string;
  length: number;
  type: OperationType;
}

export interface EditOperation extends EditOperationRequest {
  userId: string;
  sequenceNumber: number;
}

export interface OperationEvent {
  documentId: string;
  type: OperationEventType;
  sequenceNumber: number;
}

export type RealtimeOperationMessage = EditOperation | OperationEvent;
