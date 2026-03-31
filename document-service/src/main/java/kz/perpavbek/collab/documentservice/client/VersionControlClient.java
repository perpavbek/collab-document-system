package kz.perpavbek.collab.documentservice.client;

import kz.perpavbek.collab.documentservice.dto.client.OperationRequest;
import kz.perpavbek.collab.documentservice.dto.client.OperationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("version-control-service")
public interface VersionControlClient {
    @PostMapping("/documents/operations")
    OperationResponse saveOperation(@RequestBody OperationRequest operation);
}
