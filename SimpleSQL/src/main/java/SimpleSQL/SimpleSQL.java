package SimpleSQL;

import java.sql.*;

/**
 * Clase para interactuar con una base de datos SQL de manera simplificada.
 * Proporciona métodos para realizar consultas, actualizaciones e inserciones,
 * así como para gestionar transacciones.
 */
public class SimpleSQL implements AutoCloseable {

    private Connection conexion; // Conexión a la base de datos

    /**
     * Constructor de la clase SimpleSQL.
     * Establece una conexión con la base de datos utilizando la URL, el usuario y la contraseña proporcionados.
     *
     * @param url [String] URL de la base de datos
     * @param user [String] Usuario de la base de datos
     * @param pass [String]Contraseña de la base de datos
     * @throws SQLException Si hay un error al establecer la conexión con la base de datos
     */
    public SimpleSQL(String url, String user, String pass) throws SQLException {
        this.conexion = DriverManager.getConnection(url, user, pass);
    }

    /**
     * Cierra la conexión con la base de datos.
     * Si la conexión ya está cerrada, no hace nada.
     *
     * @throws RuntimeException Si hay un error al cerrar la conexión
     */
    public void close() {
        try {
            if (this.conexion != null) {
                this.conexion.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al cerrar la conexión ", e);
        }
    }

    /**
     * Ejecuta una consulta SQL en la base de datos y devuelve el resultado.
     *
     * @param query [String] Consulta SQL a ejecutar
     * @return resultado [QueryResult] de la consulta
     * @throws RuntimeException Si hay un error al ejecutar la consulta
     */
    public QueryResult query(String query) {
        validarConsulta(query);
        try {
            PreparedStatement stmt = this.conexion.prepareStatement(query);
            return new QueryResult(stmt.executeQuery());
        } catch (SQLException e) {
            throw new RuntimeException("Error al hacer la consulta: ", e);
        }
    }

    /**
     * Ejecuta una actualización SQL en la base de datos.
     *
     * @param query  Consulta SQL de actualización
     * @param params Parámetros de la consulta
     * @return Número de filas afectadas por la actualización
     * @throws RuntimeException Si hay un error al ejecutar la actualización
     */
    public int update(String query, Object... params) {
        validarConsulta(query);
        try (PreparedStatement statement = this.conexion.prepareStatement(query)) {
            setParameters(statement, params);
            return statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al hacer la actualización: ", e);
        }
    }

    /**
     * Ejecuta una inserción SQL en la base de datos.
     *
     * @param query  Consulta SQL de inserción
     * @param params Parámetros de la consulta
     * @return El ID generado por la inserción, si corresponde
     * @throws RuntimeException Si hay un error al ejecutar la inserción
     */
    public int insert(String query, Object... params) {
        validarConsulta(query);
        try (PreparedStatement statement = this.conexion.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            setParameters(statement, params);
            statement.executeUpdate();
            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ha habido un error al hacer el INSERT: ", e);
        }
        return -1;
    }

    /**
     * Inicia una transacción en la base de datos.
     *
     * @throws RuntimeException Si hay un error al iniciar la transacción
     */
    public void iniciarTransaccion() {
        try {
            this.conexion.setAutoCommit(false);
        } catch (SQLException e) {
            throw new RuntimeException("Error al iniciar la transacción: ", e);
        }
    }

    /**
     * Confirma la transacción en la base de datos.
     *
     * @throws RuntimeException Si hay un error al confirmar la transacción
     */
    public void commitTransaccion() {
        try {
            this.conexion.commit();
            this.conexion.setAutoCommit(true);
        } catch (SQLException e) {
            throw new RuntimeException("Error al confirmar la transacción", e);
        }
    }

    /**
     * Revierte la transacción en la base de datos.
     *
     * @throws RuntimeException Si hay un error al revertir la transacción
     */
    public void rollbackTransaction() {
        try {
            this.conexion.rollback();
            this.conexion.setAutoCommit(true);
        } catch (SQLException e) {
            throw new RuntimeException("Error al revertir la transacción", e);
        }
    }

    /**
     * Establece los parámetros en un PreparedStatement.
     *
     * @param statement PreparedStatement en el que establecer los parámetros
     * @param params     Parámetros a establecer
     * @throws SQLException Si hay un error al establecer los parámetros
     */
    private void setParameters(PreparedStatement statement, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            statement.setObject(i + 1, params[i]);
        }
    }

    /**
     * Valida una consulta SQL para asegurarse de que comience con una palabra clave válida.
     *
     * @param query Consulta SQL a validar
     * @throws RuntimeException Si la consulta SQL es incorrecta
     */
    public void validarConsulta(String query) {
        String[] keywords = {"SELECT", "UPDATE", "DELETE", "INSERT INTO", "CREATE", "DROP", "ALTER"};
        // Verificar si la sentencia comienza con una palabra clave SQL
        boolean valid = false;
        for (String keyword : keywords) {
            if (query.trim().toUpperCase().startsWith(keyword)) {
                valid = true;
                break;
            }
        }
        if (!valid) {
            throw new RuntimeException("La sentencia SQL es incorrecta.");
        }
    }
}
