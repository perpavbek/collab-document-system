import type { PropsWithChildren } from 'react';
import { Typography } from 'antd';

interface AuthShellProps extends PropsWithChildren {
  title: string;
  subtitle: string;
}

export const AuthShell = ({ title, subtitle, children }: AuthShellProps) => (
  <div className="min-h-screen bg-mesh-paper px-4 py-10">
    <div className="mx-auto grid min-h-[calc(100vh-5rem)] max-w-lg items-center gap-10">
      <div className="glass-panel rounded-[32px] border border-white/70 p-6 shadow-soft md:p-8">
        <Typography.Title className="!mb-2 !text-3xl !text-ink text-center" level={2}>
          {title}
        </Typography.Title>
        <Typography.Paragraph className="!mb-8 !text-slate-600 text-center">{subtitle}</Typography.Paragraph>
        {children}
      </div>
    </div>
  </div>
);
