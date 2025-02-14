const multer = require("multer");
const fs = require("fs");
const path = require("path");

// Ensure "uploads" folder exists
const uploadDir = "server/uploads";
if (!fs.existsSync(uploadDir)) {
    fs.mkdirSync(uploadDir);
}

// Set up storage for APK files
const storage = multer.diskStorage({
    destination: (req, file, cb) => cb(null, uploadDir),
    filename: (req, file, cb) => cb(null, file.originalname),
});


// Function to search for a file in a directory (recursively)
function findFile(filename) {
    let directory = path.join(__dirname, "../uploads");
    let result = null;
    const files = fs.readdirSync(directory, { withFileTypes: true });

    for (const file of files) {
        const fullPath = path.join(directory, file.name);

        if (file.isDirectory()) {
            // Recursively search in subdirectories
            result = findFile(fullPath, filename);
            if (result) break; // Stop searching if found
        } else if (file.name === filename) {
            return fullPath; // File found
        }
    }
    return result; // Return null if not found
}


const upload = multer({ storage });

module.exports = { upload, findFile };
