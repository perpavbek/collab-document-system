import { Card, Empty, Typography } from 'antd';

import { RecentOperationsFeed } from '../../../components/RecentOperationsFeed';
import { SessionList } from '../../../components/SessionList';
import type { UserProfile } from '../../../types/auth';
import type { DocumentDto, EditingSession } from '../../../types/documents';
import type { EditOperation } from '../../../types/operations';

interface DocumentEditorSidebarProps {
  canManageDocument: boolean;
  document: DocumentDto;
  participants: Record<string, UserProfile>;
  recentOperations: EditOperation[];
  sessions: EditingSession[];
}

export const DocumentEditorSidebar = ({
  canManageDocument,
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

      {canManageDocument ? (
        <div className="mt-6 border-t border-slate-100 pt-6">
          <Typography.Title className="!mb-3 !text-base !text-ink" level={5}>
            Pending invitations
          </Typography.Title>

          {document.pendingInvitations.length === 0 ? (
            <Typography.Text className="!text-slate-500">
              There are no pending invitations for this document.
            </Typography.Text>
          ) : (
            <div className="flex flex-wrap gap-3">
              {document.pendingInvitations.map((invitation) => (
                <div
                  key={invitation.id}
                  className="rounded-2xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-900"
                >
                  <div className="font-semibold text-ink">
                    {participants[invitation.invitedUserId]?.name ?? invitation.invitedEmail}
                  </div>
                  <div className="mt-1 text-xs uppercase text-amber-700">{invitation.role}</div>
                  <div className="mt-1 text-xs text-amber-800">{invitation.invitedEmail}</div>
                </div>
              ))}
            </div>
          )}
        </div>
      ) : null}
    </Card>
  </div>
);
