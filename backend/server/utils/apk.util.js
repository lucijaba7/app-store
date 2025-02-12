const { Apk } = require("node-apk");
const path = require('path');

async function extractAppInfo(filePath) {
    const apk = new Apk(filePath);
    try {
        const manifest = await apk.getManifestInfo();
        const resources = await apk.getResources();

        let label = manifest.applicationLabel;
        if (typeof label !== "string") {
            label = resources.resolve(label)[0].value;
        }

        const iconResource = resources.resolve(manifest.applicationIcon)[0];
        const iconBytes = await apk.extract(iconResource.value);

        apk.close();
        return {
            label,
            fileName: path.basename(filePath),
            version: manifest.versionName,
            packageName: manifest.package,
            iconBytes,
        };
    } catch (error) {
        apk.close();
        throw error;
    }
}

module.exports = { extractAppInfo };
