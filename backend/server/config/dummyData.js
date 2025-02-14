const bcrypt = require("bcryptjs");

async function seedDatabase(db) {
    console.log("Seeding database with dummy data...");

    const users = [
        { username: "user1", password: "password1", role: "user" },
        { username: "user2", password: "password2", role: "user" },
        { username: "novena", password: "novena", role: "admin" },
    ];

    // Function to insert a single user and wait for completion
    const insertUser = (user) => {
        return new Promise(async (resolve, reject) => {
            const hashedPassword = await bcrypt.hash(user.password, 10);
            db.run(
                `INSERT OR IGNORE INTO "user" (username, password, role) VALUES (?, ?, ?)`,
                [user.username, hashedPassword, user.role],
                function (err) {
                    if (err) {
                        console.error("User Insert Error:", err.message);
                        reject(err);
                    } else {
                        console.log(`Inserted user: ${user.username}`);
                        resolve();
                    }
                }
            );
        });
    };

    try {
        for (const user of users) {
            await insertUser(user); // Ensure one inserts before moving to the next
        }
    } catch (error) {
        console.error("Seeding failed:", error);
    }

    // Upload two apps and then INSERT INTO user_app VALUES(1, 1), (2, 2); (user 1  has app 1, user 2 has app 2)
}

module.exports = seedDatabase;
