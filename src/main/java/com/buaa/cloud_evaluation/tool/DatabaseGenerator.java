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

      addNode(statement, "总分", 0, -1, "0", -1, -1);
      addNode(statement, "业务延续性", 0, 1, "0", -1, -1);
      addNode(statement, "能耗", 0, 1, "0", -1, -1);
      addNode(statement, "资源池状态", 0, 1, "0", -1, -1);
      addNode(statement, "动环指标", 0, 1, "0", -1, -1);

//
//      statement.executeUpdate("insert into nodes (name, type, parent, history_value_ids, current_value_id, source) values('总分', 0, -1, '0', -1, -1)");
//      statement.executeUpdate("insert into nodes (name, type, parent, history_value_ids, current_value_id, source) values('child1', 0, 1, '0', -1, -1)");
//      statement.executeUpdate("insert into nodes (name, type, parent, history_value_ids, current_value_id, source) values('child2', 0, 1, '0', -1, -1)");
//      statement.executeUpdate("insert into nodes (name, type, parent, history_value_ids, current_value_id, source) values('child11', 1, 2, '0', -1, 0)");
//      statement.executeUpdate("insert into nodes (name, type, parent, history_value_ids, current_value_id, source) values('child12', 1, 2, '0', -1, 1)");
//      statement.executeUpdate("insert into nodes (name, type, parent, history_value_ids, current_value_id, source) values('child2', 1, 3, '0', -1, 0)");
//
//      statement.executeUpdate("insert into node_values (n, matrix_str, vector_str) values (2, '1,1', '2,0.5,0.5')");
//      statement.executeUpdate("insert into node_values (n, matrix_str, vector_str) values (2, '0', '2,0.5,0.5')");

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

  private static void addNode(Statement statement, String name, int type, int parent, String historyValuesIds, int currentValueId, int source)
      throws SQLException {
    String command = String.format("insert into nodes (name, type, parent, history_value_ids, current_value_id, source) values('%s', %d, %d, '%s', %d, %d)", name, type, parent, historyValuesIds, currentValueId, source);
    statement.executeUpdate(command);
  }


  public static void generateItemDataDB(String path) {
    File dbFile = new File(path);
    if (dbFile.exists()) {
      return;
    }

    Connection connection = null;
    try {
      connection = DriverManager.getConnection("jdbc:sqlite:" + path);
      Statement statement = connection.createStatement();
      statement.setQueryTimeout(30);

      createDispersedTable(statement, "table_1");
      insertDispersedEntry(statement, "table_1", 3, 0);
      insertDispersedEntry(statement, "table_1", 5, 1);

      createDispersedTable(statement, "table_2");
      insertDispersedEntry(statement, "table_2", 6, 0);
      insertDispersedEntry(statement, "table_2", 9, 1);

      createContinuousTable(statement, "table_3");
      insertContinuousEntry(statement, "table_3", 0, 0.213);
      insertContinuousEntry(statement, "table_3", 1, 0.245324);
      insertContinuousEntry(statement, "table_3", 2, 0.342523);
      insertContinuousEntry(statement, "table_3", 3, 0.41233);
      insertContinuousEntry(statement, "table_3", 4, 0.41233);
      insertContinuousEntry(statement, "table_3", 5, 0.41233);
      insertContinuousEntry(statement, "table_3", 6, 0.41233);
      insertContinuousEntry(statement, "table_3", 7, 0.41233);

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

  private static void createContinuousTable(Statement statement, String tableName) throws SQLException {
    statement.executeUpdate("create table " + tableName + " ("
        + "id integer primary key autoincrement,"
        + "stamp integer,"
        + "value real"
        + ")");
  }

  private static void insertContinuousEntry(Statement statement, String tableName, long stamp, double value) throws SQLException {
    statement.executeUpdate("insert into " + tableName + " (stamp, value) values (" + stamp + ", " + value + ")");
  }

  private static void createDispersedTable(Statement statement, String tableName) throws SQLException {
    statement.executeUpdate("create table " + tableName + " ("
        + "id integer primary key autoincrement,"
        + "stamp integer,"
        + "value integer" // 0 or 1
        + ")");
  }

  private static void insertDispersedEntry(Statement statement, String tableName, long stamp, int value) throws SQLException {
    statement.executeUpdate("insert into " + tableName + " (stamp, value) values (" + stamp + ", " + value + ")");
  }

  private static void createWindowTable(Statement statement, String tableName) throws SQLException {
    statement.executeUpdate("create table " + tableName + " ("
        + "id integer primary key autoincrement,"
        + "stamp integer,"
        + "value integer" // always 1
        + ")");
  }

  private static void insertWindowEntry(Statement statement, String tableName, long stamp, int value) throws SQLException {
    statement.executeUpdate("insert into " + tableName + " (stamp, value) values (" + stamp + ", " + value + ")");
  }
}
