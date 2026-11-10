package pe.prohabit.portal;

import org.junit.jupiter.api.Test;
import pe.prohabit.portal.admin.contenido.ContenidoServicio;
import pe.prohabit.portal.shared.ContenidoRepositorio;
import pe.prohabit.portal.shared.modelo.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class TarjetasNosotrosTest {
    Contenido tarjeta(Tipo tipo, String descripcion) {
        return new Contenido(null, tipo, "Misión", "", descripcion, "", "", "", "", 5, 0, true, 0);
    }

    @Test void permiteCrearTarjetasSinFoto() {
        var repo = mock(ContenidoRepositorio.class);
        var c = tarjeta(Tipo.NOSOTROS_VALORES, "Construimos hogares para nuestras familias.");
        when(repo.crear(c)).thenReturn(42L);
        when(repo.obtener(42L)).thenReturn(c);
        assertEquals(c, new ContenidoServicio(repo).guardar(null, c));
        verify(repo).crear(c);
        verify(repo, never()).listar(false);
    }

    @Test void lasOtrasSeccionesSiguenExigiendoFotografia() {
        var repo = mock(ContenidoRepositorio.class);
        assertThrows(RuntimeException.class, () -> new ContenidoServicio(repo)
            .guardar(null, tarjeta(Tipo.PROYECTO, "Descripción")));
        verify(repo, never()).crear(any());
    }

    @Test void rechazaTarjetasConDescripcionVacia() {
        var repo = mock(ContenidoRepositorio.class);
        assertThrows(RuntimeException.class, () -> new ContenidoServicio(repo)
            .guardar(null, tarjeta(Tipo.NOSOTROS_VALORES, "   ")));
        verify(repo, never()).crear(any());
    }
}
