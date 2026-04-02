import { Card, Empty, Typography } from 'antd';

import { RecentOperationsFeed } from '../../../components/RecentOperationsFeed';
import { SessionList } from '../../../components/SessionList';
import type { UserProfile } from '../../../types/auth';
import type { DocumentDto, EditingSession } from '../../../types/documents';
import type { EditOperation } from '../../../types/operations';

interface DocumentEditorSidebarProps {
  document: DocumentDto;
  participants: Record<string, UserProfile>;
  recentOperations: EditOperation[];
  sessions: EditingSession[];
}

export const DocumentEditorSidebar = ({
  document,
  participants,
  recentOperations,
  sessions,
}: DocumentEditorSidebarProps) => (
  <div className="space-y-6">
    <Card className="rounded-[28px] border-0 shadow-soft">
      <Typography.Title className="!mb-4 !text-xl !text-ink" level={4}>
        Active sessions
      </Typography.Title>
      <SessionList participants={participants} sessions={sessions} />
    </Card>

    <Card className="rounded-[28px] border-0 shadow-soft">
      <Typography.Title className="!mb-4 !text-xl !text-ink" level={4}>
        Recent activity
      </Typography.Title>
      <RecentOperationsFeed operations={recentOperations} participants={participants} />
    </Card>

    <Card className="rounded-[28px] border-0 shadow-soft">
      <Typography.Title className="!mb-4 !text-xl !text-ink" level={4}>
        Collaborators
      </Typography.Title>

      {document.collaborators.length === 0 ? (
        <Empty description="No collaborators added yet" image={Empty.PRESENTED_IMAGE_SIMPLE} />
      ) : (
        <div className="flex flex-wrap gap-3">
          {document.collaborators.map((collaborator) => (
            <div
              key={collaborator.userId}
              className="rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-700"
            >
              <div className="font-semibold text-ink">{participants[collaborator.userId]?.name ?? collaborator.userId}</div>
              <div className="mt-1 text-xs uppercase text-slate-500">{collaborator.role}</div>
            </div>
          ))}
        </div>
      )}
    </Card>
  </div>
);
