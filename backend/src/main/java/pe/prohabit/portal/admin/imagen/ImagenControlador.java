package pe.prohabit.portal.admin.imagen;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.Locale;
import java.util.UUID;
import javax.imageio.ImageIO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pe.prohabit.portal.shared.modelo.*;
import pe.prohabit.portal.shared.ContenidoRepositorio;
import pe.prohabit.portal.shared.Errores;

@RestController
@RequestMapping("/api/admin/archivos")
public class ImagenControlador {
    private final Path raiz;
    private final ContenidoRepositorio repo;
    private final JdbcTemplate db;
    
    public ImagenControlador(@Value("${app.uploads}") String ruta, ContenidoRepositorio r, JdbcTemplate db) throws IOException {
        raiz = Path.of(ruta).toAbsolutePath().normalize();
        Files.createDirectories(raiz);
        this.repo = r;
        this.db = db;
    }

    @GetMapping
    public List<Archivo> listar() { return repo.listarArchivos(); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Archivo subir(@RequestParam("archivo") MultipartFile archivo) throws IOException {
        if (archivo.isEmpty() || archivo.getSize() > 8 * 1024 * 1024) 
            throw Errores.invalido("Elige una imagen JPG o PNG de hasta 8 MB");
        
        BufferedImage imagen;
        String formato;
        try (var input = ImageIO.createImageInputStream(archivo.getInputStream())) {
            var lectores = ImageIO.getImageReaders(input);
            if (!lectores.hasNext()) throw Errores.invalido("El archivo no es una imagen válida");
            var lector = lectores.next();
            try {
                formato = lector.getFormatName().toLowerCase(Locale.ROOT);
                if (!Set.of("png", "jpeg", "jpg").contains(formato)) 
                    throw Errores.invalido("Solo se admiten imágenes JPG y PNG");
                lector.setInput(input, true, true);
                int ancho = lector.getWidth(0), alto = lector.getHeight(0);
                if (ancho < 1 || alto < 1 || (long) ancho * alto > 16000000) 
                    throw Errores.invalido("La imagen debe tener como máximo 16 megapíxeles");
                imagen = lector.read(0);
            } finally { lector.dispose(); }
        }
        
        String extension = formato.equals("png") ? "png" : "jpg";
        String nombre = UUID.randomUUID() + "." + extension;
        Path destino = raiz.resolve(nombre);
        String original = Optional.ofNullable(archivo.getOriginalFilename()).orElse("imagen").replaceAll("[\\r\\n]", "");
        if (original.length() > 240) original = original.substring(original.length() - 240);
        
        try {
            if (!ImageIO.write(imagen, extension, destino.toFile())) 
                throw new IOException("No se pudo procesar la imagen");
            long bytes = Files.size(destino);
            String mime = "image/" + (extension.equals("jpg") ? "jpeg" : "png");
            var a = new Archivo(nombre, original, "/media/" + nombre, mime, bytes);
            repo.guardarArchivo(a);
            return a;
        } catch (Exception e) {
            Files.deleteIfExists(destino);
            throw e;
        }
    }

    @DeleteMapping("/{nombre}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @org.springframework.transaction.annotation.Transactional
    public void borrar(@PathVariable String nombre) throws IOException {
        db.queryForObject("SELECT id FROM configuracion WHERE id=1 FOR UPDATE", Integer.class);
        validar(nombre);
        String url = "/media/" + nombre;
        if (db.queryForObject("SELECT COUNT(*) FROM contenidos WHERE imagen=? OR imagen_secundaria=?", Integer.class, url, url) > 0) 
            throw Errores.invalido("Esta imagen está en uso. Reemplázala en el contenido antes de eliminarla.");
        if (db.queryForObject("SELECT COUNT(*) FROM archivos WHERE nombre=?", Integer.class, nombre) == 0) 
            throw Errores.noExiste("Imagen");
        Files.deleteIfExists(raiz.resolve(nombre));
        repo.borrarArchivo(nombre);
    }

    private void validar(String nombre) { 
        if (!nombre.matches("[a-f0-9-]{36}\\.(png|jpg)")) 
            throw Errores.noExiste("Imagen"); 
    }
}