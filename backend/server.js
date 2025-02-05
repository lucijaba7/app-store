const express = require("express");
const multer = require("multer");
const path = require("path");
const sqlite3 = require("sqlite3").verbose();

const app = express();
const port = 3000;

// Initialize database
const db = require("./db");

// Set up storage for uploaded APK files
const storage = multer.diskStorage({
    destination: (req, file, cb) => {
        cb(null, "uploads/"); // Save files in "uploads" folder
    },
    filename: (req, file, cb) => {
        const uniqueName = file.originalname;
        cb(null, uniqueName);
    },
});

const upload = multer({ storage });

// Ensure "uploads" folder exists
const fs = require("fs");
if (!fs.existsSync("uploads")) {
    fs.mkdirSync("uploads");
}

// Route to upload an APK and save metadata in database
app.post("/upload", upload.single("apk"), (req, res) => {
    const { name, version } = req.body;

    if (!req.file) {
        return res.status(400).json({ error: "No file uploaded!" });
    }

    const apkFilePath = req.file.filename; // Store filename, not full path

    // Check if the app already exists
    db.get(`SELECT id FROM app WHERE name = ?`, [name], (err, row) => {
        if (err) {
            return res.status(500).json({ error: err.message });
        }

        if (row) {
            // App exists, use its ID
            insertVersion(row.id);
        } else {
            // App doesn't exist, create it
            db.run(`INSERT INTO app (name) VALUES (?)`, [name], function (err) {
                if (err) {
                    return res.status(500).json({ error: err.message });
                }
                insertVersion(this.lastID); // Insert version with new app ID
            });
        }
    });

    // Function to insert the new version
    function insertVersion(app_id) {
        db.run(
            `INSERT INTO version (app_id, version, file_path) VALUES (?, ?, ?)`,
            [app_id, version, apkFilePath],
            function (err) {
                if (err) {
                    return res.status(500).json({ error: err.message });
                }
                res.json({ message: "Uploaded successfully!", version_id: this.lastID });
            }
        );
    }
});


// Start server
app.listen(port, () => {
    console.log(`Server running on http://localhost:${port}`);
});
