import { Form, Input, Modal } from 'antd';
import { useEffect } from 'react';

import { UserMultiSelect } from './UserMultiSelect';
import type { UserProfile } from '../types/auth';
import type { CreateDocumentRequest } from '../types/documents';

interface DocumentFormValues {
  title: string;
  collaboratorIds: string[];
}

interface DocumentFormModalProps {
  open: boolean;
  mode: 'create' | 'edit';
  submitting?: boolean;
  initialValues?: DocumentFormValues;
  seedUsers?: UserProfile[];
  excludedUserIds?: string[];
  onCancel: () => void;
  onSubmit: (payload: CreateDocumentRequest) => Promise<void>;
}

export const DocumentFormModal = ({
  open,
  mode,
  submitting = false,
  initialValues,
  seedUsers,
  excludedUserIds,
  onCancel,
  onSubmit,
}: DocumentFormModalProps) => {
  const [form] = Form.useForm<DocumentFormValues>();

  useEffect(() => {
    if (!open) {
      return;
    }

    form.setFieldsValue(
      initialValues ?? {
        title: '',
        collaboratorIds: [],
      },
    );
  }, [form, initialValues, open]);

  return (
    <Modal
      open={open}
      title={mode === 'create' ? 'Create document' : 'Edit document settings'}
      okText={mode === 'create' ? 'Create' : 'Save changes'}
      cancelText="Cancel"
      confirmLoading={submitting}
      onCancel={onCancel}
      onOk={() => {
        void form.submit();
      }}
      afterOpenChange={(isOpen) => {
        if (!isOpen) {
          form.resetFields();
        }
      }}
      destroyOnHidden
      forceRender
    >
      <Form
        form={form}
        layout="vertical"
        onFinish={async (values) => {
          await onSubmit(values);
          form.resetFields();
        }}
      >
        <Form.Item
          label="Title"
          name="title"
          rules={[
            { required: true, message: 'Please enter a title' },
            { max: 255, message: 'The title must be 255 characters or fewer' },
          ]}
        >
          <Input placeholder="Quarterly planning notes" size="large" />
        </Form.Item>

        <Form.Item label="Collaborators" name="collaboratorIds">
          <UserMultiSelect excludedUserIds={excludedUserIds} seedUsers={seedUsers} />
        </Form.Item>
      </Form>
    </Modal>
  );
};
