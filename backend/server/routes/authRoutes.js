const express = require("express");
const jwt = require("jsonwebtoken");
const bcrypt = require("bcryptjs");
const { createUser, getUserByUsername } = require("../controllers/user");
require("dotenv").config();

const router = express.Router();

router.post("/register", async (req, res) => {
    const { username, password, role = "user" } = req.body;  // Default role is "user"

    // Hash password before storing
    const hashedPassword = await bcrypt.hash(password, 10);

    try {
        const user = await createUser(username, hashedPassword, role);
        res.json(user);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

router.post("/login", async (req, res) => {
    const { username, password } = req.body;

    const user = await getUserByUsername(username);

    if (!user || !(await bcrypt.compare(password, user.password))) {
        return res.status(401).json({ message: "Invalid credentials" });
    }

    const token = jwt.sign({ username: username, role: user.role }, process.env.SECRET_KEY, { expiresIn: "1h" });

    res.json({ token }); // Send token to client
});

module.exports = router;
