package pe.prohabit.portal.shared.modelo;

import jakarta.validation.constraints.*;

public record UsuarioEntrada(@NotBlank @Size(max=100) String nombre,
    @NotBlank @jakarta.validation.constraints.Email @Size(max=180) String email, @NotNull @Size(max=72) String clave,
    @NotNull Rol rol, boolean activo, @Min(0) long version) {}