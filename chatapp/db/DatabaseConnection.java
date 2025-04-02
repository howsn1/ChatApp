package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/chatapp"; 
    private static final String USER = "root"; 
    private static final String PASSWORD = "MySQL0397*/*"; 

    public static Connection getConnection() {
        try {
            System.out.println("Classpath: " + System.getProperty("java.class.path"));
            Class.forName("com.mysql.cj.jdbc.Driver"); // Load MySQL driver
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            System.out.println(" MySQL JDBC Driver not found!");
            e.printStackTrace();
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /*  TO ADD USERS  */
    public static void createUsersTable() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            String sql = "CREATE TABLE IF NOT EXISTS users (" +
                         "user_id INT AUTO_INCREMENT PRIMARY KEY," +
                         "username VARCHAR(50) NOT NULL UNIQUE," +
                         "password VARCHAR(255) NOT NULL," +
                         "email VARCHAR(100)," +
                         "registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                         "last_login TIMESTAMP" +
                         ")";
            
            stmt.executeUpdate(sql);
            System.out.println("Users table created successfully!");
            
        } catch (SQLException e) {
            System.out.println("Error creating users table:");
            e.printStackTrace();
        }
    }
    

    public static void main(String[] args) {
        Connection connection = getConnection();
        if (connection != null) {
            System.out.println(" Connected to MySQL successfully!");
        } else {
            System.out.println(" Connection failed!");
        }
    }
}
