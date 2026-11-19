const admin = require("firebase-admin");
const fs = require("fs");
const path = require("path");

// Replace this with the path to your service account key file
const serviceAccount = require("./serviceAccountKey.json");

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
});

const db = admin.firestore();

async function importCollection(collectionName, jsonFile) {
  const data = JSON.parse(fs.readFileSync(jsonFile, "utf8"));
  const batch = db.batch();

  data.forEach((item) => {
    const docRef = db.collection(collectionName).doc();
    batch.set(docRef, item);
  });

  await batch.commit();
  console.log(`Imported ${data.length} documents into '${collectionName}'`);
}

async function main() {
  await importCollection("furniture", path.join(__dirname, "furniture.json"));
  await importCollection("assemblers", path.join(__dirname, "assemblers.json"));
  console.log("Done!");
}

main().catch(console.error);
