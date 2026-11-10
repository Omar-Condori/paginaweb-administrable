package pe.prohabit.portal.admin;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import pe.prohabit.portal.shared.modelo.Seccion;
import pe.prohabit.portal.shared.modelo.Tipo;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.prohabit.portal.shared.ContenidoRepositorio;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminControlador {
    private final ContenidoRepositorio repo;

    public AdminControlador(ContenidoRepositorio repo) {
        this.repo = repo;
    }

    @PutMapping("/secciones/{tipo}")
    public void editarSeccion(@PathVariable Tipo tipo, @Valid @RequestBody Seccion seccion) {
        repo.editarSeccion(tipo, seccion);
    }

    @GetMapping("/sitio")
    public Map<String, Object> sitio() {
        return Map.of(
            "configuracion", repo.configuracion(),
            "secciones", repo.secciones(),
            "contenidos", repo.listar(false)
        );
    }
}
