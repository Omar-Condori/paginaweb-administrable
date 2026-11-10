package pe.prohabit.portal.admin.usuario;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import pe.prohabit.portal.shared.modelo.*;

@RestController
@RequestMapping("/api/admin/usuarios")
public class UsuarioControlador {
    private final UsuariosServicio usuarios;
    public UsuarioControlador(UsuariosServicio usuarios) { this.usuarios = usuarios; }

    @GetMapping
    public List<Usuario> listar() { return usuarios.listar(); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Usuario crear(@Valid @RequestBody UsuarioEntrada u) { return usuarios.guardar(null, u); }

    @PutMapping("/{id}")
    public Usuario editar(@PathVariable long id, @Valid @RequestBody UsuarioEntrada u,
                          @AuthenticationPrincipal UserDetails actual) {
        return usuarios.guardar(id, u, actual.getUsername());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void borrar(@PathVariable long id, @RequestParam long version,
                       @AuthenticationPrincipal UserDetails actual) {
        usuarios.eliminar(id, version, actual.getUsername());
    }
}
