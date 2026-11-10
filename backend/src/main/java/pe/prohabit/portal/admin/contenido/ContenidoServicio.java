package pe.prohabit.portal.admin.contenido;

import java.net.URI;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.prohabit.portal.shared.modelo.*;
import pe.prohabit.portal.shared.ContenidoRepositorio;
import pe.prohabit.portal.shared.Errores;

@Service
public class ContenidoServicio {
    private final ContenidoRepositorio r;
    public ContenidoServicio(ContenidoRepositorio r) { this.r = r; }

    public Map<String, Object> sitio(boolean publico) {
        var secciones = r.secciones();
        var visibles = secciones.stream().filter(Seccion::activo).map(Seccion::codigo).toList();
        var contenidos = r.listar(publico).stream().filter(c -> !publico || c.tipo() == Tipo.NOSOTROS || visibles.contains(c.tipo())).toList();
        return Map.of("configuracion", r.configuracion(), "secciones", secciones, "contenidos", contenidos);
    }

    @Transactional
    public Contenido guardar(Long id, Contenido c) {
        r.bloquear();
        boolean tarjeta = c.tipo() == Tipo.NOSOTROS_VALORES;
        if (tarjeta && c.descripcion().isBlank()) throw Errores.invalido("La descripción de la tarjeta es obligatoria");
        imagen(c.imagen(), !tarjeta);
        imagen(c.imagenSecundaria(), c.tipo() == Tipo.CASA_MODELO);
        if (c.tipo() == Tipo.VENTAS && (c.contacto().isBlank() || c.cargo().isBlank())) throw Errores.invalido("Ventas requiere contacto y rol");
        if (c.tipo() == Tipo.CASA_MODELO || c.tipo() == Tipo.NOSOTROS) {
            long total = r.listar(false).stream().filter(v -> v.tipo() == c.tipo() && !Objects.equals(v.id(), id)).count();
            if (total > 0) throw Errores.invalido("Esta sección admite un solo contenido. Edita el existente.");
        }
        if (id == null) return r.obtener(r.crear(c));
        r.editar(id, c);
        return r.obtener(id);
    }

    public void imagen(String url, boolean requerida) {
        if (url == null || url.isBlank()) {
            if (requerida) throw Errores.invalido("La imagen es obligatoria");
            return;
        }
        if (url.matches("/ejemplos/[a-z0-9-]+\\.svg")) return;
        if (!url.startsWith("/media/")) throw Errores.invalido("Selecciona una imagen de la biblioteca");
        if (!r.existeArchivo(url.substring(7))) throw Errores.invalido("La imagen seleccionada no existe");
    }

    @Transactional
    public void eliminar(long id, long version) {
        r.obtener(id);
        r.eliminar(id, version);
    }

    public Configuracion configurar(Configuracion c) {
        url(c.facebook(), false);
        url(c.instagram(), false);
        url(c.mapa(), true);
        r.configurar(c);
        return r.configuracion();
    }

    private void url(String valor, boolean mapa) {
        if (valor.isBlank()) return;
        try {
            var u = URI.create(valor);
            if (!"https".equals(u.getScheme()) || u.getHost() == null || u.getUserInfo() != null) throw new IllegalArgumentException();
            if (mapa && !(Set.of("www.google.com", "maps.google.com", "www.google.com.pe").contains(u.getHost()) && (u.getPath().startsWith("/maps") || u.getPath().equals("/")))) throw new IllegalArgumentException();
        } catch (IllegalArgumentException e) { throw Errores.invalido(mapa ? "Usa una URL HTTPS de Google Maps (para insertar: /maps/embed...)" : "Los enlaces deben comenzar con https://"); }
    }

    public void actualizarOrden(long id, int orden) {
        r.actualizarOrden(id, orden);
    }
}