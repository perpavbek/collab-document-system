import { Empty, List, Tag, Typography } from 'antd';

import type { UserProfile } from '../types/auth';
import type { EditOperation } from '../types/operations';

interface RecentOperationsFeedProps {
  operations: EditOperation[];
  participants: Record<string, UserProfile>;
}

const stripHtml = (value: string) => value.replace(/<[^>]+>/g, ' ').replace(/\s+/g, ' ').trim();

const createPreview = (operation: EditOperation) => {
  if (operation.type === 'DELETE') {
    return `${operation.length} chars removed`;
  }

  const plainText = stripHtml(operation.content || '');
  const fallbackText = plainText || 'Formatting updated';

  if (fallbackText.length <= 48) {
    return fallbackText;
  }

  return `${fallbackText.slice(0, 45)}...`;
};

export const RecentOperationsFeed = ({ operations, participants }: RecentOperationsFeedProps) => {
  if (operations.length === 0) {
    return <Empty description="No edits yet" image={Empty.PRESENTED_IMAGE_SIMPLE} />;
  }

  return (
    <div className="max-h-[340px] overflow-y-auto pr-1">
      <List
        dataSource={operations}
        renderItem={(operation) => {
          const user = participants[operation.userId];

          return (
            <List.Item>
              <div className="w-full">
                <div className="flex items-center justify-between gap-3">
                  <div className="font-medium text-ink">{user?.name ?? operation.userId}</div>
                  <Tag color={operation.type === 'DELETE' ? 'volcano' : 'green'}>{operation.type}</Tag>
                </div>
                <Typography.Paragraph className="!mb-1 !mt-2 !text-sm !text-slate-600">
                  {createPreview(operation)}
                </Typography.Paragraph>
                <Typography.Text className="!text-xs !text-slate-400">
                  Position {operation.position} | Version #{operation.sequenceNumber}
                </Typography.Text>
              </div>
            </List.Item>
          );
        }}
      />
    </div>
  );
};
