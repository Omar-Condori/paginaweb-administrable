package pe.prohabit.portal.login;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class LoginService {

    private final UserDetailsService userDetailsService;

    public LoginService(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    public UserDetails autenticar(String email, String clave) {
        UserDetails user = userDetailsService.loadUserByUsername(email);
        // La validación de contraseña la hace Spring Security internamente
        return user;
    }

    public Authentication crearAutenticacion(UserDetails user) {
        return new UsernamePasswordAuthenticationToken(
            user, null, user.getAuthorities()
        );
    }

    public void establecerContexto(Authentication auth) {
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}