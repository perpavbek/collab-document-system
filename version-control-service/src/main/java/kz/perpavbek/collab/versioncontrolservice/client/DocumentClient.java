package kz.perpavbek.collab.versioncontrolservice.client;

import kz.perpavbek.collab.versioncontrolservice.dto.client.PermissionResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "document-service")
public interface DocumentClient {
    @GetMapping("/id/{documentId}/permission")
    PermissionResponse getPermission(@PathVariable UUID documentId);
}
