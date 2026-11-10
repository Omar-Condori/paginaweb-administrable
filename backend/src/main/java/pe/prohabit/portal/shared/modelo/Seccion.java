package pe.prohabit.portal.shared.modelo;

import jakarta.validation.constraints.*;

public record Seccion(Tipo codigo, @NotBlank @Size(max=160) String titulo,
    @NotNull @Size(max=240) String subtitulo, boolean activo, @Min(0) long version) {}