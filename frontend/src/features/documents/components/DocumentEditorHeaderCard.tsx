import { Button, Card, Descriptions, Space, Tag, Typography } from 'antd';
import {
  ArrowLeftOutlined,
  DeleteOutlined,
  EditOutlined,
  HistoryOutlined,
  ReloadOutlined,
  SyncOutlined,
} from '@ant-design/icons';

import type { UserProfile } from '../../../types/auth';
import type { DocumentDto, DocumentRole } from '../../../types/documents';
import { formatDateTime } from '../../../utils/format';
import { permissionColorMap } from '../utils/editorHelpers';

interface DocumentEditorHeaderCardProps {
  canManageDocument: boolean;
  currentSequenceNumber: number;
  document: DocumentDto;
  isResyncing: boolean;
  participants: Record<string, UserProfile>;
  permission: DocumentRole;
  sessionsCount: number;
  onBack: () => void;
  onDelete: () => void;
  onOpenRollback: () => void;
  onOpenSettings: () => void;
  onResync: () => void;
}

export const DocumentEditorHeaderCard = ({
  canManageDocument,
  currentSequenceNumber,
  document,
  isResyncing,
  participants,
  permission,
  sessionsCount,
  onBack,
  onDelete,
  onOpenRollback,
  onOpenSettings,
  onResync,
}: DocumentEditorHeaderCardProps) => (
  <Card className="rounded-[28px] border-0 shadow-soft">
    <div className="flex flex-col gap-5 lg:flex-row lg:items-start lg:justify-between">
      <div className="space-y-4">
        <Button icon={<ArrowLeftOutlined />} onClick={onBack}>
          Back to documents
        </Button>

        <div>
          <Space align="center" size="middle" wrap>
            <Typography.Title className="!mb-0 !text-3xl !text-ink" level={2}>
              {document.title}
            </Typography.Title>
            <Tag color={permissionColorMap[permission] ?? 'default'}>{permission}</Tag>
            <Tag icon={<SyncOutlined spin={false} />} color="cyan">
              Version #{currentSequenceNumber}
            </Tag>
          </Space>
        </div>
      </div>

      <Space wrap>
        <Button icon={<ReloadOutlined />} loading={isResyncing} onClick={onResync}>
          Resync
        </Button>
        {canManageDocument ? (
          <Button disabled={currentSequenceNumber <= 0} icon={<HistoryOutlined />} onClick={onOpenRollback}>
            Rollback
          </Button>
        ) : null}
        {canManageDocument ? (
          <Button icon={<EditOutlined />} onClick={onOpenSettings}>
            Settings
          </Button>
        ) : null}
        {canManageDocument ? (
          <Button danger icon={<DeleteOutlined />} onClick={onDelete}>
            Delete
          </Button>
        ) : null}
      </Space>
    </div>

    <Descriptions
      className="!mt-6"
      column={{ xs: 1, md: 2, xl: 4 }}
      items={[
        {
          key: 'owner',
          label: 'Owner',
          children: participants[document.ownerId]?.name ?? document.ownerId,
        },
        {
          key: 'createdAt',
          label: 'Created',
          children: formatDateTime(document.createdAt),
        },
        {
          key: 'updatedAt',
          label: 'Updated',
          children: formatDateTime(document.updatedAt),
        },
        {
          key: 'sessions',
          label: 'Active sessions',
          children: sessionsCount,
        },
      ]}
    />
  </Card>
);
