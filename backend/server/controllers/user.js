const db = require("../config/db");

function getUserByUsername(username) {
    return new Promise((resolve, reject) => {
        db.get(`SELECT * FROM user WHERE username = ?`, [username], (err, row) => {
            if (err) return reject(err);
            resolve(row); // Return user object or null
        });
    });
}

function createUser(username, hashedPassword, role) {
    return new Promise((resolve, reject) => {
        db.run(
            "INSERT INTO user (username, password, role) VALUES (?, ?, ?)",
            [username, hashedPassword, role],
            (err) => {
                if (err) return reject(err);
                resolve({ id: this.lastID, username, role });
            }
        );
    });


}

module.exports = { getUserByUsername, createUser };
