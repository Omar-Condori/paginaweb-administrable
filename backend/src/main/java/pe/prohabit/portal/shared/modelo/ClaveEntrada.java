package pe.prohabit.portal.shared.modelo;

import jakarta.validation.constraints.*;

public record ClaveEntrada(@NotBlank String actual, @NotBlank @Size(min=12,max=72) String nueva) {}