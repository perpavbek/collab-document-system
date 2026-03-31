package kz.perpavbek.collab.documentservice.websocket;

import kz.perpavbek.collab.documentservice.security.JwtUtils;
import kz.perpavbek.collab.documentservice.service.DocumentSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.util.Objects;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final DocumentSessionService sessionService;

    @EventListener
    public void handleSubscribe(SessionSubscribeEvent event) {

        StompHeaderAccessor accessor =
                StompHeaderAccessor.wrap(event.getMessage());

        String destination = accessor.getDestination();

        if (destination != null && destination.startsWith("/topic/document/")) {

            UUID documentId = UUID.fromString(
                    destination.replace("/topic/document/", "")
            );

            Object principal = accessor.getUser() instanceof UsernamePasswordAuthenticationToken auth
                    ? auth.getPrincipal()
                    : null;

            UUID userId = null;
            if (principal instanceof UUID id) {
                userId = id;
            } else if (principal instanceof String s) {
                userId = UUID.fromString(s);
            }
            if (userId == null) return;

            sessionService.connect(
                    documentId,
                    userId,
                    accessor.getSessionId()
            );
        }
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {

        String sessionId = event.getSessionId();

        sessionService.disconnect(sessionId);
    }
}
