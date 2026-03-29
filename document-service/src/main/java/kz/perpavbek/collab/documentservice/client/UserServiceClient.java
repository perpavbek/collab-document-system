package kz.perpavbek.collab.documentservice.client;

import kz.perpavbek.collab.documentservice.dto.client.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "user-service")
public interface UserServiceClient {

    @GetMapping("/id/{id}")
    User getUserById(@PathVariable("id") UUID id);

    @GetMapping("/me")
    User getUserByToken();
}