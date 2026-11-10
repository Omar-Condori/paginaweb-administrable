package pe.prohabit.portal.auth;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.prohabit.portal.shared.modelo.Usuario;
import pe.prohabit.portal.admin.usuario.UsuariosServicio;
import java.util.Map;

@RestController
@RequestMapping("/api/sesion")
public class SesionControlador {

    private final UsuariosServicio usuarios;

    public SesionControlador(UsuariosServicio usuarios) {
        this.usuarios = usuarios;
    }

    @GetMapping
    public Usuario sesion(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return null;
        return usuarios.porEmail(userDetails.getUsername());
    }

    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken token) {
        if (token == null) return Map.of();
        return Map.of("token", token.getToken(), "headerName", token.getHeaderName(), "parameterName", token.getParameterName());
    }
}