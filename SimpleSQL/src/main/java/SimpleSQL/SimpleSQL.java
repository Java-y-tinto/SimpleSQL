package SimpleSQL;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.HashMap;

public class SimpleSQL implements AutoCloseable{
    private Connection conexion;
    public ResultSet resultSet;

    public ResultSet getResultSet() {
        return resultSet;
    }

    /**
     * Construye una nueva instancia de SimpleSQL y establece la conexión a la base de datos.
     *
     * @param url  La URL de conexión a la base de datos (String).
     * @param user El nombre de usuario para la conexión (String).
     * @param pass La contraseña para la conexión (String).
     * @throws SQLException Si ocurre un error al establecer la conexión.
     */

    public SimpleSQL(String url,String user,String pass) throws SQLException{
            this.conexion = DriverManager.getConnection(url, user, pass);
    }

    /**
     * Cierra la conexión a la base de datos.
     */
    public void close(){
        try {
            if (this.conexion!=null){
                this.conexion.close();
            }
        } catch (SQLException e){
            throw new RuntimeException("Error al cerrar la conexión " , e);
        }
    }

    /**
     * Ejecuta una consulta SELECT en la base de datos.
     *
     * @param query La consulta SQL (String).
     * @return Un objeto QueryResult que contiene los resultados de la consulta.
     */
    public QueryResult query(String query){
        validarConsulta(query);
        try{
        PreparedStatement stmt = this.conexion.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            return new QueryResult(stmt.executeQuery());
        } catch (SQLException e){
            throw new RuntimeException("Error al hacer la consulta: " ,e);
        }
    }

    /**
     * Ejecuta una consulta SELECT en la base de datos con parámetros.
     *
     * @param query  La consulta SQL (String).
     * @param params Los parámetros de la consulta (Object...).
     * @return Un objeto QueryResult que contiene los resultados de la consulta.
     */
    public QueryResult query(String query,Object... params){
        validarConsulta(query);
        try{
            PreparedStatement stmt = this.conexion.prepareStatement(query,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
            setParameters(stmt,params);
            return new QueryResult(stmt.executeQuery());
        } catch (SQLException e){
            throw new RuntimeException("Error al hacer la query: ",e);
        }
    }
    public <T> Object query(String query, Class<T> clazz, Object... params){
        validarConsulta(query);
        try{
            PreparedStatement stmt = this.conexion.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = stmt.executeQuery();
            this.resultSet = rs;
            return mapResultsToObjects(rs,clazz);
        } catch (SQLException e){
            throw new RuntimeException("Error al hacer la consulta: ",e);
        }
    }

    private <T> Object mapResultsToObjects(ResultSet resultSet,Class<T> clazz) throws SQLException{
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnas = metaData.getColumnCount();
        if (resultSet.next()){
            T obj = createObject(resultSet,clazz,columnas);
            if (!resultSet.next()){
                return obj;
            }
        }

        HashMap<Object,T> resultMap = new HashMap<>();
        resultSet.beforeFirst();
        while (resultSet.next()){
            T objeto = createObject(resultSet,clazz,columnas);
            Object clave = resultSet.getObject(1);
            resultMap.put(clave,objeto);
        }
        return resultMap;
    }

    private <T> T createObject(ResultSet resultSet,Class<T> clazz,int columnCount) throws SQLException{
        try {
            Constructor<T> constructor = clazz.getConstructor();
            constructor.setAccessible(true);
            T objeto = constructor.newInstance();
            for (int i = 1;i<=columnCount;i++){
                String columna = resultSet.getMetaData().getColumnName(i);
                Object valor = resultSet.getObject(i);
                Field campo = findField(clazz,columna);
                if (campo != null){
                    campo.setAccessible(true);
                    campo.set(objeto,valor);
                }
            }
            return objeto;
        } catch (Exception e){
            throw new RuntimeException("Error al crear los objetos resultado",e);
        }
    }

    private Field findField(Class<?> clazz, String columnName) {
        try {
            return clazz.getDeclaredField(columnName);
        } catch (NoSuchFieldException e) {
            Class<?> superClass = clazz.getSuperclass();
            if (superClass != null) {
                return findField(superClass, columnName);
            }
            return null;
        }
    }


    /**
     * Ejecuta una sentencia UPDATE en la base de datos.
     *
     * @param query  La sentencia SQL (String).
     * @param params Los parámetros de la sentencia (Object...).
     * @return El número de filas afectadas por la operación UPDATE (int), o -1 si no se actualizaron filas.
     */
    public int update(String query,Object... params){
        validarConsulta(query);
        try (PreparedStatement statement = this.conexion.prepareStatement(query)){
            setParameters(statement,params);
            int updResult =  statement.executeUpdate();
            if (updResult <= 0) {
                return -1;
            }
            return  updResult;
        }catch (SQLException e){
            throw new RuntimeException("Error al hacer la actualización: " ,e);
        }
    }
    /**
     * Ejecuta una sentencia INSERT en la base de datos.
     *
     * @param query  La sentencia SQL (String).
     * @param params Los parámetros de la sentencia (Object...).
     * @return El valor de la clave primaria generada (int), o -1 si no se insertaron filas.
     */
    public int insert(String query,Object... params){
        validarConsulta(query);
        try(PreparedStatement statement = this.conexion.prepareStatement(query,Statement.RETURN_GENERATED_KEYS)){
            setParameters(statement,params);
            statement.executeUpdate();
            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()){
                return generatedKeys.getInt(1);
            }
        } catch (SQLException e){
            throw new RuntimeException("Ha habido un error al hacer el INSERT: " ,e );
        }
        return -1;
    }

