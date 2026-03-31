package kz.perpavbek.collab.versioncontrolservice.service;

import feign.FeignException;
import kz.perpavbek.collab.versioncontrolservice.client.DocumentClient;
import kz.perpavbek.collab.versioncontrolservice.dto.client.PermissionResponse;
import kz.perpavbek.collab.versioncontrolservice.enums.CollaboratorRole;
import kz.perpavbek.collab.versioncontrolservice.exception.AccessDeniedException;
import kz.perpavbek.collab.versioncontrolservice.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccessControlServiceTest {

    @Mock
    private DocumentClient documentClient;

    @InjectMocks
    private AccessControlService accessService;

    @Test
    void shouldAllowAccess() {

        UUID docId = UUID.randomUUID();

        PermissionResponse permission = new PermissionResponse();
        permission.setRole(CollaboratorRole.EDITOR);

        when(documentClient.getPermission(docId)).thenReturn(permission);

        assertDoesNotThrow(() ->
                accessService.checkAccess(docId, CollaboratorRole.EDITOR));
    }

    @Test
    void shouldThrowAccessDenied() {

        UUID docId = UUID.randomUUID();

        PermissionResponse permission = new PermissionResponse();
        permission.setRole(CollaboratorRole.VIEWER);

        when(documentClient.getPermission(docId)).thenReturn(permission);

        assertThrows(
                AccessDeniedException.class,
                () -> accessService.checkAccess(docId, CollaboratorRole.EDITOR)
        );
    }

    @Test
    void shouldThrowNotFound() {

        UUID docId = UUID.randomUUID();

        when(documentClient.getPermission(docId))
                .thenThrow(mock(FeignException.NotFound.class));

        assertThrows(
                NotFoundException.class,
                () -> accessService.checkAccess(docId, CollaboratorRole.EDITOR)
        );
    }
}
