package pe.prohabit.portal.shared;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class Errores {
    public Errores() {}

    public static RuntimeException noExiste(String que) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, que + " no existe");
    }

    public static RuntimeException invalido(String msg) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, msg);
    }

    public static RuntimeException conflicto() {
        return new ResponseStatusException(HttpStatus.CONFLICT, "El recurso fue modificado por otro usuario. Recarga e inténtalo de nuevo.");
    }

    public static void validar(DataIntegrityViolationException e, String entidad) {
        String msg = e.getMostSpecificCause().getMessage().toLowerCase();
        if (msg.contains("unique") || msg.contains("duplicate")) throw invalido(entidad + " ya existe");
        if (msg.contains("foreign key")) throw invalido("No se puede eliminar: hay registros vinculados");
        throw invalido("Error de integridad: " + e.getMostSpecificCause().getMessage());
    }
}