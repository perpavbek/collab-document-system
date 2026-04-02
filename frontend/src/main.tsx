import ReactDOM from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import { App as AntApp, ConfigProvider } from 'antd';

import App from './App';
import './index.css';
import 'antd/dist/reset.css';

ReactDOM.createRoot(document.getElementById('root')!).render(
  <ConfigProvider
    theme={{
      token: {
        colorPrimary: '#0f766e',
        colorInfo: '#0f766e',
        colorSuccess: '#0f766e',
        borderRadius: 16,
        colorTextBase: '#10231b',
        colorBgLayout: '#f4f7f1',
        colorLink: '#0f766e',
        fontFamily:
          '"Segoe UI", ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, "Helvetica Neue", sans-serif',
      },
    }}
  >
    <AntApp>
      <BrowserRouter
        future={{
          v7_relativeSplatPath: true,
          v7_startTransition: true,
        }}
      >
        <App />
      </BrowserRouter>
    </AntApp>
  </ConfigProvider>,
);
