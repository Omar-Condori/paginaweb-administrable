package pe.prohabit.portal.admin.contenido;

import java.util.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import pe.prohabit.portal.shared.modelo.*;

@RestController
@RequestMapping("/api/admin/contenidos")
public class ContenidoControlador {
    private final ContenidoServicio servicio;
    public ContenidoControlador(ContenidoServicio s) { this.servicio = s; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Contenido crear(@Valid @RequestBody Contenido c) { return servicio.guardar(null, c); }

    @PutMapping("/{id}")
    public Contenido editar(@PathVariable long id, @Valid @RequestBody Contenido c) { return servicio.guardar(id, c); }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable long id, @RequestParam long version) { servicio.eliminar(id, version); }

    @PutMapping("/reordenar")
    public void reordenar(@RequestBody Map<String, Integer> ordenes) {
        for (var e : ordenes.entrySet()) {
            long id = Long.parseLong(e.getKey());
            int orden = e.getValue();
            servicio.actualizarOrden(id, orden);
        }
    }
}