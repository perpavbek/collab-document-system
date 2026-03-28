package kz.perpavbek.collab.userservice.dto.response;
import kz.perpavbek.collab.userservice.enums.Role;
import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private UUID id;
    private String email;
    private String name;
    private Role role;
}
