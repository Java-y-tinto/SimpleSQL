package SimpleSQL.test;
import SimpleSQL.*;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;

class actor {
    private int actor_id;
    private String first_name;
    private String last_name;
    private Timestamp last_update;
    /*
        public actor(String first_name, int actor_id, String last_name, Timestamp last_update) {
            this.first_name = first_name;
            this.actor_id = actor_id;
            this.last_name = last_name;
            this.last_update = last_update;
        }
    */
    public actor(){

    }
    public int getActor_id() {
        return actor_id;
    }

    public void setActor_id(int actor_id) {
        this.actor_id = actor_id;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public Timestamp getLast_update() {
        return last_update;
    }

    public void setLast_update(Timestamp last_update) {
        this.last_update = last_update;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }
}

public class test {
    public static void main(String[] args){

        SimpleSQL sql = null;
        try {
            sql = new SimpleSQL("jdbc:mysql://localhost:3306/sakila","root","mysqlkubernetes");
            HashMap<Object, actor> actores = (HashMap<Object, actor>) sql.query("SELECT * FROM sakila.actor", actor.class);
            for (actor Actor : actores.values()) {
                System.out.println("Nombre: " + Actor.getFirst_name());
            }
            sql.getResultSet().close();
        } catch (SQLException e){
            System.out.println(e);
        }


    }
}
