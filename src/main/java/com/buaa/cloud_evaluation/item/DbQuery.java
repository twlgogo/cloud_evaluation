package com.buaa.cloud_evaluation.item;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DbQuery {

  private static Connection connection;

  static {
    try {
      String path = "src/main/resources/db/cloud_evaluate_item.db";
      connection = DriverManager.getConnection("jdbc:sqlite:" + path);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public static double getContinuousValue(String tableName, long timestamp) {
    try (Statement statement = connection.createStatement()) {
      ResultSet rs = statement.executeQuery("select value from " + tableName
          + " where stamp <= " + timestamp
          + " order by stamp desc"
          + " limit 1");
      if (rs.next()) {
        return rs.getDouble("value");
      } else {
        return 1.0;
      }
    } catch (SQLException e) {
      return 1.0;
    }
  }

  public static boolean getDispersedState(String tableName, long timestamp) {
    try (Statement statement = connection.createStatement()) {
      ResultSet rs = statement.executeQuery("select value from " + tableName
          + " where stamp <= " + timestamp
          + " order by stamp desc"
          + " limit 1");
      if (rs.next()) {
        return rs.getInt("value") != 0;
      } else {
        return true;
      }
    } catch (SQLException e) {
      return true;
    }
  }

  public static int getWindowCount(String tableName, long timestamp, long window) {
    try (Statement statement = connection.createStatement()) {
      ResultSet rs = statement.executeQuery("select value from " + tableName
          + " where stamp <= " + timestamp + " and stamp >= " + (timestamp - window) + " and value = 1");
      int count = 0;
      while (rs.next()) {
        count++;
      }
      return count;
    } catch (SQLException e) {
      return 0;
    }
  }
}
