package pe.prohabit.portal.publico;

import java.nio.file.Path;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import pe.prohabit.portal.shared.Errores;

@RestController
@RequestMapping("/media")
public class MediaControlador {
    private final Path raiz;
    private final JdbcTemplate db;

    public MediaControlador(@Value("${app.uploads}") String ruta, JdbcTemplate db) {
        this.raiz = Path.of(ruta).toAbsolutePath().normalize();
        this.db = db;
    }

    @GetMapping("/{nombre}")
    public ResponseEntity<Resource> ver(@PathVariable String nombre) {
        if (!nombre.matches("(?i)[^/\\\\\\p{Cntrl}]{1,240}\\.(png|jpg)") || nombre.contains(".."))
            throw Errores.noExiste("Imagen");

        var tipos = db.query("SELECT mime FROM archivos WHERE nombre=?",
            (r, n) -> r.getString("mime"), nombre);
        var recurso = new FileSystemResource(raiz.resolve(nombre));
        if (tipos.isEmpty() || !recurso.isReadable())
            throw Errores.noExiste("Imagen");

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(tipos.getFirst()))
            .header("X-Content-Type-Options", "nosniff")
            .cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic())
            .body(recurso);
    }
}
