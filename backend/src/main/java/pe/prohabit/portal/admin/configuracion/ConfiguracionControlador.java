package pe.prohabit.portal.admin.configuracion;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import pe.prohabit.portal.shared.modelo.*;
import pe.prohabit.portal.shared.ContenidoRepositorio;

@RestController
@RequestMapping("/api/admin/configuracion")
public class ConfiguracionControlador {
    private final ContenidoRepositorio repo;
    public ConfiguracionControlador(ContenidoRepositorio r) { this.repo = r; }

    @PutMapping
    public Configuracion actualizar(@Valid @RequestBody Configuracion c) { return repo.configurar(c); }
}