const db = require("../config/db");

function getUserByUsername(username) {
    return new Promise((resolve, reject) => {
        db.get(`SELECT * FROM user WHERE username = ?`, [username], (err, row) => {
            if (err) return reject(err);
            resolve(row); // Return user object or null
        });
    });
}

function createUser(username, password) {
    return new Promise((resolve, reject) => {
        db.run(`INSERT INTO user (username, password) VALUES (?, ?)`, [username, password], function (err) {
            if (err) return reject(err);
            resolve({ id: this.lastID, username });
        });
    });
}

function checkUserAppAccess(userId, appId) {
    return new Promise((resolve, reject) => {
        db.get(`SELECT * FROM user_app WHERE user_id = ? AND app_id = ?`, [userId, appId], (err, row) => {
            if (err) return reject(err);
            resolve(row !== undefined); // Return true if access exists
        });
    });
}

module.exports = { getUserByUsername, createUser, checkUserAppAccess };
