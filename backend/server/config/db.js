const sqlite3 = require("sqlite3").verbose();
const path = require("path");
const bcrypt = require("bcryptjs");
const seedDatabase = require("./dummyData");

const DB_PATH = path.join(__dirname, "../store.db");

const db = new sqlite3.Database(DB_PATH, (err) => {
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
        password TEXT NOT NULL,
        role TEXT 
    )`);

    db.run(`CREATE TABLE IF NOT EXISTS app (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        app_name TEXT NOT NULL,
        file_name TEXT NOT NULL,
        version TEXT NOT NULL,
        package_name TEXT NOT NULL,
        icon BLOB NOT NULL
    )`);

    db.run(`CREATE TABLE IF NOT EXISTS user_app (
        user_id INTEGER NOT NULL,
        app_id INTEGER NOT NULL,
        PRIMARY KEY (user_id, app_id),
        FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
        FOREIGN KEY (app_id) REFERENCES app(id) ON DELETE CASCADE
    )`);

    console.log("Database initialized successfully.");

    // Call function to seed database
    seedDatabase(db);

});

module.exports = db;
