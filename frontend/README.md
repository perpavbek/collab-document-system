# Collab Document System Frontend

React + TypeScript frontend for the collaborative editing system API.

## Stack

- React
- React Router
- Axios
- Ant Design
- Tailwind CSS
- Zustand
- WebSocket + STOMP
- CodeMirror (open-source text editor)
- Vite

## Features

- User registration and login
- JWT-based authenticated routing
- Documents list with pagination
- Document creation with collaborator search
- Document settings update and deletion
- Full document loading from the version service
- Live edit operation sending through `/documents/edit`
- STOMP subscription to `/topic/document/{documentId}`
- Active session display
- Recent operation feed for user contribution visibility
- Periodic fallback synchronization for missed operations

## Environment

Create a `.env` file based on `.env.example`.

```env
VITE_API_BASE_URL=http://localhost:8080
VITE_WS_BASE_URL=ws://localhost:8080
```

Expected gateway routes:

- `/users/*`
- `/documents/*`
- `/versions/*`
- WebSocket endpoint: `/documents/ws`

## Run

```bash
npm install
npm run dev
```

Production build:

```bash
npm run build
```

## Notes

- The editor uses CodeMirror because it is open source, reliable, and exposes enough low-level change information to map local edits into your `INSERT` and `DELETE` API operations.
- Browser WebSocket connections cannot attach arbitrary HTTP headers during the initial handshake. This frontend sends the JWT in STOMP `connectHeaders` as `Authorization: Bearer <token>`. That works when Spring security reads auth from STOMP headers or a channel interceptor.
- Active sessions are refreshed with polling because the provided API exposes `GET /documents/{documentId}/sessions` but does not describe a dedicated realtime session stream.
