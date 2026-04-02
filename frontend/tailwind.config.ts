import type { Config } from 'tailwindcss';

const config: Config = {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        canvas: '#f4f7f1',
        ink: '#10231b',
        accent: '#0f766e',
        sun: '#f59e0b',
      },
      boxShadow: {
        soft: '0 20px 60px rgba(16, 35, 27, 0.10)',
      },
      backgroundImage: {
        'mesh-paper':
          'radial-gradient(circle at top left, rgba(15, 118, 110, 0.18), transparent 35%), radial-gradient(circle at top right, rgba(245, 158, 11, 0.16), transparent 28%), linear-gradient(180deg, rgba(255,255,255,0.95), rgba(244,247,241,0.98))',
      },
    },
  },
  plugins: [],
};

export default config;
