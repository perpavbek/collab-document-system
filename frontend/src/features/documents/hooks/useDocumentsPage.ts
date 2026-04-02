import { useEffect, useState } from 'react';
import { App, Grid } from 'antd';
import { useNavigate } from 'react-router-dom';
import type { AxiosError } from 'axios';
import type { TablePaginationConfig } from 'antd/es/table';

import { documentsApi } from '../../../api/documents';
import { usersApi } from '../../../api/users';
import { useAuthStore } from '../../../stores/authStore';
import type { UserProfile } from '../../../types/auth';
import type { CreateDocumentRequest, DocumentDto } from '../../../types/documents';

export const useDocumentsPage = () => {
  const { message } = App.useApp();
  const screens = Grid.useBreakpoint();
  const navigate = useNavigate();
  const user = useAuthStore((state) => state.user);

  const [documents, setDocuments] = useState<DocumentDto[]>([]);
  const [owners, setOwners] = useState<Record<string, UserProfile>>({});
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [pagination, setPagination] = useState<TablePaginationConfig>({
    current: 1,
    pageSize: 10,
    total: 0,
  });

  const loadDocuments = async (page = 1, size = 10) => {
    setLoading(true);

    try {
      const response = await documentsApi.list(page - 1, size);
      const uniqueOwnerIds = Array.from(new Set(response.content.map((document) => document.ownerId)));
      const ownerProfiles = await Promise.all(
        uniqueOwnerIds.map(async (ownerId) => {
          if (user && ownerId === user.id) {
            return user;
          }

          try {
            return await usersApi.getById(ownerId);
          } catch {
            return null;
          }
        }),
      );

      setDocuments(response.content);
      setOwners((currentOwners) => {
        const nextOwners = { ...currentOwners };

        ownerProfiles.forEach((ownerProfile) => {
          if (ownerProfile) {
            nextOwners[ownerProfile.id] = ownerProfile;
          }
        });

        return nextOwners;
      });
      setPagination({
        current: response.page + 1,
        pageSize: response.size,
        total: response.totalElements,
      });
    } catch {
      message.error('Failed to load documents');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadDocuments();
  }, []);

  const currentPage = pagination.current ?? 1;
  const currentPageSize = pagination.pageSize ?? 10;

  const handlePageChange = (page: number, pageSize: number) => {
    void loadDocuments(page, pageSize);
  };

  const handleCreateDocument = async (payload: CreateDocumentRequest) => {
    setSubmitting(true);

    try {
      const document = await documentsApi.create(payload);
      message.success('Document created');
      setModalOpen(false);
      await loadDocuments(currentPage, currentPageSize);
      navigate(`/documents/${document.id}`);
    } catch (error) {
      const detail = (error as AxiosError<{ message?: string }>)?.response?.data?.message;
      message.error(detail ?? 'Failed to create document');
    } finally {
      setSubmitting(false);
    }
  };

  return {
    currentPage,
    currentPageSize,
    documents,
    handleCreateDocument,
    handleOpenDocument: (documentId: string) => navigate(`/documents/${documentId}`),
    handlePageChange,
    handleRefresh: () => void loadDocuments(currentPage, currentPageSize),
    isMobile: !screens.md,
    loading,
    modalOpen,
    owners,
    pagination,
    setModalOpen,
    submitting,
    user,
  };
};
