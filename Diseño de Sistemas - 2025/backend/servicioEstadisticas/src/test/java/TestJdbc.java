import java.sql.*;

public class TestJdbc {
  public static void main(String[] args) {
    String url = "jdbc:mysql://127.0.0.1:3306/tu_db?serverTimezone=UTC&useSSL=false";
    try (Connection c = DriverManager.getConnection(url, "root", "tuPass")) {
      System.out.println("OK: connected");
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
}