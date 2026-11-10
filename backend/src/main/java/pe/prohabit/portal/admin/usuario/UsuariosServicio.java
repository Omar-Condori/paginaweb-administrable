package pe.prohabit.portal.admin.usuario;

import java.sql.*;
import java.util.*;
import java.util.Locale;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.prohabit.portal.shared.Errores;
import pe.prohabit.portal.shared.ContenidoRepositorio;
import pe.prohabit.portal.shared.modelo.*;

@Service
public class UsuariosServicio implements UserDetailsService {
    private final JdbcTemplate db;
    private final PasswordEncoder encoder;
    private final SessionRegistry sesiones;
    public UsuariosServicio(JdbcTemplate db,PasswordEncoder encoder,SessionRegistry sesiones) {this.db=db;this.encoder=encoder;this.sesiones=sesiones;}
    static Usuario fila(ResultSet r,int n) throws SQLException {return new Usuario(r.getLong("id"),r.getString("nombre"),r.getString("email"),Rol.valueOf(r.getString("rol")),r.getBoolean("activo"),r.getLong("version"));}
    public List<Usuario> listar() {return db.query("SELECT * FROM usuarios ORDER BY nombre,id",UsuariosServicio::fila);}
    public Usuario obtener(long id) {return db.query("SELECT * FROM usuarios WHERE id=?",UsuariosServicio::fila,id).stream().findFirst().orElseThrow(()->Errores.noExiste("Usuario"));}
    public Usuario porEmail(String email) {return db.query("SELECT * FROM usuarios WHERE LOWER(TRIM(email))=?",UsuariosServicio::fila,email.strip().toLowerCase(Locale.ROOT)).stream().findFirst().orElseThrow(()->new UsernameNotFoundException("Usuario no disponible"));}
    @Override public UserDetails loadUserByUsername(String email) {
        var u=porEmail(email.strip());
        String hash=db.queryForObject("SELECT clave FROM usuarios WHERE id=?",String.class,u.id());
        return User.withUsername(u.email()).password(hash).roles(u.rol().name()).disabled(!u.activo()).build();
    }
    @Transactional
    public Usuario guardar(Long id,UsuarioEntrada u) {
        return guardar(id, u, null);
    }
    @Transactional
    public Usuario guardar(Long id,UsuarioEntrada u,String emailActual) {
        db.queryForObject("SELECT id FROM configuracion WHERE id=1 FOR UPDATE",Integer.class);
        String email=u.email().strip().toLowerCase(Locale.ROOT);
        validarClave(u.clave(),id==null);
        if(id==null) {
            var claves = new GeneratedKeyHolder();
            String hash = encoder.encode(u.clave());
            db.update(conexion -> {
                var sentencia = conexion.prepareStatement(
                    "INSERT INTO usuarios(nombre,email,clave,rol,activo) VALUES (?,?,?,?,?)", new String[]{"id"});
                sentencia.setString(1, u.nombre());
                sentencia.setString(2, email);
                sentencia.setString(3, hash);
                sentencia.setString(4, u.rol().name());
                sentencia.setBoolean(5, u.activo());
                return sentencia;
            }, claves);
            return obtener(Objects.requireNonNull(claves.getKey()).longValue());
        }
        var previo=obtener(id);
        if(previo.email().equalsIgnoreCase(emailActual) && (!u.activo()||u.rol()!=Rol.ADMINISTRADOR)) throw Errores.invalido("No puedes desactivar ni quitar tu propio rol de administrador");
        protegerUltimo(previo,u.rol()==Rol.ADMINISTRADOR&&u.activo());
        String hash=u.clave().isBlank()?db.queryForObject("SELECT clave FROM usuarios WHERE id=?",String.class,id):encoder.encode(u.clave());
        if(db.update("UPDATE usuarios SET nombre=?,email=?,clave=?,rol=?,activo=?,version=version+1 WHERE id=? AND version=?",u.nombre(),email,hash,u.rol().name(),u.activo(),id,u.version())==0) throw Errores.conflicto();
        expirar(previo.email()); return obtener(id);
    }
    @Transactional
    public void eliminar(long id,long version,String emailActual) {
        db.queryForObject("SELECT id FROM configuracion WHERE id=1 FOR UPDATE",Integer.class);
        var u=obtener(id);
        if(u.email().equalsIgnoreCase(emailActual)) throw Errores.invalido("No puedes eliminar tu propia cuenta");
        protegerUltimo(u,false);
        if(db.update("DELETE FROM usuarios WHERE id=? AND version=?",id,version)==0) throw Errores.conflicto();
        expirar(u.email());
    }
    private void protegerUltimo(Usuario anterior,boolean sigueAdmin) {
        if(anterior.rol()==Rol.ADMINISTRADOR&&anterior.activo()&&!sigueAdmin&&db.queryForObject("SELECT COUNT(*) FROM usuarios WHERE rol='ADMINISTRADOR' AND activo=TRUE",Integer.class)<=1) throw Errores.invalido("Debe existir al menos un administrador activo");
    }
    public void cambiarClave(String email,ClaveEntrada c) {
        validarClave(c.nueva(),true);
        var u=loadUserByUsername(email);
        if(!encoder.matches(c.actual(),u.getPassword())) throw Errores.invalido("La contraseña actual no es correcta");
        db.update("UPDATE usuarios SET clave=?,version=version+1 WHERE email=?",encoder.encode(c.nueva()),email);
        expirar(email);
    }
    private void validarClave(String clave,boolean obligatoria) {
        if(!obligatoria&&clave.isBlank()) return;
        if(clave.length()<12||clave.getBytes(java.nio.charset.StandardCharsets.UTF_8).length>72) throw Errores.invalido("La contraseña debe tener al menos 12 caracteres y como máximo 72 bytes");
    }
    private void expirar(String email) {
        for(Object p:sesiones.getAllPrincipals()) if(p instanceof UserDetails u&&u.getUsername().equals(email)) sesiones.getAllSessions(p,false).forEach(s->s.expireNow());
    }
}