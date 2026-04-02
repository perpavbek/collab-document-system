import { DocumentFormModal } from '../components/DocumentFormModal';
import { DocumentsDesktopTable } from '../features/documents/components/DocumentsDesktopTable';
import { DocumentsHero } from '../features/documents/components/DocumentsHero';
import { DocumentsMobileList } from '../features/documents/components/DocumentsMobileList';
import { useDocumentsPage } from '../features/documents/hooks/useDocumentsPage';

export const DocumentsPage = () => {
  const {
    currentPage,
    currentPageSize,
    documents,
    handleCreateDocument,
    handleOpenDocument,
    handlePageChange,
    handleRefresh,
    isMobile,
    loading,
    modalOpen,
    owners,
    pagination,
    setModalOpen,
    submitting,
    user,
  } = useDocumentsPage();

  return (
    <div className="space-y-6">
      <DocumentsHero userName={user?.name} onCreate={() => setModalOpen(true)} onRefresh={handleRefresh} />

      <div className="rounded-[28px] border-0 bg-white shadow-soft">
        {isMobile ? (
          <div className="p-6">
            <DocumentsMobileList
              currentPage={currentPage}
              currentPageSize={currentPageSize}
              documents={documents}
              loading={loading}
              owners={owners}
              total={pagination.total}
              onOpenDocument={handleOpenDocument}
              onPageChange={handlePageChange}
            />
          </div>
        ) : (
          <DocumentsDesktopTable
            documents={documents}
            loading={loading}
            owners={owners}
            pagination={pagination}
            onOpenDocument={handleOpenDocument}
            onPageChange={handlePageChange}
          />
        )}
      </div>

      <DocumentFormModal
        excludedUserIds={user ? [user.id] : []}
        mode="create"
        open={modalOpen}
        submitting={submitting}
        onCancel={() => setModalOpen(false)}
        onSubmit={handleCreateDocument}
      />
    </div>
  );
};
