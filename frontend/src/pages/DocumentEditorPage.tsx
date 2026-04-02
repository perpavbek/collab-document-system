import { Card, Space, Tag, Typography } from 'antd';

import { CollaborativeEditor } from '../components/CollaborativeEditor';
import { DocumentFormModal } from '../components/DocumentFormModal';
import { PageLoader } from '../components/PageLoader';
import { DocumentEditorHeaderCard } from '../features/documents/components/DocumentEditorHeaderCard';
import { DocumentEditorSidebar } from '../features/documents/components/DocumentEditorSidebar';
import { DocumentRollbackModal } from '../features/documents/components/DocumentRollbackModal';
import { useDocumentEditorPage } from '../features/documents/hooks/useDocumentEditorPage';

export const DocumentEditorPage = () => {
  const {
    canEditContent,
    canManageDocument,
    content,
    currentSequenceNumber,
    currentUser,
    document,
    handleBack,
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
  } = useDocumentEditorPage();

  if (isLoading || !document || !permission) {
    return <PageLoader label="Loading document workspace" />;
  }

  return (
    <div className="space-y-6">
      <DocumentEditorHeaderCard
        canManageDocument={canManageDocument}
        currentSequenceNumber={currentSequenceNumber}
        document={document}
        isResyncing={isResyncing}
        participants={participants}
        permission={permission}
        sessionsCount={sessions.length}
        onBack={handleBack}
        onDelete={handleDelete}
        onOpenRollback={handleOpenRollback}
        onOpenSettings={() => setSettingsOpen(true)}
        onResync={() => void handleResync()}
      />

      <div className="grid gap-6 xl:grid-cols-[minmax(0,1.7fr)_minmax(340px,0.9fr)]">
        <Card className="document-editor-card h-full rounded-[28px] border-0 shadow-soft">
          <div className="mb-4 flex items-center justify-between gap-4">
            <div>
              <Typography.Text className="!text-slate-500">
                {canEditContent
                  ? 'Changes are buffered briefly before syncing.'
                  : 'You have viewer access to this document.'}
              </Typography.Text>
            </div>
            <Space wrap>
              {canEditContent ? (
                <Tag color={saveState === 'saved' ? 'green' : saveState === 'saving' ? 'blue' : 'gold'}>
                  {saveState === 'saved' ? 'Saved' : saveState === 'saving' ? 'Saving...' : 'Changes buffered'}
                </Tag>
              ) : null}
              <Tag color={canEditContent ? 'green' : 'default'}>{canEditContent ? 'Editable' : 'Read only'}</Tag>
            </Space>
          </div>

          <CollaborativeEditor onChange={handleEditorChange} readOnly={!canEditContent} value={content} />
        </Card>

        <DocumentEditorSidebar
          document={document}
          participants={participants}
          recentOperations={recentOperations}
          sessions={sessions}
        />
      </div>

      <DocumentRollbackModal
        currentContent={content}
        currentSequenceNumber={currentSequenceNumber}
        isLoadingRollbackHistory={isLoadingRollbackHistory}
        isLoadingRollbackPreview={isLoadingRollbackPreview}
        isRollingBack={isRollingBack}
        isRollbackTargetInvalid={isRollbackTargetInvalid}
        open={rollbackOpen}
        participants={participants}
        rollbackPreview={rollbackPreview}
        rollbackTargetSequence={rollbackTargetSequence}
        rollbackTimeline={rollbackTimeline}
        onCancel={handleRollbackCancel}
        onConfirm={() => void handleRollback()}
        onSelectVersion={(sequenceNumber) => void handleSelectRollbackVersion(sequenceNumber)}
      />

      <DocumentFormModal
        excludedUserIds={currentUser ? [currentUser.id] : []}
        initialValues={{
          title: document.title,
          collaboratorIds: document.collaborators.map((entry) => entry.userId),
        }}
        mode="edit"
        open={settingsOpen}
        seedUsers={seedUsers}
        submitting={settingsSubmitting}
        onCancel={() => setSettingsOpen(false)}
        onSubmit={handleSettingsSubmit}
      />
    </div>
  );
};
