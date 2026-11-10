package pe.prohabit.portal;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.session.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.List;

import pe.prohabit.portal.admin.usuario.UsuariosServicio;

@Configuration
@EnableWebSecurity
public class SeguridadConfig {
    @Bean static PasswordEncoder passwordEncoder() {return new BCryptPasswordEncoder(12);}
    @Bean static SessionRegistry sessionRegistry() {return new SessionRegistryImpl();}
    @Bean static HttpSessionEventPublisher sessionPublisher() {return new HttpSessionEventPublisher();}
    
    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:4200"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean SecurityFilterChain filtros(HttpSecurity http,UsuariosServicio usuarios,PasswordEncoder encoder,SessionRegistry registro) throws Exception {
        var provider=new DaoAuthenticationProvider(usuarios); provider.setPasswordEncoder(encoder);
        http.authenticationProvider(provider)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(a->a
                .requestMatchers(HttpMethod.GET,"/api/publico/**","/media/**","/api/sesion/csrf").permitAll()
                .requestMatchers("/api/auth/**","/error").permitAll()
                .requestMatchers("/api/admin/usuarios/**").hasRole("ADMINISTRADOR")
                .requestMatchers("/api/admin/**").hasAnyRole("ADMINISTRADOR","OPERADOR")
                .requestMatchers("/api/sesion/**","/api/sesion").authenticated()
                .anyRequest().denyAll())
            .formLogin(f->f.loginProcessingUrl("/api/auth/login").usernameParameter("email").passwordParameter("clave")
                .successHandler((q,s,a)->s.setStatus(204))
                .failureHandler((q,s,e)->json(s,401,"Correo o contraseña incorrectos")))
            .logout(l->l.logoutUrl("/api/auth/logout").deleteCookies("JSESSIONID").logoutSuccessHandler((q,s,a)->s.setStatus(204)))
            .exceptionHandling(e->e.authenticationEntryPoint((q,s,x)->json(s,401,"Inicia sesión para continuar"))
                .accessDeniedHandler((q,s,x)->json(s,403,"No tienes permisos o la sesión de seguridad ha caducado. Recarga e inicia sesión.")))
            .sessionManagement(s->s.maximumSessions(3).sessionRegistry(registro).expiredSessionStrategy(e->json(e.getResponse(),401,"Tu sesión ha caducado. Inicia sesión nuevamente.")))
            .addFilterBefore(new IntentosLogin(),UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
    static void json(HttpServletResponse r,int estado,String mensaje) throws IOException {
        r.setStatus(estado);r.setContentType("application/json;charset=UTF-8");r.getWriter().write("{\"mensaje\":\""+mensaje+"\"}");
    }
    static class IntentosLogin extends OncePerRequestFilter {
        record Ventana(long inicio,int cantidad) {}
        private final ConcurrentHashMap<String,Ventana> intentos=new ConcurrentHashMap<>();
        @Override protected void doFilterInternal(HttpServletRequest q,HttpServletResponse s,FilterChain cadena) throws IOException,ServletException {
            if(q.getMethod().equals("POST")&&q.getServletPath().equals("/api/auth/login")) {
                long ahora=System.currentTimeMillis();
                intentos.entrySet().removeIf(e->ahora-e.getValue().inicio()>300000);
                var v=intentos.compute(q.getRemoteAddr(),(k,anterior)->anterior==null?new Ventana(ahora,1):new Ventana(anterior.inicio(),anterior.cantidad()+1));
                if(v.cantidad()>20) {s.setHeader("Retry-After","300");json(s,429,"Demasiados intentos. Inténtalo en cinco minutos.");return;}
            }
            cadena.doFilter(q,s);
        }
    }
}