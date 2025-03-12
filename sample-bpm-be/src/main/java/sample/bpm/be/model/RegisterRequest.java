package sample.bpm.be.model;

import lombok.Getter;
import lombok.Setter;
import sample.bpm.be.entity.Role;

import java.util.Set;

@Getter
@Setter
public class RegisterRequest {
    private String username;
    private String password;
    private Set<Role> roles; // USER or ADMIN
}
