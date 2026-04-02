import { Button, Result } from 'antd';
import { useNavigate } from 'react-router-dom';

export const NotFoundPage = () => {
  const navigate = useNavigate();

  return (
    <div className="flex min-h-screen items-center justify-center px-4">
      <Result
        status="404"
        title="Page not found"
        subTitle="The page you requested does not exist in this frontend workspace."
        extra={
          <Button type="primary" onClick={() => navigate('/documents')}>
            Back to documents
          </Button>
        }
      />
    </div>
  );
};
