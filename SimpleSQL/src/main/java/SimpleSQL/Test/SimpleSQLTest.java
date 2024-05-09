package SimpleSQL.Test;

import SimpleSQL.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class SimpleSQLTest {
    static SimpleSQL conexion;
    @BeforeAll
    public static void beforeAll() throws SQLException {
        conexion = new SimpleSQL("urldeprueba","prueba","prueba");
    }
    @Test
    public void queryTest() throws SQLException {
        QueryResult resultados = conexion.query("SELECT 'HOLA' FROM DUAL");
        //resultados.getResultMultimap().isEmpty();
        assertFalse(resultados.getResultMultimap().isEmpty());
        resultados.getResultados().close();
    }
    @AfterAll
    public static void clean(){
        conexion.close();
    }
}
