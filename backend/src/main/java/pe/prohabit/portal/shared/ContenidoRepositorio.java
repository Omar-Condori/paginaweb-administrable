package pe.prohabit.portal.shared;

import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import pe.prohabit.portal.shared.modelo.*;

@Repository
public class ContenidoRepositorio {
    private final JdbcTemplate db;
    public ContenidoRepositorio(JdbcTemplate db) { this.db = db; }

    private Contenido fila(java.sql.ResultSet r, int n) throws java.sql.SQLException {
        return new Contenido(r.getLong("id"), Tipo.valueOf(r.getString("tipo")),
            r.getString("titulo"), r.getString("subtitulo"), r.getString("descripcion"),
            r.getString("imagen"), r.getString("imagen_secundaria"), r.getString("contacto"),
            r.getString("cargo"), r.getInt("calificacion"), r.getInt("orden"),
            r.getBoolean("activo"), r.getLong("version"));
    }

    private Usuario filaUsuario(java.sql.ResultSet r, int n) throws java.sql.SQLException {
        return new Usuario(r.getLong("id"), r.getString("nombre"), r.getString("email"),
            Rol.valueOf(r.getString("rol")), r.getBoolean("activo"), r.getLong("version"));
    }

    private Archivo filaArchivo(java.sql.ResultSet r, int n) throws java.sql.SQLException {
        return new Archivo(r.getString("nombre"), r.getString("original"), "/media/" + r.getString("nombre"), r.getString("mime"), r.getLong("bytes"));
    }

    public List<Contenido> listar(boolean publico) {
        String sql = "SELECT * FROM contenidos " + (publico ? "WHERE activo=TRUE " : "") + "ORDER BY tipo, orden, id";
        return db.query(sql, this::fila);
    }

    public List<Usuario> listarUsuarios() {
        return db.query("SELECT * FROM usuarios ORDER BY nombre, id", this::filaUsuario);
    }

    public List<Archivo> listarArchivos() {
        return db.query("SELECT * FROM archivos", this::filaArchivo);
    }

    public List<Seccion> secciones() {
        return db.query("SELECT * FROM secciones ORDER BY codigo", (r, n) ->
            new Seccion(Tipo.valueOf(r.getString("codigo")), r.getString("titulo"),
                r.getString("subtitulo"), r.getBoolean("activo"), r.getLong("version")));
    }

    public Configuracion configuracion() {
        return db.query("SELECT * FROM configuracion WHERE id=1", (r, n) ->
            new Configuracion(r.getString("nombre"), r.getString("lema"), r.getString("telefono"),
                r.getString("correo"), r.getString("direccion"), r.getString("horario"),
                r.getString("mapa"), r.getString("facebook"), r.getString("instagram"),
                r.getString("whatsapp"), r.getLong("version"))).stream().findFirst().orElse(
                    new Configuracion("PROHABIT", "Espacios para vivir", "", "", "", "", "", "", "", "", 0)
                );
    }

    public long crear(Contenido c) {
        return db.queryForObject(
            "INSERT INTO contenidos(tipo,titulo,subtitulo,descripcion,imagen,imagen_secundaria,contacto,cargo,calificacion,orden,activo) VALUES (?,?,?,?,?,?,?,?,?,?,?) RETURNING id",
            Long.class, c.tipo().name(), c.titulo(), c.subtitulo(), c.descripcion(),
            c.imagen(), c.imagenSecundaria(), c.contacto(), c.cargo(), c.calificacion(), c.orden(), c.activo());
    }

    public void editar(long id, Contenido c) {
        int filas = db.update("UPDATE contenidos SET titulo=?,subtitulo=?,descripcion=?,imagen=?,imagen_secundaria=?,contacto=?,cargo=?,calificacion=?,orden=?,activo=?,version=version+1 WHERE id=? AND version=?",
            c.titulo(), c.subtitulo(), c.descripcion(), c.imagen(), c.imagenSecundaria(), c.contacto(), c.cargo(), c.calificacion(), c.orden(), c.activo(), id, c.version());
        if (filas == 0) throw new Errores().conflicto();
    }

