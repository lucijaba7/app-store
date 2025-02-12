const session = require("express-session");

module.exports = {
    sessionMiddleware: session({
        secret: "your_secret_key",
        resave: false,
        saveUninitialized: true,
    }),

    authMiddleware: (req, res, next) => {
        if (!req.session.user) {
            return res.status(401).json({ success: false, message: "Unauthorized" });
        }
        next();
    },
};