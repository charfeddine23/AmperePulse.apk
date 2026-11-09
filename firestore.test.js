const { initializeTestEnvironment, assertFails, assertSucceeds } = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;

before(async () => {
  testEnv = await initializeTestEnvironment({
    projectId: "demo-no-project",
    firestore: {
      rules: fs.readFileSync("firestore.rules", "utf8"),
      host: "127.0.0.1",
      port: 8085
    }
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("unauthenticated user cannot read or write user profile", async () => {
  const unauthedDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthedDb.doc("users/user_1").get());
  await assertFails(unauthedDb.doc("users/user_1").set({
    userId: "user_1",
    displayName: "Charfeddine",
    email: "charfeddineka02@gmail.com",
    isCloudSyncActive: true,
    createdAt: new Date(),
    updatedAt: new Date()
  }));
});

test("authenticated owner can create and read their user profile", async () => {
  const authedDb = testEnv.authenticatedContext("user_1").firestore();
  await assertSucceeds(authedDb.doc("users/user_1").set({
    userId: "user_1",
    displayName: "Charfeddine",
    email: "charfeddineka02@gmail.com",
    isCloudSyncActive: true,
    createdAt: new Date(),
    updatedAt: new Date()
  }));
  await assertSucceeds(authedDb.doc("users/user_1").get());
});

test("authenticated user cannot access other user's profile", async () => {
  const authedDb = testEnv.authenticatedContext("user_2").firestore();
  await assertFails(authedDb.doc("users/user_1").get());
});

test("authenticated owner can insert valid telemetry log", async () => {
  const authedDb = testEnv.authenticatedContext("user_1").firestore();
  await assertSucceeds(authedDb.collection("users/user_1/telemetry_logs").doc("log_1").set({
    userId: "user_1",
    timestamp: new Date(),
    voltageMv: 4180,
    currentMa: 2200,
    temperatureC: 28.5,
    batteryPct: 85,
    state: "STORING",
    createdAt: new Date()
  }));
});

test("other user cannot read or insert into foreign user's telemetry logs", async () => {
  const otherDb = testEnv.authenticatedContext("user_2").firestore();
  await assertFails(otherDb.collection("users/user_1/telemetry_logs").doc("log_1").get());
  await assertFails(otherDb.collection("users/user_1/telemetry_logs").doc("log_2").set({
    userId: "user_1",
    timestamp: new Date(),
    voltageMv: 4180,
    currentMa: 2200,
    temperatureC: 28.5,
    batteryPct: 85,
    state: "STORING",
    createdAt: new Date()
  }));
});