    /**
     * Inicia una transacción deshabilitando el autocommit.
     */
    public void iniciarTransaccion(){
        try{
            this.conexion.setAutoCommit(false);
        } catch (SQLException e){
            throw new RuntimeException("Error al iniciar la transacción: ",e);
        }
    }
    /**
     * Confirma los cambios realizados en la transacción actual y habilita el autocommit.
     */
    public void commitTransaccion(){
        try {
            this.conexion.commit();
            this.conexion.setAutoCommit(true);
        } catch (SQLException e) {
            throw new RuntimeException("Error al confirmar la transacción", e);
        }
    }

    /**
     * Revierte los cambios realizados en la transacción actual y habilita el autocommit.
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
     * Establece los parámetros en el objeto PreparedStatement.
     *
     * @param statement El objeto PreparedStatement.
     * @param params    Los parámetros a establecer (Object[]).
     * @throws SQLException Si ocurre un error al establecer los parámetros.
     */
    private void setParameters(PreparedStatement statement, Object... params)  throws SQLException {
        for (int i = 0;i<params.length;i++){
            statement.setObject(i+1,params[i]);
        }
    }
    /**
     * Valida si una sentencia SQL es válida comprobando si comienza con una palabra clave reconocida.
     *
     * @param query La sentencia SQL a validar (String).
     * @throws RuntimeException Si la sentencia SQL no es válida.
     */
    public void validarConsulta(String query){
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
    /**
     * Ejecuta una sentencia DELETE en la base de datos.
     *
     * @param query La sentencia SQL (String).
     * @return El número de filas afectadas por la operación DELETE (int), o -1 si no se eliminaron filas.
     */

    public int delete(String query) {
        validarConsulta(query);
        try (PreparedStatement statement = this.conexion.prepareStatement(query)) {
            int del = statement.executeUpdate();
            if (del <= 0) {
                return -1;
            }
            return del;
        } catch (SQLException e) {
            throw new RuntimeException("Error al hacer el DELETE: ", e);
        }
    }
    /**
     * Ejecuta una sentencia DELETE en la base de datos con parámetros.
     *
     * @param query  La sentencia SQL (String).
     * @param params Los parámetros de la sentencia (Object[]).
     * @return El número de filas afectadas por la operación DELETE (int), o -1 si no se eliminaron filas.
     */
    public int delete(String query, Object... params) {
        validarConsulta(query);
        try (PreparedStatement statement = this.conexion.prepareStatement(query)) {
            setParameters(statement, params);
            int del = statement.executeUpdate();
            if (del <= 0) {
                return -1;
            }
            return del;
        } catch (SQLException e) {
            throw new RuntimeException("Error al hacer el DELETE: ", e);
        }
    }

}