    public void eliminar(long id, long version) {
        if (db.update("DELETE FROM contenidos WHERE id=? AND version=?", id, version) == 0) throw new Errores().conflicto();
    }

    public void editarSeccion(Tipo tipo, Seccion s) {
        int filas = db.update("UPDATE secciones SET titulo=?,subtitulo=?,activo=?,version=version+1 WHERE codigo=? AND version=?",
            s.titulo(), s.subtitulo(), s.activo(), tipo.name(), s.version());
        if (filas == 0) throw Errores.conflicto();
    }

    public Configuracion configurar(Configuracion c) {
        db.update("UPDATE configuracion SET nombre=?,lema=?,telefono=?,correo=?,direccion=?,horario=?,mapa=?,facebook=?,instagram=?,whatsapp=?,version=version+1 WHERE id=1 AND version=?",
            c.nombre(), c.lema(), c.telefono(), c.correo(), c.direccion(), c.horario(), c.mapa(), c.facebook(), c.instagram(), c.whatsapp(), c.version());
        return configuracion();
    }

    public Usuario guardarUsuario(UsuarioEntrada u) {
        db.update("INSERT INTO usuarios(nombre,email,clave,rol,activo) VALUES (?,?,?,?,?)", u.nombre(), u.email(), u.clave(), u.rol().name(), u.activo());
        return db.queryForObject("SELECT * FROM usuarios WHERE email=?", this::filaUsuario, u.email());
    }

    public void editarUsuario(long id, UsuarioEntrada u) {
        String hash = u.clave().isBlank() ? db.queryForObject("SELECT clave FROM usuarios WHERE id=?", String.class, id) : u.clave();
        if (db.update("UPDATE usuarios SET nombre=?,email=?,clave=?,rol=?,activo=?,version=version+1 WHERE id=? AND version=?", u.nombre(), u.email(), hash, u.rol().name(), u.activo(), id, u.version()) == 0) throw new Errores().conflicto();
    }

    public void eliminarUsuario(long id, long version) {
        if (db.update("DELETE FROM usuarios WHERE id=? AND version=?", id, version) == 0) throw new Errores().conflicto();
    }

    public Usuario obtenerUsuario(long id) {
        return db.query("SELECT * FROM usuarios WHERE id=?", this::filaUsuario, id).stream().findFirst().orElseThrow(Errores::conflicto);
    }

    public Usuario porEmail(String email) {
        return db.query("SELECT * FROM usuarios WHERE email=?", this::filaUsuario, email.toLowerCase()).stream().findFirst().orElseThrow(() -> new org.springframework.security.core.userdetails.UsernameNotFoundException("Usuario no disponible"));
    }

    public String hashClave(long id) {
        return db.queryForObject("SELECT clave FROM usuarios WHERE id=?", String.class, id);
    }

    public void actualizarClave(String email, String hash) {
        db.update("UPDATE usuarios SET clave=?,version=version+1 WHERE email=?", hash, email);
    }

    public void guardarArchivo(Archivo a) {
        db.update("INSERT INTO archivos(nombre,original,mime,bytes) VALUES (?,?,?,?)", a.nombre(), a.original(), a.mime(), a.bytes());
    }

    public boolean existeArchivo(String nombre) {
        return !db.query("SELECT nombre FROM archivos WHERE nombre=?", (r, n) -> r.getString(1), nombre).isEmpty();
    }

    public void borrarArchivo(String nombre) {
        db.update("DELETE FROM archivos WHERE nombre=?", nombre);
    }

    public void actualizarOrden(long id, int orden) {
        db.update("UPDATE contenidos SET orden=?, version=version+1 WHERE id=?", orden, id);
    }

    public Contenido obtener(long id) {
        return db.query("SELECT * FROM contenidos WHERE id=?", this::fila, id).stream().findFirst().orElseThrow(Errores::conflicto);
    }

    public void bloquear() {
        db.execute("SELECT id FROM configuracion WHERE id=1 FOR UPDATE");
    }

    public static long insertar(JdbcTemplate db,String sql,Object...args) {
        return db.queryForObject(sql,Long.class,args);
    }
}