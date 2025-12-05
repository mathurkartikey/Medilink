import java.sql.*;

public class DBConnectionTest {
    public static void main(String[] args) {
        Connection conn = null;
        try {
            // Step 1: Register the driver (optional for newer versions)
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Step 2: Connect to your database
            conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/medilink", 
                "Kartikey",       // your MySQL username
                "Kartikey@1234"        // your MySQL password
            );

            if (conn != null) {
                System.out.println("✅ Successfully connected to MySQL database!");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { if (conn != null) conn.close(); } catch (Exception e) {}
        }
    }
}