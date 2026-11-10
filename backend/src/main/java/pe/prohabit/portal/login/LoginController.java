package pe.prohabit.portal.login;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.prohabit.portal.shared.modelo.Rol;

@RestController
@RequestMapping("/api/auth")
public class LoginController {

    private final AuthenticationManager authenticationManager;
    private final LoginService loginService;

    public LoginController(AuthenticationManager authenticationManager, LoginService loginService) {
        this.authenticationManager = authenticationManager;
        this.loginService = loginService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email(), request.clave())
        );

        SecurityContextHolder.getContext().setAuthentication(auth);

        var user = (org.springframework.security.core.userdetails.User) auth.getPrincipal();
        var roles = user.getAuthorities().stream()
            .map(a -> a.getAuthority())
            .toList();

        return ResponseEntity.ok(new LoginResponse(
            null, // Token JWT si se implementa
            null, // ID del usuario
            user.getUsername(),
            user.getUsername(),
            roles.contains("ROLE_ADMINISTRADOR") ? Rol.ADMINISTRADOR : Rol.OPERADOR
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.noContent().build();
    }
}