const express = require("express");
const multer = require("multer");
const util = require('util');
const path = require("path");
const fs = require("fs");
const JSZip = require('jszip');
const { Apk } = require('node-apk');

const app = express();
const port = 3000;

// Initialize database
const db = require("./db");
const { version } = require("os");

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

// Set up multer with storage
const upload = multer({ storage });

// Ensure "uploads" folder exists
if (!fs.existsSync("uploads")) {
    fs.mkdirSync("uploads");
}


app.post("/upload", upload.single("file"), async (req, res) => {
    if (!req.file) {
        return res.status(400).json({ error: "No file uploaded" });
    }

    const filePath = req.file.path;

    try {

        const appInfo = await extractAppInfo(filePath);
        const insertQuery = `INSERT INTO app (app_name, file_name, version, package_name, icon) VALUES (?, ?, ?, ?, ?)`;
        db.run(insertQuery, [appInfo.label, appInfo.fileName, appInfo.version, appInfo.packageName, appInfo.iconBytes], (err) => {
            if (err) {
                return res.status(500).json({ error: "Failed to save to database" });
            }

            res.json({
                message: "File uploaded and processed successfully",
                appName: appInfo.label,
                fileName: appInfo.fileName,
                version: appInfo.version,
                packageName: appInfo.packageName,
                icon: appInfo.iconBytes,
            });
        });


    } catch (error) {
        console.error(error);
        return res.status(500).json({ error: error.message });
    }
});

app.get("/apps", (req, res) => {
    db.all(`SELECT id, app_name, file_name, version, package_name, icon FROM app`, [], (err, rows) => {
        if (err) {
            return res.status(500).json({ error: err.message });
        }

        // Convert file paths to just file names
        const apps = rows.map(row => ({
            id: row.id,
            appName: row.app_name,
            fileName: row.file_name,
            version: row.version,
            packageName: row.package_name,
            icon: row.icon.toString("base64")
        }));

        res.json(apps);
    });
});

app.get("/download/:filename", (req, res) => {
    const fileName = req.params.filename; const filePath = path.join(__dirname, "uploads", fileName); // Build the full path to the file
    // Check if the file exists
    fs.stat(filePath, (err, stats) => {
        if (err) {
            return res.status(404).json({ error: "File not found" });
        }
        // Send the file for download 
        res.sendFile(filePath, (err) => { if (err) { return res.status(500).json({ error: "Failed to send file" }); } });
    });
});

// Start server
app.listen(port, () => {
    console.log(`Server running on http://localhost:${port}`);
});


// Helpers
async function extractAppInfo(filePath) {
    // Initialize Apk object with the file path
    const apk = new Apk(filePath);

    try {
        // Read APK's manifest
        const manifestInfo = await apk.getManifestInfo();
        const version = manifestInfo.versionName;
        const packageName = manifestInfo.package;

        // Retrieve manifest and resources concurrently
        const [manifest, resources] = await Promise.all([
            apk.getManifestInfo(),
            apk.getResources()
        ]);

        // Get application label (name)
        let label = manifest.applicationLabel;
        if (typeof label !== "string") {
            const allResources = resources.resolve(label);
            label = (allResources.find((res) => res.locale && res.locale.language === "fr") || allResources[0]).value;
        }

        // Resolve and extract the first application icon found
        const iconResource = resources.resolve(manifest.applicationIcon)[0];
        const iconBytes = await apk.extract(iconResource.value);

        // Close APK
        apk.close();

        // Create an object to hold all the app information
        const appInfo = {
            label: label,
            fileName: path.basename(filePath),
            version: version,
            packageName: packageName,
            iconBytes: iconBytes
        };

        // Return the appInfo object
        return appInfo;

    } catch (error) {
        console.error("Error extracting APK information:", error);
        apk.close();
        throw error;  // Rethrow the error so it can be handled by the caller
    }
}