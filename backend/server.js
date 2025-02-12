const express = require("express");
const session = require("express-session");
const cors = require("cors");
const authRoutes = require("./server/routes/authRoutes");
const appRoutes = require("./server/routes/appRoutes");

const app = express();
const port = 3000;

// Middleware
app.use(cors()); // Allow frontend to communicate with the backend
app.use(express.json()); // Parse JSON request bodies
app.use(express.urlencoded({ extended: true })); // Parse URL-encoded data

// Session Middleware (Simple Auth)
app.use(session({
    secret: "your_secret_key", // Change this to a strong random string
    resave: false,
    saveUninitialized: true,
    cookie: { secure: false } // Set secure: true if using HTTPS
}));

// Routes
app.use("/auth", authRoutes);
app.use("/apps", appRoutes);

// Start Server
app.listen(port, () => {
    console.log(`Server running on http://localhost:${port}`);
});
