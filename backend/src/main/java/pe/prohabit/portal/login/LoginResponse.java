package pe.prohabit.portal.login;

import pe.prohabit.portal.shared.modelo.Rol;

public record LoginResponse(
    String token,
    Long id,
    String nombre,
    String email,
    Rol rol
) {}