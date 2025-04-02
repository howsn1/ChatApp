package db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Authenticate {

    // We create two methods, the first is to register, the second is to authenticate
        public static boolean registerUser(String username, String password, String email) {
          String sql = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
    
       try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
        pstmt.setString(1, username);
        pstmt.setString(2, password);
        pstmt.setString(3, email);
        
        int rowsAffected = pstmt.executeUpdate();
        return rowsAffected > 0;
        
    } catch (SQLException e) {
        System.out.println("Error registering user:");
        e.printStackTrace();
        return false;
    }
}

public static boolean authenticateUser(String username, String password) {
    String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
    
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
        pstmt.setString(1, username);
        pstmt.setString(2, password);
        
        try (ResultSet rs = pstmt.executeQuery()) {
            return rs.next();
        }
        
    } catch (SQLException e) {
        System.out.println("Error authenticating user:");
        e.printStackTrace();
        return false;
    }
}
public static void main(String[] args) {
    if (authenticateUser("testUser", "testPass")) {
        System.out.println(" Authentication successful!");
    } else {
        System.out.println(" Invalid username or password.");
    }
}
    
}
