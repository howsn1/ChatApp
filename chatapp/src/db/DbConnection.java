

package src.db;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages database connections for the chat application.
 * Uses connection pooling for efficiency and loads database credentials from a properties file.
 */
public class DbConnection {
    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());
    
    // Default values that will be overridden by properties file if available
    private static String url = "jdbc:mysql://localhost:3306/chatapp";
    private static String user = "root";
    private static String password = "MySQL0397*/*";
    private static boolean isInitialized = false;
    
    /**
     * Initializes the database connection parameters from a properties file.
     * Should be called once at application startup.
     */
    public static void initialize() {
        if (isInitialized) {
            return;
        }
        
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("config/database.properties")) {
            props.load(fis);
            
            url = props.getProperty("db.url", url);
            user = props.getProperty("db.user", user);
            password = props.getProperty("db.password", password);
            
            // Load the JDBC driver
            Class.forName(props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver"));
            
            isInitialized = true;
            LOGGER.info("Database connection initialized successfully");
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Could not load database properties file. Using default values.", e);
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "JDBC Driver not found!", e);
            throw new RuntimeException("JDBC Driver not found. Cannot continue.", e);
        }
    }
    
    /**
     * Gets a connection to the database.
     * Will initialize the connection parameters if not already done.
     *
     * @return A connection to the database
     * @throws SQLException If a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        if (!isInitialized) {
            initialize();
        }
        
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to connect to database", e);
            throw e; // Re-throw to allow calling code to handle it
        }
    }
    
    /**
     * Creates the users table if it doesn't already exist.
     */
    public static void createUsersTable() {
        String sql = "CREATE TABLE IF NOT EXISTS users (" +
                     "user_id INT AUTO_INCREMENT PRIMARY KEY," +
                     "username VARCHAR(50) NOT NULL UNIQUE," +
                     "password_hash VARCHAR(255) NOT NULL," +
                     "email VARCHAR(100) NOT NULL UNIQUE," +
                     "registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                     "last_login TIMESTAMP," +
                     "status ENUM('online', 'away', 'offline') DEFAULT 'offline'" +
                     ")";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            LOGGER.info("Users table created successfully");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating users table", e);
        }
    }
    
    /**
     * Creates the messages table if it doesn't already exist.
     */
    public static void createMessagesTable() {
        String sql = "CREATE TABLE IF NOT EXISTS messages (" +
                     "message_id INT AUTO_INCREMENT PRIMARY KEY," +
                     "sender_id INT NOT NULL," +
                     "receiver_id INT," +
                     "group_id INT," +
                     "content TEXT NOT NULL," +
                     "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                     "is_read BOOLEAN DEFAULT FALSE," +
                     "FOREIGN KEY (sender_id) REFERENCES users(user_id)," +
                     "CHECK (receiver_id IS NOT NULL OR group_id IS NOT NULL)" +
                     ")";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            LOGGER.info("Messages table created successfully");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating messages table", e);
        }
    }
    
    /**
     * Creates the groups table if it doesn't already exist.
     */
    public static void createGroupsTable() {
        String sql = "CREATE TABLE IF NOT EXISTS groups (" +
                     "group_id INT AUTO_INCREMENT PRIMARY KEY," +
                     "name VARCHAR(100) NOT NULL," +
                     "created_by INT NOT NULL," +
                     "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                     "FOREIGN KEY (created_by) REFERENCES users(user_id)" +
                     ")";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            LOGGER.info("Groups table created successfully");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating groups table", e);
        }
    }
    
    /**
     * Creates the group_members table if it doesn't already exist.
     */
    public static void createGroupMembersTable() {
        String sql = "CREATE TABLE IF NOT EXISTS group_members (" +
                     "group_id INT NOT NULL," +
                     "user_id INT NOT NULL," +
                     "joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                     "PRIMARY KEY (group_id, user_id)," +
                     "FOREIGN KEY (group_id) REFERENCES groups(group_id)," +
                     "FOREIGN KEY (user_id) REFERENCES users(user_id)" +
                     ")";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            LOGGER.info("Group_members table created successfully");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating group_members table", e);
        }
    }
    
    /**
     * Initialize all database tables.
     * Should be called when the application starts.
     */
    public static void initializeDatabase() {
        createUsersTable();
        createMessagesTable();
        createGroupsTable();
        createGroupMembersTable();
    }
    
    /**
     * Test the database connection.
     */
    public static void main(String[] args) {
        try {
            Connection connection = getConnection();
            LOGGER.info("Connected to MySQL successfully!");
            initializeDatabase();
            connection.close();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Connection failed", e);
        }
    }
}