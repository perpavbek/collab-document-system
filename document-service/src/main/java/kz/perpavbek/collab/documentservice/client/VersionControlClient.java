package kz.perpavbek.collab.documentservice.client;

import kz.perpavbek.collab.documentservice.dto.client.OperationRequest;
import kz.perpavbek.collab.documentservice.dto.client.OperationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@FeignClient("version-control-service")
public interface VersionControlClient {
    @PostMapping("/internal/documents/operations")
    OperationResponse saveOperation(@RequestBody OperationRequest operation);

    @DeleteMapping("/internal/documents/{documentId}/history")
    void deleteDocumentVersions(@PathVariable UUID documentId);
}
