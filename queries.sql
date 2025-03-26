-- Create user authentication table
CREATE TABLE user_auth (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    salt VARCHAR(255) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    last_login DATETIME,
    is_active BOOLEAN DEFAULT TRUE,
    reset_token VARCHAR(255),
    token_expiry DATETIME
);

-- Create contact table
CREATE TABLE contact (
    contact_id INT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    profile_photo BLOB,
    email VARCHAR(255) NOT NULL,
    FOREIGN KEY (email) REFERENCES user_auth(email) ON DELETE CASCADE
);

-- Create conversation table
CREATE TABLE conversation (
    conversation_id INT PRIMARY KEY AUTO_INCREMENT,
    conversation_name VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    created_by INT NOT NULL,
    FOREIGN KEY (created_by) REFERENCES contact(contact_id) ON DELETE CASCADE
);

-- Create message table
CREATE TABLE message (
    message_id INT PRIMARY KEY AUTO_INCREMENT,
    from_email VARCHAR(255) NOT NULL,
    message_text TEXT NOT NULL,
    sent_datetime DATETIME DEFAULT CURRENT_TIMESTAMP,
    conversation_id INT NOT NULL,
    FOREIGN KEY (from_email) REFERENCES contact(email) ON DELETE CASCADE,
    FOREIGN KEY (conversation_id) REFERENCES conversation(conversation_id) ON DELETE CASCADE
);

-- Create group member table
CREATE TABLE group_member (
    contact_id INT NOT NULL,
    conversation_id INT NOT NULL,
    joined_datetime DATETIME DEFAULT CURRENT_TIMESTAMP,
    left_datetime DATETIME,
    PRIMARY KEY (contact_id, conversation_id),
    FOREIGN KEY (contact_id) REFERENCES contact(contact_id) ON DELETE CASCADE,
    FOREIGN KEY (conversation_id) REFERENCES conversation(conversation_id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX idx_message_conversation ON message(conversation_id);
CREATE INDEX idx_message_sender ON message(from_email);
CREATE INDEX idx_group_member_contact ON group_member(contact_id);
CREATE INDEX idx_group_member_conversation ON group_member(conversation_id);
CREATE INDEX idx_user_auth_email ON user_auth(email);
CREATE INDEX idx_contact_email ON contact(email);