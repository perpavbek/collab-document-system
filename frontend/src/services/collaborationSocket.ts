import { Client, type StompSubscription } from '@stomp/stompjs';

import { env } from '../config/env';
import { tokenStorage } from '../lib/storage';
import type { RealtimeOperationMessage } from '../types/operations';

const DOCUMENT_SOCKET_PATH = '/documents/ws';
const SOCKET_ENDPOINT_PATTERN = /\/documents\/ws(?:\/websocket)?$/i;

const resolveBrokerUrl = (baseUrl: string) =>
  SOCKET_ENDPOINT_PATTERN.test(baseUrl) ? baseUrl : `${baseUrl}${DOCUMENT_SOCKET_PATH}`;

class CollaborationSocket {
  private client: Client | null = null;
  private subscription: StompSubscription | null = null;

  connect(
    documentId: string,
    onOperation: (operation: RealtimeOperationMessage) => void,
    onError?: (message: string) => void,
    onConnected?: () => void,
  ) {
    this.disconnect();

    const token = tokenStorage.get();

    this.client = new Client({
      brokerURL: resolveBrokerUrl(env.wsBaseUrl),
      connectHeaders: token
        ? {
            Authorization: `Bearer ${token}`,
          }
        : {},
      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      debug: () => undefined,
    });

    this.client.onConnect = () => {
      onConnected?.();
      this.subscription = this.client?.subscribe(`/topic/document/${documentId}`, (message) => {
        try {
          onOperation(JSON.parse(message.body) as RealtimeOperationMessage);
        } catch {
          onError?.('Received an unreadable WebSocket payload.');
        }
      }) ?? null;
    };

    this.client.onStompError = (frame) => {
      onError?.(frame.headers.message ?? 'The STOMP broker returned an error.');
    };

    this.client.onWebSocketError = () => {
      onError?.('WebSocket connection failed.');
    };

    this.client.activate();
  }

  disconnect() {
    this.subscription?.unsubscribe();
    this.subscription = null;

    if (this.client) {
      void this.client.deactivate();
      this.client = null;
    }
  }

  isConnected() {
    return this.client?.connected ?? false;
  }
}

export const collaborationSocket = new CollaborationSocket();
