package pe.prohabit.portal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties={
 "spring.datasource.url=jdbc:h2:mem:usuarios_auth;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
 "spring.datasource.username=sa", "spring.datasource.password=pruebas",
 "spring.flyway.enabled=false", "spring.sql.init.mode=always",
 "spring.sql.init.schema-locations=classpath:db/migration/V1__estructura.sql",
 "app.admin-email=admin@test.local", "app.admin-password=ClaveAdmin-Pruebas2026!", "app.seed-demo=false"
})
class UsuariosAutenticacionTest {
    @Autowired WebApplicationContext contexto;
    @Autowired FilterChainProxy seguridad;
    @Autowired JdbcTemplate db;

    @Test void operadorCreadoDesdeElPanelPuedeEntrarYSigueSinAdministrarUsuarios() throws Exception {
        MockMvc web = MockMvcBuilders.webAppContextSetup(contexto).addFilters(seguridad).build();
        var admin = (MockHttpSession) web.perform(post("/api/auth/login")
            .param("email", "admin@test.local").param("clave", "ClaveAdmin-Pruebas2026!"))
            .andExpect(status().isNoContent()).andReturn().getRequest().getSession(false);
        assertNotNull(admin);
        web.perform(post("/api/admin/usuarios").session(admin).contentType("application/json")
            .content(datos("Operador@Test.local", "ClaveOperador-2026!", true, 0)))
            .andExpect(status().isCreated());
        long id = db.queryForObject("SELECT id FROM usuarios WHERE email='operador@test.local'", Long.class);
        String hash = db.queryForObject("SELECT clave FROM usuarios WHERE id=?", String.class, id);
        assertTrue(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().matches("ClaveOperador-2026!", hash));
        web.perform(post("/api/auth/login").param("email", "operador@test.local").param("clave", "incorrecta"))
            .andExpect(status().isUnauthorized());
        var operador = (MockHttpSession) web.perform(post("/api/auth/login")
            .param("email", "OPERADOR@TEST.LOCAL").param("clave", "ClaveOperador-2026!"))
            .andExpect(status().isNoContent()).andReturn().getRequest().getSession(false);
        web.perform(get("/api/admin/sitio").session(operador)).andExpect(status().isOk());
        web.perform(get("/api/admin/usuarios").session(operador)).andExpect(status().isForbidden());
        web.perform(put("/api/admin/usuarios/" + id).session(admin).contentType("application/json")
            .content(datos("operador@test.local", "", true, 0))).andExpect(status().isOk());
        assertEquals(hash, db.queryForObject("SELECT clave FROM usuarios WHERE id=?", String.class, id));
        web.perform(get("/api/admin/sitio").session(operador)).andExpect(status().isUnauthorized());
        web.perform(put("/api/admin/usuarios/" + id).session(admin).contentType("application/json")
            .content(datos("operador@test.local", "NuevaClaveOperador2026!", true, 1))).andExpect(status().isOk());
        web.perform(post("/api/auth/login").param("email", "operador@test.local").param("clave", "ClaveOperador-2026!"))
            .andExpect(status().isUnauthorized());
        web.perform(post("/api/auth/login").param("email", "operador@test.local").param("clave", "NuevaClaveOperador2026!"))
            .andExpect(status().isNoContent());
        web.perform(put("/api/admin/usuarios/" + id).session(admin).contentType("application/json")
            .content(datos("operador@test.local", "", false, 2))).andExpect(status().isOk());
        web.perform(post("/api/auth/login").param("email", "operador@test.local").param("clave", "NuevaClaveOperador2026!"))
            .andExpect(status().isUnauthorized());
        long adminId = db.queryForObject("SELECT id FROM usuarios WHERE email='admin@test.local'", Long.class);
        web.perform(put("/api/admin/usuarios/" + adminId).session(admin).contentType("application/json")
            .content(datos("admin@test.local", "", true, 0))).andExpect(status().isBadRequest());
        web.perform(delete("/api/admin/usuarios/" + adminId).session(admin).param("version", "0"))
            .andExpect(status().isBadRequest());
        web.perform(delete("/api/admin/usuarios/" + id).session(admin).param("version", "3"))
            .andExpect(status().isNoContent());
    }

    private String datos(String email, String clave, boolean activo, long version) {
        return """
            {"nombre":"Operador de prueba","email":"%s","clave":"%s","rol":"OPERADOR","activo":%s,"version":%s}
            """.formatted(email, clave, activo, version);
    }
}
