const express = require("express");
const router = express.Router();

// router.post("/login", async (req, res) => {
//     const { username, password } = req.body;

//     try {
//         const user = await getUserByUsername(username);
//         if (!user || user.password !== password) {
//             return res.status(401).json({ success: false, message: "Invalid credentials" });
//         }

//         req.session.user = { id: user.id, username: user.username };
//         res.json({ success: true, message: "Login successful!" });
//     } catch (error) {
//         res.status(500).json({ success: false, message: "Database error" });
//     }
// });

// router.post("/logout", (req, res) => {
//     req.session.destroy(() => res.json({ success: true, message: "Logged out" }));
// });

module.exports = router;
