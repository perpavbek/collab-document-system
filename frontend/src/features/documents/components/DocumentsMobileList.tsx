import { Button, Empty, Pagination, Tag, Typography } from 'antd';
import { FolderOpenOutlined } from '@ant-design/icons';

import type { UserProfile } from '../../../types/auth';
import type { DocumentDto } from '../../../types/documents';
import { formatDateTime } from '../../../utils/format';

interface DocumentsMobileListProps {
  currentPage: number;
  currentPageSize: number;
  documents: DocumentDto[];
  loading: boolean;
  owners: Record<string, UserProfile>;
  total?: number;
  onOpenDocument: (documentId: string) => void;
  onPageChange: (page: number, pageSize: number) => void;
}

export const DocumentsMobileList = ({
  currentPage,
  currentPageSize,
  documents,
  loading,
  owners,
  total,
  onOpenDocument,
  onPageChange,
}: DocumentsMobileListProps) => (
  <div className="space-y-4">
    {loading ? (
      <div className="rounded-[24px] border border-slate-200/80 bg-white/90 px-5 py-6 text-center shadow-sm">
        <Typography.Text className="!text-slate-500">Loading documents...</Typography.Text>
      </div>
    ) : documents.length === 0 ? (
      <Empty description="No documents yet. Create the first one to start collaborating." />
    ) : (
      documents.map((document) => (
        <div key={document.id} className="rounded-[24px] border border-slate-200/80 bg-white/90 p-4 shadow-sm">
          <div className="flex items-start justify-between gap-3">
            <div className="min-w-0">
              <Typography.Title className="!mb-2 !text-lg !leading-snug !text-ink" level={5}>
                {document.title}
              </Typography.Title>
              <Typography.Paragraph className="!mb-1 !text-sm !text-slate-500">
                Owner: {owners[document.ownerId]?.name ?? 'Unknown user'}
              </Typography.Paragraph>
              <Typography.Paragraph className="!mb-0 !break-all !text-xs !text-slate-400">
                {owners[document.ownerId]?.email ?? document.ownerId}
              </Typography.Paragraph>
            </div>
            <Tag className="!m-0" color="gold">
              #{document.versionSequenceNumber}
            </Tag>
          </div>

          <div className="mt-4 grid grid-cols-2 gap-3 rounded-2xl bg-slate-50 p-3">
            <div>
              <div className="text-[11px] uppercase tracking-[0.16em] text-slate-400">Collaborators</div>
              <div className="mt-1 text-sm font-semibold text-ink">{document.collaborators.length}</div>
            </div>
            <div>
              <div className="text-[11px] uppercase tracking-[0.16em] text-slate-400">Updated</div>
              <div className="mt-1 text-sm font-semibold text-ink">{formatDateTime(document.updatedAt)}</div>
            </div>
          </div>

          <Button
            block
            className="!mt-4"
            icon={<FolderOpenOutlined />}
            onClick={() => onOpenDocument(document.id)}
            type="primary"
          >
            Open document
          </Button>
        </div>
      ))
    )}

    {total ? (
      <div className="flex justify-center pt-2">
        <Pagination
          current={currentPage}
          pageSize={currentPageSize}
          responsive
          simple
          total={total}
          onChange={onPageChange}
        />
      </div>
    ) : null}
  </div>
);
