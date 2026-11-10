package pe.prohabit.portal;

import db.migration.V5__cifrar_claves_usuarios_existentes;
import org.flywaydb.core.api.migration.Context;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class ClavesExistentesTest {
    @Test void corrigeLaClaveAntiguaYConservaLasYaCifradas() throws Exception {
        var origen = new DriverManagerDataSource("jdbc:h2:mem:migracion_claves", "sa", "");
        try (var conexion = origen.getConnection(); var sql = conexion.createStatement()) {
            sql.execute("CREATE TABLE usuarios(id BIGINT PRIMARY KEY, clave VARCHAR(100))");
            var encoder = new BCryptPasswordEncoder(12);
            String hashAnterior = encoder.encode("Admin-Pruebas2026!");
            try (var insertar = conexion.prepareStatement("INSERT INTO usuarios VALUES (?,?)")) {
                insertar.setLong(1, 1); insertar.setString(2, "Operador-Pruebas2026!"); insertar.executeUpdate();
                insertar.setLong(1, 2); insertar.setString(2, hashAnterior); insertar.executeUpdate();
            }
            Context contexto = mock(Context.class);
            when(contexto.getConnection()).thenReturn(conexion);
            var migracion = new V5__cifrar_claves_usuarios_existentes();
            migracion.migrate(contexto);
            String hashOperador;
            try (var filas = sql.executeQuery("SELECT clave FROM usuarios WHERE id=1")) {
                assertTrue(filas.next()); hashOperador = filas.getString(1);
                assertTrue(encoder.matches("Operador-Pruebas2026!", hashOperador));
            }
            migracion.migrate(contexto);
            try (var filas = sql.executeQuery("SELECT clave FROM usuarios ORDER BY id")) {
                assertTrue(filas.next()); assertEquals(hashOperador, filas.getString(1));
                assertTrue(filas.next()); assertEquals(hashAnterior, filas.getString(1));
            }
        }
    }
}
