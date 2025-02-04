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
    db.run(`CREATE TABLE IF NOT EXISTS user (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        username TEXT UNIQUE NOT NULL,
        password TEXT NOT NULL
    )`);

    db.run(`CREATE TABLE IF NOT EXISTS app (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        name TEXT UNIQUE NOT NULL
    )`);

    db.run(`CREATE TABLE IF NOT EXISTS version (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        app_id INTEGER NOT NULL,
        version TEXT NOT NULL,
        file_path TEXT NOT NULL,  
        uploaded_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (app_id) REFERENCES app(id) ON DELETE CASCADE
    )`);

    db.run(`CREATE TABLE IF NOT EXISTS user_app (
        user_id INTEGER NOT NULL,
        app_id INTEGER NOT NULL,
        PRIMARY KEY (user_id, app_id),
        FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
        FOREIGN KEY (app_id) REFERENCES app(id) ON DELETE CASCADE
    )`);

    console.log("Database initialized successfully.");
});

module.exports = db; // Export the database connection
