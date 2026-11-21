package pe.prohabit.portal.shared.modelo;

import java.util.*;
import jakarta.validation.constraints.*;

public record Contenido(Long id, Tipo tipo,
    @NotBlank @Size(max=160) String titulo, @NotNull @Size(max=240) String subtitulo,
    @NotNull @Size(max=6000) String descripcion, @NotNull @Size(max=300) String imagen,
    @NotNull @Size(max=300) String imagenSecundaria, @NotNull @Size(max=160) String contacto,
    @NotNull @Size(max=100) String cargo, @Min(1) @Max(5) int calificacion,
    @Min(0) @Max(10000) int orden, boolean activo, @Min(0) long version,
    /** Solo proyectos: "", EN_EJECUCION, TERMINADO o PROXIMAMENTE. */
    @Pattern(regexp = "^(|EN_EJECUCION|TERMINADO|PROXIMAMENTE)$") String estado,
    /** Solo proyectos: fecha ISO (AAAA-MM-DD) o vacío. */
    @Pattern(regexp = "^(\\d{4}-\\d{2}-\\d{2})?$") String fecha,
    /** Fotografías adicionales (la principal es {@code imagen}). */
    @Size(max=12) List<@NotNull @Size(max=300) String> galeria) {

    public Contenido {
        estado = estado == null ? "" : estado;
        fecha = fecha == null ? "" : fecha;
        galeria = galeria == null ? List.of() : galeria.stream().filter(Objects::nonNull).toList();
    }

    /** Constructor anterior (sin estado, fecha ni galería); se conserva por compatibilidad. */
    public Contenido(Long id, Tipo tipo, String titulo, String subtitulo, String descripcion, String imagen,
                     String imagenSecundaria, String contacto, String cargo, int calificacion,
                     int orden, boolean activo, long version) {
        this(id, tipo, titulo, subtitulo, descripcion, imagen, imagenSecundaria, contacto, cargo,
             calificacion, orden, activo, version, "", "", List.of());
    }

    public Contenido conGaleria(List<String> nueva) {
        return new Contenido(id, tipo, titulo, subtitulo, descripcion, imagen, imagenSecundaria, contacto, cargo,
            calificacion, orden, activo, version, estado, fecha, nueva);
    }
}
