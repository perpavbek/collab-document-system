import { useEffect, useState } from 'react';
import { App, Button, Card, Descriptions, Result, Space, Tag, Typography } from 'antd';
import { CheckCircleOutlined, MailOutlined } from '@ant-design/icons';
import type { AxiosError } from 'axios';
import { useNavigate, useParams } from 'react-router-dom';

import { documentsApi } from '../api/documents';
import { usersApi } from '../api/users';
import { PageLoader } from '../components/PageLoader';
import type { UserProfile } from '../types/auth';
import type { DocumentInvitationDetailsResponse } from '../types/documents';

export const DocumentInvitationPage = () => {
  const { message } = App.useApp();
  const navigate = useNavigate();
  const { token } = useParams<{ token: string }>();

  const [accepting, setAccepting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [invitation, setInvitation] = useState<DocumentInvitationDetailsResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [owner, setOwner] = useState<UserProfile | null>(null);

  useEffect(() => {
    let cancelled = false;

    const loadInvitation = async () => {
      if (!token) {
        setError('Invitation link is invalid.');
        setIsLoading(false);
        return;
      }

      setIsLoading(true);
      setError(null);

      try {
        const details = await documentsApi.getInvitation(token);

        if (cancelled) {
          return;
        }

        setInvitation(details);

        try {
          const ownerProfile = await usersApi.getById(details.ownerId);

          if (!cancelled) {
            setOwner(ownerProfile);
          }
        } catch {
          if (!cancelled) {
            setOwner(null);
          }
        }
      } catch (requestError) {
        if (!cancelled) {
          const detail = (requestError as AxiosError<{ message?: string }>)?.response?.data?.message;
          setError(detail ?? 'Failed to load the invitation.');
        }
      } finally {
        if (!cancelled) {
          setIsLoading(false);
        }
      }
    };

    void loadInvitation();

    return () => {
      cancelled = true;
    };
  }, [token]);

  if (isLoading) {
    return <PageLoader label="Loading invitation" />;
  }

  if (!invitation || error) {
    return (
      <div className="mx-auto max-w-3xl">
        <Result
          extra={
            <Button onClick={() => navigate('/documents')} type="primary">
              Back to documents
            </Button>
          }
          status="warning"
          subTitle={error ?? 'This invitation is no longer available.'}
          title="Invitation unavailable"
        />
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-3xl">
      <Card className="rounded-[28px] border-0 shadow-soft">
        <Space className="w-full" direction="vertical" size={24}>
          <div>
            <Tag color="blue">{invitation.role}</Tag>
            <Typography.Title className="!mb-3 !mt-4 !text-3xl !text-ink" level={2}>
              Accept document invitation
            </Typography.Title>
            <Typography.Paragraph className="!mb-0 !text-base !text-slate-600">
              {owner?.name ?? owner?.email ?? 'A collaborator'} invited you to join{' '}
              <span className="font-semibold text-ink">{invitation.documentTitle}</span>. After acceptance the
              document will appear in your workspace and you will be able to edit it.
            </Typography.Paragraph>
          </div>

          <Descriptions
            className="rounded-[24px] border border-slate-200 bg-slate-50 px-4 py-3"
            column={1}
            items={[
              {
                key: 'document',
                label: 'Document',
                children: invitation.documentTitle,
              },
              {
                key: 'email',
                label: 'Invited email',
                children: (
                  <Space size="small">
                    <MailOutlined />
                    <span>{invitation.invitedEmail}</span>
                  </Space>
                ),
              },
            ]}
          />

          <Space wrap>
            <Button
              icon={<CheckCircleOutlined />}
              loading={accepting}
              onClick={() => {
                if (!token) {
                  return;
                }

                setAccepting(true);

                void documentsApi
                  .acceptInvitation(token)
                  .then((document) => {
                    message.success('Invitation accepted');
                    navigate(`/documents/${document.id}`, { replace: true });
                  })
                  .catch((requestError: unknown) => {
                    const detail = (requestError as AxiosError<{ message?: string }>)?.response?.data?.message;
                    message.error(detail ?? 'Failed to accept the invitation.');
                  })
                  .finally(() => {
                    setAccepting(false);
                  });
              }}
              size="large"
              type="primary"
            >
              Accept invitation
            </Button>

            <Button onClick={() => navigate('/documents')} size="large">
              Maybe later
            </Button>
          </Space>
        </Space>
      </Card>
    </div>
  );
};
