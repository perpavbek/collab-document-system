import { App, Button, Form, Input, Typography } from 'antd';
import { LockOutlined, MailOutlined } from '@ant-design/icons';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import type { AxiosError } from 'axios';

import { AuthShell } from '../components/AuthShell';
import { useAuthStore } from '../stores/authStore';
import type { LoginRequest } from '../types/auth';

export const LoginPage = () => {
  const { message } = App.useApp();
  const navigate = useNavigate();
  const location = useLocation();
  const login = useAuthStore((state) => state.login);
  const isSubmitting = useAuthStore((state) => state.isSubmitting);

  return (
    <AuthShell title="Welcome back" subtitle="Sign in to manage documents, collaborators, and realtime sessions.">
      <Form<LoginRequest>
        layout="vertical"
        onFinish={async (values) => {
          try {
            await login(values);
            const nextPath = (location.state as { from?: { pathname?: string } } | null)?.from?.pathname ?? '/documents';
            navigate(nextPath, { replace: true });
          } catch (error) {
            const detail = (error as AxiosError<{ message?: string }>)?.response?.data?.message;
            message.error(detail ?? 'Login failed. Please check your credentials.');
          }
        }}
      >
        <Form.Item
          label="Email"
          name="email"
          rules={[
            { required: true, message: 'Please enter your email' },
            { type: 'email', message: 'Use a valid email address' },
          ]}
        >
          <Input prefix={<MailOutlined />} placeholder="name@example.com" size="large" />
        </Form.Item>

        <Form.Item
          label="Password"
          name="password"
          rules={[
            { required: true, message: 'Please enter your password' },
            { min: 6, message: 'Password must be at least 6 characters' },
          ]}
        >
          <Input.Password prefix={<LockOutlined />} placeholder="Your password" size="large" />
        </Form.Item>

        <Button block htmlType="submit" loading={isSubmitting} size="large" type="primary">
          Sign in
        </Button>
      </Form>

      <Typography.Paragraph className="!mb-0 !mt-6 !text-center !text-slate-500">
        No account yet? <Link to="/register">Create one</Link>
      </Typography.Paragraph>
    </AuthShell>
  );
};
