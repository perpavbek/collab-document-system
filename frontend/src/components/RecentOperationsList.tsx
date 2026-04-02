import { Empty, List, Tag, Typography } from 'antd';

import type { UserProfile } from '../types/auth';
import type { EditOperation } from '../types/operations';
import { truncateText } from '../utils/format';

interface RecentOperationsListProps {
  operations: EditOperation[];
  participants: Record<string, UserProfile>;
}

export const RecentOperationsList = ({ operations, participants }: RecentOperationsListProps) => {
  if (operations.length === 0) {
    return <Empty description="No edits yet" image={Empty.PRESENTED_IMAGE_SIMPLE} />;
  }

  return (
    <List
      dataSource={operations}
      renderItem={(operation) => {
        const user = participants[operation.userId];
        const preview = operation.type === 'DELETE' ? `${operation.length} chars removed` : truncateText(operation.content || 'Inserted text');

        return (
          <List.Item>
            <div className="w-full">
              <div className="flex items-center justify-between gap-3">
                <div className="font-medium text-ink">{user?.name ?? operation.userId}</div>
                <Tag color={operation.type === 'DELETE' ? 'volcano' : 'green'}>{operation.type}</Tag>
              </div>
              <Typography.Paragraph className="!mb-1 !mt-2 !text-sm !text-slate-600">
                {preview}
              </Typography.Paragraph>
              <Typography.Text className="!text-xs !text-slate-400">
                Position {operation.position} · Version #{operation.sequenceNumber}
              </Typography.Text>
            </div>
          </List.Item>
        );
      }}
    />
  );
};
