package src.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Register {
    
    public static boolean registerUser(String username, String email, String password) {
        // First check if username or email already exists
        if (usernameOrEmailExists(username, email)) {
            return false;
        }
        
        String query = "INSERT INTO users (username, email, password) VALUES (?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, email);
            pstmt.setString(3, password); // In a real app, use password hashing!
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private static boolean usernameOrEmailExists(String username, String email) {
        String query = "SELECT * FROM users WHERE username = ? OR email = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, email);
            
            ResultSet rs = pstmt.executeQuery();
            return rs.next(); // If there's a result, username or email already exists
            
        } catch (SQLException e) {
            e.printStackTrace();
            return true; // Assume exists on error
        }
    }
}