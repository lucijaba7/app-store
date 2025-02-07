const sqlite3 = require("sqlite3").verbose();

const db = new sqlite3.Database("store.db", (err) => {
    if (err) {
        console.error("Error opening database:", err.message);
    } else {
        console.log("Connected to SQLite database");
    }
});

// Create tables when the server starts
db.serialize(() => {
    // Users table
    db.run(`CREATE TABLE IF NOT EXISTS user (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        username TEXT UNIQUE NOT NULL,
        password TEXT NOT NULL
    )`);

    // Apps table
    db.run(`CREATE TABLE IF NOT EXISTS app (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        name TEXT UNIQUE NOT NULL,
        icon_path TEXT NOT NULL
    )`);

    // App versions table
    db.run(`CREATE TABLE IF NOT EXISTS version (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        app_id INTEGER NOT NULL,
        version TEXT NOT NULL,
        file_path TEXT NOT NULL,
        uploaded_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (app_id) REFERENCES app(id) ON DELETE CASCADE
    )`);

    // User access to apps table
    db.run(`CREATE TABLE IF NOT EXISTS user_app (
        user_id INTEGER NOT NULL,
        app_id INTEGER NOT NULL,
        PRIMARY KEY (user_id, app_id),
        FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
        FOREIGN KEY (app_id) REFERENCES app(id) ON DELETE CASCADE
    )`);

    // Devices table (tracks devices per user)
    db.run(`CREATE TABLE IF NOT EXISTS device (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        user_id INTEGER NOT NULL,
        device_name TEXT NOT NULL,
        device_unique_id TEXT UNIQUE NOT NULL,
        FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
    )`);

    // Tracks which version of an app is installed on each device
    db.run(`CREATE TABLE IF NOT EXISTS device_app_version (
        device_id INTEGER NOT NULL,
        version_id INTEGER NOT NULL,
        installed_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        PRIMARY KEY (device_id, version_id),
        FOREIGN KEY (device_id) REFERENCES device(id) ON DELETE CASCADE,
        FOREIGN KEY (version_id) REFERENCES version(id) ON DELETE CASCADE
    )`);

    console.log("Database initialized successfully.");
});

module.exports = db; // Export the database connection
