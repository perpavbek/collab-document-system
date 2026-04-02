import { Spin, Typography } from 'antd';

interface PageLoaderProps {
  label?: string;
  fullScreen?: boolean;
}

export const PageLoader = ({ label = 'Loading', fullScreen = false }: PageLoaderProps) => (
  <div className={fullScreen ? 'flex min-h-screen items-center justify-center' : 'flex min-h-[320px] items-center justify-center'}>
    <div className="glass-panel rounded-3xl border border-white/60 px-8 py-10 text-center shadow-soft">
      <Spin size="large" />
      <Typography.Paragraph className="!mb-0 !mt-4 !text-slate-600">{label}</Typography.Paragraph>
    </div>
  </div>
);
