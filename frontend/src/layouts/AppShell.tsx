import { Avatar, Button, Layout, Space, Typography } from 'antd';
import { LogoutOutlined } from '@ant-design/icons';
import { Outlet, useNavigate } from 'react-router-dom';

import { getInitials } from '../utils/format';
import { useAuthStore } from '../stores/authStore';

const { Header, Content } = Layout;

export const AppShell = () => {
  const navigate = useNavigate();
  const user = useAuthStore((state) => state.user);
  const logout = useAuthStore((state) => state.logout);

  return (
    <Layout className="!bg-transparent">
      <Header className="z-20 border-b !h-16 border-white/40 bg-white/70 px-4 backdrop-blur md:px-8">
        <div className="mx-auto flex h-full max-w-7xl items-center justify-between">
          <button
            className="flex items-center gap-3 border-0 bg-transparent p-0 text-left text-ink"
            onClick={() => navigate('/documents')}
            type="button"
          >
            <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-ink text-sm font-semibold text-white shadow-soft">
              CD
            </div>
            <div className="leading-none">
              <Typography.Title className="!mb-0 !text-base !text-ink" level={5}>
                Collab Documents
              </Typography.Title>
            </div>
          </button>

          <Space size="middle">
            {user ? (
              <div className="hidden items-center gap-3 rounded-full border border-emerald-100 bg-white/80 px-3 py-2 md:flex">
                <Avatar className="bg-emerald-700">{getInitials(user.name)}</Avatar>
                <div className="leading-tight">
                  <div className="text-sm font-semibold text-ink">{user.name}</div>
                  <div className="text-xs text-slate-500">{user.email}</div>
                </div>
              </div>
            ) : null}

            <Button
              icon={<LogoutOutlined />}
              onClick={() => {
                logout();
                navigate('/login', { replace: true });
              }}
            >
              Logout
            </Button>
          </Space>
        </div>
      </Header>

      <Content className="!bg-transparent px-4 py-6 md:px-8 md:py-8">
        <div className="mx-auto max-w-7xl">
          <Outlet />
        </div>
      </Content>
    </Layout>
  );
};
