const jwt = require("jsonwebtoken");

const authMiddleware = (req, res, next) => {
    const token = req.headers["authorization"];

    if (!token) {
        return res.status(403).json({ error: "No token provided" });
    }

    // Remove 'Bearer ' prefix if exists
    const bearerToken = token.startsWith("Bearer ") ? token.slice(7) : token;
    const decodedToken = jwt.decode(bearerToken, { complete: true });


    jwt.verify(bearerToken, process.env.SECRET_KEY, (err, decoded) => {
        if (err) {
            return res.status(401).json({ error: "Token is invalid or expired" });
        }

        // Store the decoded payload for further use (if needed)
        req.user = decoded;
        next();  // Proceed to the next middleware/route handler
    });
};

module.exports = { authMiddleware };
