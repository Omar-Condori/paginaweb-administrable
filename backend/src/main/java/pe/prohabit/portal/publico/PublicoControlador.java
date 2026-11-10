package pe.prohabit.portal.publico;

import java.util.*;
import org.springframework.web.bind.annotation.*;
import pe.prohabit.portal.shared.*;

@RestController
@RequestMapping("/api/publico")
public class PublicoControlador {
    private final ContenidoRepositorio repo;
    public PublicoControlador(ContenidoRepositorio r) { this.repo = r; }

    @GetMapping("/sitio")
    public Map<String, Object> sitio() {
        var configuracion = repo.configuracion();
        var secciones = repo.secciones();
        var contenidos = repo.listar(true);
        return Map.of("configuracion", configuracion, "secciones", secciones, "contenidos", contenidos);
    }

    @GetMapping("/salud")
    public Map<String, String> salud() {
        return Map.of("estado", "ok");
    }
}