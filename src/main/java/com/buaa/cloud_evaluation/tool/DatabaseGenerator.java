package com.buaa.cloud_evaluation.tool;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

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
      Connection finalConnection = connection;
      Statement statement = connection.createStatement();
      statement.setQueryTimeout(30);

      createUserTable(statement,"user_table");
      insertUserItem(statement,"user_table", "admin","admin");
      statement.close();

      try {
        Reader reader = new FileReader("src/main/resources/item/items.json");
        Gson gson = new GsonBuilder().create();
        JsonArray ja = gson.fromJson(reader, JsonArray.class);

        long timestamp = 1541491200;
        Random random = new Random();

        ja.forEach(child -> {

          try {
            JsonObject jo = child.getAsJsonObject();
            int id = jo.get("id").getAsInt();
            int type = jo.get("type").getAsInt();
            String tableName = jo.get("table_name").getAsString();
            String itemName = jo.get("item_name").getAsString();

            switch (type) {
              case 0: {

                Statement statement1 = finalConnection.createStatement();
                createContinuousTable(statement1, tableName);
                statement1.close();

                double max = jo.get("max").getAsDouble();
                double min = jo.get("min").getAsDouble();
                double newMax = max + (max - min) / 100 * 0.5;
                double newMin = min - (max - min) / 100 * 0.5;

                String query = "insert into " + tableName + " (stamp, value) values (?, ?)";
                finalConnection.setAutoCommit(false);
                PreparedStatement ps = finalConnection.prepareStatement(query);
                for (long i = timestamp; i < 6000; i++) {
                  double value = newMin + random.nextDouble() * (newMax - newMin);
                  ps.setLong(1, i);
                  ps.setDouble(2, value);
                  ps.addBatch();
                }
                ps.executeBatch();
                finalConnection.setAutoCommit(true);
                break;
              }
              case 1: {
                createDispersedTable(statement, tableName);

                String query = "insert into " + tableName + " (stamp, value) values (?, ?)";
                finalConnection.setAutoCommit(false);
                PreparedStatement ps = finalConnection.prepareStatement(query);
                for (long i = timestamp; i < 6000; i++) {
                  int value;
                  if (random.nextDouble() <= 0.01) {
                    value = 0;
                  } else {
                    value = 1;
                  }
                  ps.setLong(1, i);
                  ps.setInt(2, value);
                  ps.addBatch();
                }
                ps.executeBatch();
                finalConnection.setAutoCommit(true);
                break;
              }
              case 2: {
                createWindowTable(statement, tableName);

                double max = jo.get("max").getAsDouble();
                int windowSize = jo.get("window_size").getAsInt();

                String query = "insert into " + tableName + " (stamp, value) values (?, ?)";
                finalConnection.setAutoCommit(false);
                PreparedStatement ps = finalConnection.prepareStatement(query);
                for (long i = timestamp; i < 6000; i++) {
                  int value;
                  if (random.nextDouble() <= 0.01 / windowSize * max) {
                    value = 0;
                  } else {
                    value = 1;
                  }
                  ps.setLong(1, i);
                  ps.setInt(2, value);
                  ps.addBatch();
                }
                ps.executeBatch();
                finalConnection.setAutoCommit(true);
                break;
              }
            }
          } catch (SQLException e) {
            throw new RuntimeException(e);
          }
        });
      } catch (FileNotFoundException e) {
        throw new RuntimeException(e);
      }

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
        + "value integer" // 0 or 1
        + ")");
  }

  private static void insertWindowEntry(Statement statement, String tableName, long stamp, int value) throws SQLException {
    statement.executeUpdate("insert into " + tableName + " (stamp, value) values (" + stamp + ", " + value + ")");
  }

  private static void createUserTable(Statement statement,String tableName) throws SQLException {
    statement.executeUpdate("create table " + tableName + " ("
            + "id integer primary key autoincrement,"
            + "user_name varchar ,"
            + "pass_word varchar " //用户名、密码
            + ")");
  }

  private static void insertUserItem(Statement statement, String tableName, String userName, String passWord) throws SQLException {
    statement.executeUpdate("insert into " + tableName + " (user_name, pass_word) values ('" + userName + "', '" + passWord + "')");
  }
}
