import { useState } from 'react';
import { App, Modal } from 'antd';
import { useNavigate, useParams } from 'react-router-dom';

import { documentsApi } from '../../../api/documents';
import { useAuthStore } from '../../../stores/authStore';
import type { CreateDocumentRequest } from '../../../types/documents';
import { useDocumentRollbackController } from './useDocumentRollbackController';
import { useDocumentWorkspaceSync } from './useDocumentWorkspaceSync';

export const useDocumentEditorPage = () => {
  const { message } = App.useApp();
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const currentUser = useAuthStore((state) => state.user);

  const [settingsOpen, setSettingsOpen] = useState(false);
  const [settingsSubmitting, setSettingsSubmitting] = useState(false);
  const {
    canEditContent,
    clearPendingEdits,
    content,
    currentSequenceNumber,
    document,
    handleEditorChange,
    handleResync,
    hydrateParticipants,
    isLoading,
    isResyncing,
    participants,
    permission,
    recentOperations,
    reloadWorkspace,
    saveState,
    seedUsers,
    sessions,
    updateDocumentDetails,
  } = useDocumentWorkspaceSync({
    currentUser,
    documentId: id,
  });
  const canManageDocument = permission === 'OWNER';
  const {
    handleOpenRollback,
    handleRollback,
    handleRollbackCancel,
    handleSelectRollbackVersion,
    isLoadingRollbackHistory,
    isLoadingRollbackPreview,
    isRollbackTargetInvalid,
    isRollingBack,
    rollbackOpen,
    rollbackPreview,
    rollbackTargetSequence,
    rollbackTimeline,
  } = useDocumentRollbackController({
    currentSequenceNumber,
    documentId: id,
    onAfterRollback: reloadWorkspace,
    onBeforeRollback: clearPendingEdits,
  });

  const handleDelete = () => {
    if (!document) {
      return;
    }

    Modal.confirm({
      title: 'Delete this document?',
      content: 'This action removes the document for everyone who has access.',
      okText: 'Delete',
      okButtonProps: {
        danger: true,
      },
      cancelText: 'Cancel',
      onOk: async () => {
        try {
          await documentsApi.remove(document.id);
          message.success('Document deleted');
          navigate('/documents', { replace: true });
        } catch {
          message.error('Failed to delete the document.');
          throw new Error('Delete document failed');
        }
      },
    });
  };

  const handleSettingsSubmit = async (payload: CreateDocumentRequest) => {
    if (!id) {
      return;
    }

    setSettingsSubmitting(true);

    try {
      const updatedDocument = await documentsApi.update(id, payload);
      updateDocumentDetails(updatedDocument);
      await hydrateParticipants([
        updatedDocument.ownerId,
        ...updatedDocument.collaborators.map((entry) => entry.userId),
        ...updatedDocument.pendingInvitations.map((entry) => entry.invitedUserId),
      ]);
      message.success('Document settings updated');
      setSettingsOpen(false);
    } catch {
      message.error('Failed to update document settings');
    } finally {
      setSettingsSubmitting(false);
    }
  };

  return {
    canEditContent,
    canManageDocument,
    content,
    currentSequenceNumber,
    currentUser,
    document,
    handleBack: () => navigate('/documents'),
    handleDelete,
    handleEditorChange,
    handleOpenRollback,
    handleResync,
    handleRollback,
    handleRollbackCancel,
    handleSelectRollbackVersion,
    handleSettingsSubmit,
    isLoading,
    isLoadingRollbackHistory,
    isLoadingRollbackPreview,
    isResyncing,
    isRollbackTargetInvalid,
    isRollingBack,
    participants,
    permission,
    recentOperations,
    rollbackOpen,
    rollbackPreview,
    rollbackTargetSequence,
    rollbackTimeline,
    saveState,
    seedUsers,
    sessions,
    settingsOpen,
    settingsSubmitting,
    setSettingsOpen,
  };
};
