const express = require("express");
const session = require("express-session");
const cors = require("cors");
const authRoutes = require("./server/routes/authRoutes");
const appRoutes = require("./server/routes/appRoutes");
require("dotenv").config();

const app = express();
const port = process.env.PORT;

// Middleware
app.use(cors()); // Allow frontend to communicate with the backend
app.use(express.json()); // Parse JSON request bodies
app.use(express.urlencoded({ extended: true })); // Parse URL-encoded data

// Routes
app.use("/auth", authRoutes);
app.use("/apps", appRoutes);

// Start Server
app.listen(port, () => {
    console.log(`Server running on http://localhost:${port}`);
});
