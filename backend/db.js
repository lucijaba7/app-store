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

    // Apps table (stores apps and file paths)
    db.run(`CREATE TABLE IF NOT EXISTS app (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        file_path TEXT NOT NULL,
        version TEXT NOT NULL,
        package_name TEXT NOT NULL
    )`);

    // User-App Access table (tracks which user has access to which app)
    db.run(`CREATE TABLE IF NOT EXISTS user_app (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        user_id INTEGER NOT NULL,
        app_id INTEGER NOT NULL,
        FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
        FOREIGN KEY (app_id) REFERENCES app(id) ON DELETE CASCADE
    )`);

    console.log("Database initialized successfully.");
});

module.exports = db; // Export the database connection
