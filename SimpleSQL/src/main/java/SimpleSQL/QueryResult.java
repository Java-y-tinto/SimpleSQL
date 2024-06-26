package SimpleSQL;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class QueryResult {
    private final Multimap<String, Object> resultMap;

    private final ResultSet resultados;

    public QueryResult(ResultSet resultSet) {
        this.resultMap = mapResultSetToMultimap(resultSet);
        this.resultados = resultSet;
    }

    public ResultSet getResultados() throws SQLException {
       this.resultados.beforeFirst();
        return resultados;
    }

    public Multimap<String, Object> getResultMultimap() {
        return resultMap;
    }

    private Multimap<String, Object> mapResultSetToMultimap(ResultSet resultSet) {
        Multimap<String, Object> result = ArrayListMultimap.create();
        try {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            while (resultSet.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    Object columnValue = resultSet.getObject(i);
                    result.put(columnName, columnValue);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al mapear los resultados del ResultSet", e);
        }
        return result;
    }
}
