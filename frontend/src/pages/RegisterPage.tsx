import { App, Button, Form, Input, Typography } from 'antd';
import { LockOutlined, MailOutlined, UserOutlined } from '@ant-design/icons';
import { Link, useNavigate } from 'react-router-dom';
import type { AxiosError } from 'axios';

import { AuthShell } from '../components/AuthShell';
import { useAuthStore } from '../stores/authStore';
import type { RegisterRequest } from '../types/auth';

export const RegisterPage = () => {
  const { message } = App.useApp();
  const navigate = useNavigate();
  const register = useAuthStore((state) => state.register);
  const isSubmitting = useAuthStore((state) => state.isSubmitting);

  return (
    <AuthShell title="Create your account" subtitle="Register once and start collaborating across documents immediately.">
      <Form<RegisterRequest>
        layout="vertical"
        onFinish={async (values) => {
          try {
            await register(values);
            navigate('/documents', { replace: true });
          } catch (error) {
            const detail = (error as AxiosError<{ message?: string }>)?.response?.data?.message;
            message.error(detail ?? 'Registration failed. Please try again.');
          }
        }}
      >
        <Form.Item label="Name" name="name" rules={[{ required: true, message: 'Please enter your name' }]}>
          <Input prefix={<UserOutlined />} placeholder="Jane Collaborator" size="large" />
        </Form.Item>

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
            { required: true, message: 'Please create a password' },
            { min: 6, message: 'Password must be at least 6 characters' },
          ]}
        >
          <Input.Password prefix={<LockOutlined />} placeholder="Minimum 6 characters" size="large" />
        </Form.Item>

        <Button block htmlType="submit" loading={isSubmitting} size="large" type="primary">
          Create account
        </Button>
      </Form>

      <Typography.Paragraph className="!mb-0 !mt-6 !text-center !text-slate-500">
        Already registered? <Link to="/login">Sign in</Link>
      </Typography.Paragraph>
    </AuthShell>
  );
};
