package pe.prohabit.portal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import pe.prohabit.portal.admin.AdminControlador;
import pe.prohabit.portal.shared.ContenidoRepositorio;
import pe.prohabit.portal.shared.Errores;
import pe.prohabit.portal.shared.modelo.Seccion;
import pe.prohabit.portal.shared.modelo.Tipo;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SeccionesControladorTest {
    ContenidoRepositorio repo;
    MockMvc web;

    @BeforeEach void preparar() {
        repo = mock(ContenidoRepositorio.class);
        web = MockMvcBuilders.standaloneSetup(new AdminControlador(repo)).build();
    }

    @ParameterizedTest @EnumSource(Tipo.class)
    void permiteEditarEncabezadosDeTodasLasSecciones(Tipo tipo) throws Exception {
        String cuerpo = """
            {"codigo":"%s","titulo":"Nuevo título","subtitulo":"Nuevo subtítulo","activo":true,"version":3}
            """.formatted(tipo);
        web.perform(put("/api/admin/secciones/" + tipo).contentType("application/json").content(cuerpo))
            .andExpect(status().isOk());
        verify(repo).editarSeccion(tipo, new Seccion(tipo, "Nuevo título", "Nuevo subtítulo", true, 3));
    }

    @Test void rechazaTituloVacio() throws Exception {
        web.perform(put("/api/admin/secciones/TESTIMONIO").contentType("application/json")
            .content("{\"codigo\":\"TESTIMONIO\",\"titulo\":\"\",\"subtitulo\":\"Clientes\",\"activo\":true,\"version\":0}"))
            .andExpect(status().isBadRequest());
        verifyNoInteractions(repo);
    }

    @Test void informaConflictoEnVezDeConfirmarUnCambioNoGuardado() throws Exception {
        doThrow(Errores.conflicto()).when(repo).editarSeccion(eq(Tipo.TESTIMONIO), any());
        web.perform(put("/api/admin/secciones/TESTIMONIO").contentType("application/json")
            .content("{\"codigo\":\"TESTIMONIO\",\"titulo\":\"Clientes\",\"subtitulo\":\"Opiniones\",\"activo\":true,\"version\":0}"))
            .andExpect(status().isConflict());
    }
}
