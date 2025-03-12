package sample.bpm.be.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/secure")
public class SecureController {// ✅ Secured Endpoint (Accessible by any authenticated user)
    @GetMapping("/user")
    public String userAccess() {
        return "Hello, User! You have access to this secured endpoint.";
    }

    // ✅ Admin-only Endpoint (Only accessible by ADMIN users)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public String adminAccess() {
        return "Hello, Admin! You have access to this restricted endpoint.";
    }

}
