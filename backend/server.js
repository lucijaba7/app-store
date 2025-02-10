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

const ApkReader = require("node-apk-parser"); // Import APK parser

app.post("/upload", upload.single("file"), async (req, res) => {
    if (!req.file) {
        return res.status(400).json({ error: "No file uploaded" });
    }

    const fileName = req.file.filename;
    const filePath = req.file.path;

    try {
        // Read APK metadata
        const reader = new ApkReader(filePath);
        const manifest = reader.readManifestSync();

        const version = manifest.versionName; // Extract version name
        const packageName = manifest.package; // Extract package name

        // Check if an app with the same package name already exists
        db.get(`SELECT id FROM app WHERE package_name = ?`, [packageName], (err, row) => {
            if (err) {
                return res.status(500).json({ error: err.message });
            }

            if (row) {
                return res.status(400).json({ error: "App already exists with this package name" });
            }

            // Insert file details into the database
            db.run(
                `INSERT INTO app (file_path, version, package_name) VALUES (?, ?, ?)`,
                [filePath, version, packageName],
                function (err) {
                    if (err) {
                        return res.status(500).json({ error: err.message });
                    }
                    res.json({
                        message: "File uploaded successfully",
                        appId: this.lastID,
                        version: version,
                        package: packageName
                    });
                }
            );
        });

    } catch (error) {
        return res.status(500).json({ error: "Failed to read APK metadata" });
    }
});

app.get("/apps", (req, res) => {
    db.all(`SELECT id, file_path, version, package_name FROM app`, [], (err, rows) => {
        if (err) {
            return res.status(500).json({ error: err.message });
        }

        // Convert file paths to just file names
        const apps = rows.map(row => ({
            id: row.id,
            file_name: row.file_path.split("\\").pop(), // Extract file name from path
            version: row.version,
            package_name: row.package_name
        }));

        res.json(apps);
    });
});

// Start server
app.listen(port, () => {
    console.log(`Server running on http://localhost:${port}`);
});