package db.migration;

import java.util.regex.Pattern;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/** Conserva las credenciales existentes, corrigiendo las guardadas sin BCrypt. */
public class V5__cifrar_claves_usuarios_existentes extends BaseJavaMigration {
    private static final Pattern BCRYPT = Pattern.compile("\\A\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}\\z");

    @Override
    public void migrate(Context context) throws Exception {
        var encoder = new BCryptPasswordEncoder(12);
        try (var consulta = context.getConnection().createStatement();
             var filas = consulta.executeQuery("SELECT id, clave FROM usuarios FOR UPDATE");
             var actualizar = context.getConnection().prepareStatement("UPDATE usuarios SET clave=? WHERE id=?")) {
            while (filas.next()) {
                String clave = filas.getString("clave");
                if (!BCRYPT.matcher(clave).matches()) {
                    actualizar.setString(1, encoder.encode(clave));
                    actualizar.setLong(2, filas.getLong("id"));
                    actualizar.executeUpdate();
                }
            }
        }
    }
}
