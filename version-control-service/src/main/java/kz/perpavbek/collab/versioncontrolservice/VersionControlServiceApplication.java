package kz.perpavbek.collab.versioncontrolservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class VersionControlServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(VersionControlServiceApplication.class, args);
    }

}
