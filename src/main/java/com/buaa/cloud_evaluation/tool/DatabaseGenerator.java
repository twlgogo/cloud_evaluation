package com.buaa.cloud_evaluation.tool;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseGenerator {

  public static void generate(String path) {
    File dbFile = new File(path);
    if (dbFile.exists()) {
      return;
    }

    Connection connection = null;
    try {
      connection = DriverManager.getConnection("jdbc:sqlite:" + path);
      Statement statement = connection.createStatement();
      statement.setQueryTimeout(30);

      statement.executeUpdate("create table nodes ("
          + "id integer primary key autoincrement,"
          + "name text,"
          + "type integer,"
          + "parent integer," // 0 if it's root
          + "history_value_ids text," // nullable
          + "current_value_id integer," // nullable
          + "source integer" // nullable
          + ")");

      statement.executeUpdate("create table node_values ("
          + "id integer primary key autoincrement,"
          + "n integer,"
          + "matrix_str string," // nullable
          + "vector_str string" // nullable
          + ")");

      statement.executeUpdate("insert into nodes (name, type, parent, history_value_ids, current_value_id, source) values('root', 0, -1, '1,1', 2, -1)");
      statement.executeUpdate("insert into nodes (name, type, parent, history_value_ids, current_value_id, source) values('child1', 0, 1, '0', -1, -1)");
      statement.executeUpdate("insert into nodes (name, type, parent, history_value_ids, current_value_id, source) values('child2', 0, 1, '0', -1, -1)");
      statement.executeUpdate("insert into nodes (name, type, parent, history_value_ids, current_value_id, source) values('child11', 1, 2, '0', -1, 0)");
      statement.executeUpdate("insert into nodes (name, type, parent, history_value_ids, current_value_id, source) values('child12', 1, 2, '0', -1, 1)");
      statement.executeUpdate("insert into nodes (name, type, parent, history_value_ids, current_value_id, source) values('child2', 1, 3, '0', -1, 0)");

      statement.executeUpdate("insert into node_values (n, matrix_str, vector_str) values (2, '1,1', '2,0.5,0.5')");
      statement.executeUpdate("insert into node_values (n, matrix_str, vector_str) values (2, '0', '2,0.5,0.5')");

    } catch(SQLException e) {
      e.printStackTrace();
    } finally {
      try {
        if(connection != null)
          connection.close();
      } catch(SQLException e) {
        e.printStackTrace();
      }
    }
  }
}
