# SimpleSQL

SimpleSQL es una biblioteca Java que proporciona una interfaz simplificada para interactuar con bases de datos relacionales. Ofrece una capa de abstracción sobre JDBC, facilitando la ejecución de consultas y operaciones de base de datos.

## Características

- **Ejecución de consultas SQL**: Permite ejecutar consultas `SELECT` con o sin parámetros.
- **Mapeo de resultados a objetos**: Las consultas `SELECT` se pueden mapear directamente a objetos Java, evitando la necesidad de procesar manualmente los resultados.
- **Operaciones CRUD**: Admite operaciones `INSERT`, `UPDATE` y `DELETE` con o sin parámetros.
- **Transacciones**: Proporciona métodos para iniciar, confirmar y revertir transacciones.
- **Manejo de excepciones**: Las excepciones de bajo nivel de JDBC se envuelven en `RuntimeException` para un manejo de errores más sencillo.
- **Cierre automático de recursos**: Implementa `AutoCloseable` para facilitar el cierre de la conexión a la base de datos.

## Uso

1. Crea una instancia de `SimpleSQL` pasando los detalles de conexión a la base de datos:

```java
SimpleSQL sql = new SimpleSQL("jdbc:mysql://localhost:3306/mydatabase", "username", "password");
```

2.Ejecuta consultas SELECT con o sin parámetros:

```java
// Consulta sin parámetros
QueryResult result = sql.query("SELECT * FROM users");

// Consulta con parámetros
QueryResult result = sql.query("SELECT * FROM users WHERE id = ?", 1);
```
Mapea los resultados de una consulta a objetos:

```java
class User {
    public int id;
    public String name;
    // getters y setters
    //constructor vacío u omitido
}

// Mapear resultados a un objeto
Object[] params = {1};
User user = (User) sql.query("SELECT * FROM users WHERE id = ?", params, User.class);

// Mapear resultados a un HashMap<Object, User>
HashMap<Object, User> users = (HashMap<Object, User>) sql.query("SELECT * FROM users", User.class);
```
Ejecuta operaciones INSERT, UPDATE y DELETE:

```java
// INSERT
Object[] paramsInsert = {"John Doe"};
int newId = sql.insert("INSERT INTO users (name) VALUES (?)", paramsInsert);

// UPDATE
Object[] paramsUpdate = {"Jane Doe",1};
int rowsUpdated = sql.update("UPDATE users SET name = ? WHERE id = ?", paramsUpdate);

// DELETE
Object[] paramsDelete = {1};
int rowsDeleted = sql.delete("DELETE FROM users WHERE id = ?", 1);
```
Utiliza transacciones:

```java
sql.iniciarTransaccion();
try {
    // Realizar operaciones de base de datos
    sql.insert(...);
    sql.update(...);
    sql.commitTransaccion(); // Confirmar la transacción
} catch (Exception e) {
    sql.rollbackTransaction(); // Revertir la transacción
}
```
Cierra la conexión a la base de datos:
```java
sql.close();
```
O,úsala en un try-with-resource
```java
try(SimpleSQL sql = new SimpleSQL(url,user,password)){
  //operaciones con la base de datos
} catch(Exception e){
  //manejo de errores
}
```

##Dependencias

Java 8 o superior
Controlador JDBC compatible con tu base de datos (por ejemplo, MySQL Connector/J para MySQL)
