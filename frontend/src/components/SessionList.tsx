import { Avatar, Empty, List, Tag, Typography } from 'antd';

import type { UserProfile } from '../types/auth';
import type { EditingSession } from '../types/documents';
import { formatDateTime, getInitials } from '../utils/format';

interface SessionListProps {
  sessions: EditingSession[];
  participants: Record<string, UserProfile>;
}

export const SessionList = ({ sessions, participants }: SessionListProps) => {
  if (sessions.length === 0) {
    return <Empty description="No active sessions right now" image={Empty.PRESENTED_IMAGE_SIMPLE} />;
  }

  return (
    <List
      dataSource={sessions}
      renderItem={(session) => {
        const user = participants[session.userId];

        return (
          <List.Item className="!flex !flex-col !items-start !gap-3 sm:!flex-row sm:!items-start sm:!justify-between">
            <div className="flex min-w-0 flex-1 items-start gap-3">
              <Avatar className="mt-0.5 shrink-0 bg-emerald-700">{getInitials(user?.name ?? session.userId)}</Avatar>
              <div className="min-w-0">
                <Typography.Text className="block !font-medium !text-ink">{user?.name ?? session.userId}</Typography.Text>
                <Typography.Paragraph className="!mb-0 !mt-1 !break-all !text-sm !text-slate-500">
                  {user?.email ?? 'User details are loading'}
                </Typography.Paragraph>
              </div>
            </div>
            <div className="w-full text-left sm:w-auto sm:shrink-0 sm:text-right">
              <Tag color="blue">Active</Tag>
              <Typography.Paragraph className="!mb-0 !mt-2 !text-xs !text-slate-500">
                Last activity: {formatDateTime(session.lastActivityAt)}
              </Typography.Paragraph>
            </div>
          </List.Item>
        );
      }}
    />
  );
};
