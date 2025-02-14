const db = require("../config/db");
const { extractAppInfo } = require("../utils/apk.util");

async function saveAppInfo(filePath) {
    const appInfo = await extractAppInfo(filePath);

    const existingApp = await findAppByPackageName(appInfo.packageName);
    if (existingApp) {
        return new Promise((resolve, reject) => {
            db.run(`
            UPDATE app
            SET app_name = ?, file_name = ?, version = ?, icon = ?
            WHERE package_name = ?
        `, [appInfo.label, appInfo.fileName, appInfo.version, appInfo.iconBytes, appInfo.packageName], (err) => {
                if (err) return reject(err);
                resolve(appInfo);
            })
        });
    }
    return new Promise((resolve, reject) => {
        db.run(`
            INSERT INTO app (app_name, file_name, version, package_name, icon) 
            VALUES (?, ?, ?, ?, ?)
            `,
            [appInfo.label, appInfo.fileName, appInfo.version, appInfo.packageName, appInfo.iconBytes],
            (err) => {
                if (err) return reject(err);
                resolve(appInfo);
            }
        );
    });
}


function getAllAppsForUser(user) {
    return new Promise((resolve, reject) => {

        if (user.role == "admin")
            db.all(`
            SELECT id, app_name, file_name, version, package_name, icon 
            FROM app
            `, [], (err, rows) => {
                if (err) return reject(err);
                resolve(rows.map(row => ({
                    id: row.id,
                    appName: row.app_name,
                    fileName: row.file_name,
                    version: row.version,
                    packageName: row.package_name,
                    icon: row.icon.toString("base64"),
                })));
            });
        else {
            db.all(`
                SELECT app.id, app_name, file_name, version, package_name, icon 
                FROM app
                JOIN user_app ON user_app.app_id = app.id
                JOIN user ON user_app.user_id = user.id
                WHERE user.username = ?
                `, [user.username], (err, rows) => {
                if (err) return reject(err);
                resolve(rows.map(row => ({
                    id: row.id,
                    appName: row.app_name,
                    fileName: row.file_name,
                    version: row.version,
                    packageName: row.package_name,
                    icon: row.icon.toString("base64"),
                })));
            });
        }

    });
}


module.exports = { saveAppInfo, getAllAppsForUser };
