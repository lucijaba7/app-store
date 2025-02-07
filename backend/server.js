const express = require("express");
const multer = require("multer");
const path = require("path");
const sqlite3 = require("sqlite3").verbose();

const app = express();
const port = 3000;

// Initialize database
const db = require("./db");

// Serve static files (e.g., app icons) from the 'uploads' folder
app.use("/uploads", express.static(path.join(__dirname, "uploads")));

// Configure storage for APK files
const apkStorage = multer.diskStorage({
    destination: "./uploads/apks/",
    filename: (req, file, cb) => {
        cb(null, file.fieldname + "-" + Date.now() + path.extname(file.originalname));
    }
});

// Configure storage for app icons
const iconStorage = multer.diskStorage({
    destination: "./uploads/icons/",
    filename: (req, file, cb) => {
        cb(null, file.fieldname + "-" + Date.now() + path.extname(file.originalname));
    }
});

// Multer setup to handle both APK and icon files
const upload = multer({
    storage: multer.diskStorage({
        destination: (req, file, cb) => {
            if (file.fieldname === "apk") {
                cb(null, "./uploads/apks/");
            } else if (file.fieldname === "icon") {
                cb(null, "./uploads/icons/");
            }
        },
        filename: (req, file, cb) => {
            cb(null, file.originalname);
        }
    })
});

// Ensure upload directories exist
const fs = require("fs");
if (!fs.existsSync("./uploads/apks")) fs.mkdirSync("./uploads/apks", { recursive: true });
if (!fs.existsSync("./uploads/icons")) fs.mkdirSync("./uploads/icons", { recursive: true });

/** 
 * 🚀 Route: Upload a New App (with First APK Version)
 */
app.post("/upload", upload.fields([{ name: "apk", maxCount: 1 }, { name: "icon", maxCount: 1 }]), (req, res) => {
    const { name, version } = req.body;
    if (!name || !version) return res.status(400).json({ error: "App name and version are required." });

    const iconFile = req.files["icon"] ? req.files["icon"][0].path : null;
    const apkFile = req.files["apk"] ? req.files["apk"][0].path : null;

    if (!iconFile || !apkFile) return res.status(400).json({ error: "Both APK and icon must be uploaded." });

    // Insert new app and then insert the first version
    db.run(`INSERT INTO app (name, icon_path) VALUES (?, ?)`, [name, iconFile], function (err) {
        if (err) return res.status(500).json({ error: err.message });

        const appId = this.lastID; // Get the newly created app's ID

        db.run(
            `INSERT INTO version (app_id, version, file_path, uploaded_at) VALUES (?, ?, ?, CURRENT_TIMESTAMP)`,
            [appId, version, apkFile],
            function (err) {
                if (err) return res.status(500).json({ error: err.message });

                res.json({
                    message: "App and first version uploaded successfully!",
                    app_id: appId,
                    version_id: this.lastID,
                    version: version
                });
            }
        );
    });
});

/** 
 * 🚀 Route 2: Update App Version (with APK)
 * Adds a new version to an existing app.
 */
app.post("/updateVersion", upload.single("apk"), (req, res) => {
    const { app_id, version } = req.body;
    if (!app_id || !version) return res.status(400).json({ error: "App ID and version are required." });

    const apkFile = req.file ? req.file.path : null;
    if (!apkFile) return res.status(400).json({ error: "APK file is required." });

    // Insert new version
    db.run(
        `INSERT INTO version (app_id, version, file_path, uploaded_at) VALUES (?, ?, ?, CURRENT_TIMESTAMP)`,
        [app_id, version, apkFile],
        function (err) {
            if (err) return res.status(500).json({ error: err.message });
            res.json({ message: "Version updated successfully!", version_id: this.lastID, version: version });
        }
    );
});

app.get("/apps", (req, res) => {
    const sql = `
        SELECT app.id, app.name, app.icon_path, v.version
        FROM app
        LEFT JOIN version v ON app.id = v.app_id
        WHERE v.uploaded_at = (
            SELECT MAX(uploaded_at) FROM version WHERE version.app_id = app.id
        )
    `;

    db.all(sql, [], (err, rows) => {
        if (err) return res.status(500).json({ error: err.message });

        // Dynamically get the base URL (protocol + host + port)
        const baseUrl = `${req.protocol}://${req.get("host")}`;
        // Modify the file paths to be dynamic URLs
        const result = rows.map(row => ({
            id: row.id,
            name: row.name,
            appIcon: `${baseUrl}/uploads/icons/${path.basename(row.icon_path)}`,  // Dynamic URL for app icon
            version: row.version,
        }));

        res.json(result); // Return app list with latest version info
    });
});

// Route to get the latest APK of an app
app.get("/download/:appName", (req, res) => {
    const { appName } = req.params;

    db.get(
        `SELECT file_path FROM version 
         WHERE app_id = (SELECT id FROM app WHERE name = ?) 
         ORDER BY version DESC LIMIT 1`,
        [appName],
        (err, row) => {
            if (err) {
                return res.status(500).json({ error: err.message });
            }
            if (!row) {
                return res.status(404).json({ error: "App not found or no version available." });
            }

            const filePath = path.join(__dirname, "uploads", row.file_path);
            res.download(filePath); // Send the APK file
        }
    );
});

// Start server
app.listen(port, () => {
    console.log(`Server running on http://localhost:${port}`);
});
