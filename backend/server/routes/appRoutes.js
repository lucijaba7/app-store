const express = require("express");
const { upload, findFile } = require("../config/storage");
const { authMiddleware } = require("../middlewares/authMiddleware");
const { saveAppInfo, getAllAppsForUser } = require("../controllers/app");
const path = require("path");

const router = express.Router();

// List all apps
router.get("/", authMiddleware, async (req, res) => {
    try {
        const apps = await getAllAppsForUser(req.user);
        res.json(apps);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});


// Upload APK (Protected)
router.post("/upload", upload.single("file"), async (req, res) => {
    if (!req.file) return res.status(400).json({ error: "No file uploaded" });
    //if (req.user.role != "admin") return res.status(400).json({ error: "User is not admin" });
    try {
        const appInfo = await saveAppInfo(req.file.path);
        res.json({ success: true, ...appInfo });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});


router.get("/:packageName", async (req, res) => {
    const { packageName } = req.params;

    try {
        // Try to find the file in the directory
        const filePath = findFile(packageName);
        console.log("Found file path:", filePath); // Debugging log

        if (!filePath) {
            return res.status(404).json({ error: "File not found." });
        }

        // Send the file
        res.sendFile(filePath, (err) => {
            if (err) {
                console.error("Error sending file:", err);
                if (!res.headersSent) {
                    res.status(500).json({ error: "Failed to send the file." });
                }
            }
        });

    } catch (error) {
        console.error("Server error:", error);
        res.status(500).json({ error: error.message });
    }
});


module.exports = router;
