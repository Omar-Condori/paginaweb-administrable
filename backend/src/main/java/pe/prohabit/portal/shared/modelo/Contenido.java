package pe.prohabit.portal.shared.modelo;

import jakarta.validation.constraints.*;

public record Contenido(Long id, Tipo tipo,
    @NotBlank @Size(max=160) String titulo, @NotNull @Size(max=240) String subtitulo,
    @NotNull @Size(max=6000) String descripcion, @NotNull @Size(max=300) String imagen,
    @NotNull @Size(max=300) String imagenSecundaria, @NotNull @Size(max=160) String contacto,
    @NotNull @Size(max=100) String cargo, @Min(1) @Max(5) int calificacion,
    @Min(0) @Max(10000) int orden, boolean activo, @Min(0) long version) {}