# Secure Gemini backend

The Android app calls the callable function `generateTutorResponse` in
`us-central1`. The function requires Firebase App Check and reads the Gemini
Developer API credential from Firebase Functions Secret Manager.

## Existing secret

The expected secret name is **`GEMINI_API_KEY`**. The secret value is never
stored in this repository, Android resources, Gradle properties, or the APK.
If this secret already exists in the `zahra-self-learning` Firebase project,
do not create another one.

## Deploy

From the repository root, with Firebase CLI authenticated to the
`zahra-self-learning` project:

```bash
npm --prefix functions install
firebase deploy --only functions:generateTutorResponse --project zahra-self-learning
```

Firebase deploy will bind the existing `GEMINI_API_KEY` secret to the
function version. If Firebase reports that the secret is missing or the
deploying identity cannot access it, check Secret Manager in the same Google
Cloud project and grant the Firebase Functions service agent the required
secret accessor permission. Do not paste the secret value into GitHub or chat.

## Security notes

- Callable requests require a valid Firebase App Check token.
- The function rejects empty prompts and prompts longer than 12,000 characters.
- Gemini credentials remain server-side.
- This callable endpoint currently has App Check protection and payload bounds;
  add per-user quotas/rate limiting before a wide public release.
