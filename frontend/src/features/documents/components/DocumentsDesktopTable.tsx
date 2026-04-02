import { Button, Empty, Table, Tag } from 'antd';
import { FolderOpenOutlined } from '@ant-design/icons';
import type { ColumnsType, TablePaginationConfig } from 'antd/es/table';

import type { UserProfile } from '../../../types/auth';
import type { DocumentDto } from '../../../types/documents';
import { formatDateTime } from '../../../utils/format';

interface DocumentsDesktopTableProps {
  documents: DocumentDto[];
  loading: boolean;
  owners: Record<string, UserProfile>;
  pagination: TablePaginationConfig;
  onOpenDocument: (documentId: string) => void;
  onPageChange: (page: number, pageSize: number) => void;
}

export const DocumentsDesktopTable = ({
  documents,
  loading,
  owners,
  pagination,
  onOpenDocument,
  onPageChange,
}: DocumentsDesktopTableProps) => {
  const columns: ColumnsType<DocumentDto> = [
    {
      title: 'Document',
      dataIndex: 'title',
      key: 'title',
      render: (_, record) => (
        <div>
          <div className="font-semibold text-ink">{record.title}</div>
          <div className="mt-1 text-xs text-slate-500">Owner: {owners[record.ownerId]?.name ?? 'Unknown user'}</div>
          <div className="text-xs text-slate-400">{owners[record.ownerId]?.email ?? record.ownerId}</div>
        </div>
      ),
    },
    {
      title: 'Version',
      dataIndex: 'versionSequenceNumber',
      key: 'versionSequenceNumber',
      render: (value: number) => <Tag color="gold">#{value}</Tag>,
    },
    {
      title: 'Collaborators',
      key: 'collaborators',
      render: (_, record) => record.collaborators.length,
    },
    {
      title: 'Updated',
      dataIndex: 'updatedAt',
      key: 'updatedAt',
      render: (value: string) => formatDateTime(value),
    },
    {
      title: 'Actions',
      key: 'actions',
      render: (_, record) => (
        <Button icon={<FolderOpenOutlined />} onClick={() => onOpenDocument(record.id)} type="primary">
          Open
        </Button>
      ),
    },
  ];

  return (
    <Table
      rowKey="id"
      columns={columns}
      dataSource={documents}
      loading={loading}
      locale={{
        emptyText: <Empty description="No documents yet. Create the first one to start collaborating." />,
      }}
      pagination={pagination}
      onChange={(nextPagination) => {
        onPageChange(nextPagination.current ?? 1, nextPagination.pageSize ?? 10);
      }}
    />
  );
};
