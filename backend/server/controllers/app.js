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

function findAppByPackageName(packageName) {
    return new Promise((resolve, reject) => {
        const query = `SELECT * FROM app WHERE package_name = ?`;
        db.get(query, [packageName], (err, row) => {
            if (err) return reject(err);
            if (!row) return resolve(null); // Return null if no app is found
            resolve(row); // Return the full app object
        });
    });
}

function getAllApps() {
    return new Promise((resolve, reject) => {
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
    });
}


module.exports = { saveAppInfo, getAllApps, findAppByPackageName };
