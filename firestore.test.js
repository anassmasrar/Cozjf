const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
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

test("Unauthenticated user cannot read or write projects", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("projects").get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("projects").doc("proj_1").set({
      userId: ALICE_UID,
      name: "Test",
    })
  );
});

test("Authenticated user can create, list, update, and delete own StudioProject", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const projectRef = aliceDb.collection("users").doc(ALICE_UID).collection("projects").doc("proj_alpha");
  const now = new Date(Date.now() - 1000);

  await assertSucceeds(
    projectRef.set({
      userId: ALICE_UID,
      name: "Scene Cube Studio",
      geometryMode: "SMOOTH_ORGANIQUE",
      renderEngine: "POLYX3D_MOBILE",
      nodeCount: 3,
      keyframeCount: 4,
      sceneSummary: "Cube_01|Sphere_02",
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("projects").get()
  );

  await assertSucceeds(
    projectRef.update({
      name: "Scene Cube V2",
      nodeCount: 5,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  await assertSucceeds(projectRef.delete());
});

test("Cross-user isolation: Bob cannot read, list, create, or delete Alice projects", async () => {
  const now = new Date(Date.now() - 1000);
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(ALICE_UID).collection("projects").doc("alice_proj").set({
      userId: ALICE_UID,
      name: "Alice Private Scene",
      geometryMode: "SMOOTH_ORGANIQUE",
      renderEngine: "POLYX3D_MOBILE",
      nodeCount: 2,
      keyframeCount: 2,
      sceneSummary: "Cube_01",
      createdAt: now,
      updatedAt: now,
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("projects").doc("alice_proj").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("projects").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("projects").doc("alice_proj").delete());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("projects").doc("spoof_proj").set({
      userId: ALICE_UID,
      name: "Spoof",
      geometryMode: "SMOOTH_ORGANIQUE",
      renderEngine: "POLYX3D_MOBILE",
      nodeCount: 1,
      keyframeCount: 0,
      sceneSummary: "Cube_01",
      createdAt: now,
      updatedAt: now,
    })
  );
});

test("Shadow update and invalid enum are rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const projectRef = aliceDb.collection("users").doc(ALICE_UID).collection("projects").doc("proj_sec");
  const now = new Date(Date.now() - 1000);

  // Ghost field isAdmin rejected
  await assertFails(
    projectRef.set({
      userId: ALICE_UID,
      name: "Ghost Test",
      geometryMode: "SMOOTH_ORGANIQUE",
      renderEngine: "POLYX3D_MOBILE",
      nodeCount: 1,
      keyframeCount: 0,
      sceneSummary: "Cube_01",
      isAdmin: true,
      createdAt: now,
      updatedAt: now,
    })
  );

  // Invalid geometryMode enum rejected
  await assertFails(
    projectRef.set({
      userId: ALICE_UID,
      name: "Bad Enum",
      geometryMode: "INVALID_MODE",
      renderEngine: "POLYX3D_MOBILE",
      nodeCount: 1,
      keyframeCount: 0,
      sceneSummary: "Cube_01",
      createdAt: now,
      updatedAt: now,
    })
  );
});
