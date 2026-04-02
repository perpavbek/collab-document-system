import { Button, Card, Space, Typography } from 'antd';
import { FileAddOutlined, ReloadOutlined } from '@ant-design/icons';

interface DocumentsHeroProps {
  userName?: string;
  onCreate: () => void;
  onRefresh: () => void;
}

export const DocumentsHero = ({ userName, onCreate, onRefresh }: DocumentsHeroProps) => (
  <Card className="overflow-hidden rounded-[28px] border-0 shadow-soft">
    <div className="flex flex-col gap-6 md:flex-row md:items-end md:justify-between">
      <div className="max-w-2xl">
        <Typography.Text className="rounded-full border border-emerald-100 bg-emerald-50 px-3 py-1 text-xs uppercase tracking-[0.24em] text-emerald-700">
          Documents
        </Typography.Text>
        <Typography.Title className="!mb-2 !mt-4 !text-3xl !text-ink" level={2}>
          Hello {userName ?? 'Unknown user'}!
        </Typography.Title>
        <Typography.Paragraph className="!mb-0 !text-slate-600">
          Create a document, invite collaborators, and jump into the realtime editor.
        </Typography.Paragraph>
      </div>

      <Space wrap>
        <Button icon={<ReloadOutlined />} onClick={onRefresh}>
          Refresh
        </Button>
        <Button icon={<FileAddOutlined />} onClick={onCreate} size="large" type="primary">
          New document
        </Button>
      </Space>
    </div>
  </Card>
);
