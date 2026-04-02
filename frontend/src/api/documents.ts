import { http } from '../lib/http';
import type { PageResponse } from '../types/api';
import type {
  CreateDocumentRequest,
  DocumentDto,
  DocumentPermissionResponse,
  DocumentRollbackRequest,
  DocumentVersionResponse,
  EditingSession,
  UpdateDocumentRequest,
} from '../types/documents';
import type { EditOperation, EditOperationRequest } from '../types/operations';

export const documentsApi = {
  async list(page = 0, size = 10) {
    const { data } = await http.get<PageResponse<DocumentDto>>('/documents', {
      params: { page, size },
    });
    return data;
  },

  async create(payload: CreateDocumentRequest) {
    const { data } = await http.post<DocumentDto>('/documents', payload);
    return data;
  },

  async getById(id: string) {
    const { data } = await http.get<DocumentDto>(`/documents/${id}`);
    return data;
  },

  async getPermission(id: string) {
    const { data } = await http.get<DocumentPermissionResponse>(`/documents/${id}/permission`);
    return data;
  },

  async update(id: string, payload: UpdateDocumentRequest) {
    const { data } = await http.put<DocumentDto>(`/documents/${id}`, payload);
    return data;
  },

  async rollback(id: string, payload: DocumentRollbackRequest) {
    const { data } = await http.post(`/documents/${id}/rollback`, payload);
    return data;
  },

  async getVersion(documentId: string, sequenceNumber: number) {
    const { data } = await http.get<DocumentVersionResponse>(`/versions/documents/${documentId}/version/${sequenceNumber}`);
    return data;
  },

  async applyEdit(payload: EditOperationRequest) {
    const { data } = await http.put<EditOperation>('/documents/edit', payload);
    return data;
  },

  async remove(id: string) {
    await http.delete(`/documents/${id}`);
  },

  async getSessions(documentId: string) {
    const { data } = await http.get<EditingSession[]>(`/documents/${documentId}/sessions`);
    return data;
  },
};
