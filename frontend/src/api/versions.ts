import { http } from '../lib/http';
import type { EditOperation } from '../types/operations';

export const versionsApi = {
  async getContent(documentId: string) {
    const { data } = await http.get<string>(`/versions/documents/${documentId}/content`);
    return data;
  },

  async getOperations(documentId: string, after = 0) {
    const { data } = await http.get<EditOperation[]>(`/versions/documents/${documentId}/operations`, {
      params: { after },
    });
    return data;
  },
};
