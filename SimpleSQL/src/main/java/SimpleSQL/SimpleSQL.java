package SimpleSQL;

import java.sql.*;

public class SimpleSQL implements AutoCloseable{
    private Connection conexion;

    public SimpleSQL(String url,String user,String pass) throws SQLException{
            this.conexion = DriverManager.getConnection(url, user, pass);
    }

    public void close(){
        try {
            if (this.conexion!=null){
                this.conexion.close();
            }
        } catch (SQLException e){
            throw new RuntimeException("Error al cerrar la conexión " , e);
        }
    }

    public QueryResult query(String query){
        validarConsulta(query);
        try(PreparedStatement stmt = this.conexion.prepareStatement(query)){
            return new QueryResult(stmt.executeQuery());
        } catch (SQLException e){
            throw new RuntimeException("Error al hacer la consulta: " ,e);
        }
    }

    public int update(String query,Object... params){
        validarConsulta(query);
        try (PreparedStatement statement = this.conexion.prepareStatement(query)){
            setParameters(statement,params);
            return statement.executeUpdate();
        }catch (SQLException e){
            throw new RuntimeException("Error al hacer la actualización: " ,e);
        }
    }

    public int insert(String query,Object... params){
        validarConsulta(query);
        try(PreparedStatement statement = this.conexion.prepareStatement(query,Statement.RETURN_GENERATED_KEYS)){
            setParameters(statement,params);
            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()){
                return generatedKeys.getInt(1);
            }
        } catch (SQLException e){
            throw new RuntimeException("Ha habido un error al hacer el INSERT: " ,e );
        }
        return 0;
    }

    public void iniciarTransaccion(){
        try{
            this.conexion.setAutoCommit(false);
        } catch (SQLException e){
            throw new RuntimeException("Error al iniciar la transacción: ",e);
        }
    }
    public void commitTransaccion(){
        try {
            this.conexion.commit();
            this.conexion.setAutoCommit(true);
        } catch (SQLException e) {
            throw new RuntimeException("Error al confirmar la transacción", e);
        }
    }
    public void rollbackTransaction() {
        try {
            this.conexion.rollback();
            this.conexion.setAutoCommit(true);
        } catch (SQLException e) {
            throw new RuntimeException("Error al revertir la transacción", e);
        }
    }
    private void setParameters(PreparedStatement statement, Object... params)  throws SQLException {
        for (int i = 0;i<params.length;i++){
            statement.setObject(i+1,params[i]);
        }
    }

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
}
