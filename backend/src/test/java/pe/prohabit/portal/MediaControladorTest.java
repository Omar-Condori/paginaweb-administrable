package pe.prohabit.portal;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import pe.prohabit.portal.publico.MediaControlador;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MediaControladorTest {
    private static final String NOMBRE = "00d3ead7-747e-4b41-ad74-d2ceff3d9126.jpg";
    @TempDir Path carpeta;
    JdbcTemplate db;
    MockMvc web;

    @BeforeEach void preparar() {
        db = mock(JdbcTemplate.class);
        web = MockMvcBuilders.standaloneSetup(new MediaControlador(carpeta.toString(), db)).build();
    }

    @Test void entregaArchivoExistenteDesdeLaDireccionGuardadaSinSesion() throws Exception {
        byte[] foto = {1, 2, 3, 4};
        Files.write(carpeta.resolve(NOMBRE), foto);
        when(db.query(anyString(), org.mockito.ArgumentMatchers.<RowMapper<String>>any(), eq(NOMBRE)))
            .thenReturn(List.of("image/jpeg"));
        web.perform(get("/media/" + NOMBRE))
            .andExpect(status().isOk())
            .andExpect(content().contentType("image/jpeg"))
            .andExpect(content().bytes(foto))
            .andExpect(header().string("X-Content-Type-Options", "nosniff"));
    }

    @Test void entregaFotografiaConNombreAntiguoRegistrado() throws Exception {
        Files.write(carpeta.resolve("calle.jpg"), new byte[]{1, 2});
        when(db.query(anyString(), org.mockito.ArgumentMatchers.<RowMapper<String>>any(), eq("calle.jpg")))
            .thenReturn(List.of("image/jpeg"));
        web.perform(get("/media/calle.jpg")).andExpect(status().isOk())
            .andExpect(content().bytes(new byte[]{1, 2}));
    }

    @Test void archivoNoRegistradoDevuelve404() throws Exception {
        Files.write(carpeta.resolve(NOMBRE), new byte[]{1});
        when(db.query(anyString(), org.mockito.ArgumentMatchers.<RowMapper<String>>any(), eq(NOMBRE)))
            .thenReturn(List.of());
        web.perform(get("/media/" + NOMBRE)).andExpect(status().isNotFound());
    }

    @Test void archivoRegistradoPeroAusenteDelDiscoDevuelve404() throws Exception {
        when(db.query(anyString(), org.mockito.ArgumentMatchers.<RowMapper<String>>any(), eq(NOMBRE)))
            .thenReturn(List.of("image/jpeg"));
        web.perform(get("/media/" + NOMBRE)).andExpect(status().isNotFound());
    }

    @Test void rechazaNombresAjenosALasImagenesSubidas() throws Exception {
        web.perform(get("/media/application.yml")).andExpect(status().isNotFound());
        verifyNoInteractions(db);
    }
}
