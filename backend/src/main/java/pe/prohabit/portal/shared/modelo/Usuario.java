package pe.prohabit.portal.shared.modelo;

public record Usuario(long id, String nombre, String email, Rol rol, boolean activo, long version) {}