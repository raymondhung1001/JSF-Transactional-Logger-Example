-- Insert default users if they don't exist
INSERT INTO users (username, password, email, fullName) 
SELECT 'admin', 'admin123', 'admin@example.com', 'Administrator'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin');

INSERT INTO users (username, password, email, fullName) 
SELECT 'user1', 'user123', 'user1@example.com', 'User One'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'user1');

INSERT INTO users (username, password, email, fullName) 
SELECT 'user2', 'user123', 'user2@example.com', 'User Two'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'user2');

