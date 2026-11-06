const { onCall, HttpsError } = require("firebase-functions/v2/https");
const { defineSecret } = require("firebase-functions/params");
const { initializeApp } = require("firebase-admin/app");

initializeApp();

const geminiApiKey = defineSecret("GEMINI_API_KEY");
const MODEL = "gemini-3.8-flash";
const MAX_INPUT_CHARS = 12000;

exports.generateTutorResponse = onCall(
  {
    region: "us-central1",
    enforceAppCheck: true,
    secrets: [geminiApiKey],
    timeoutSeconds: 90,
    memory: "256MiB",
  },
  async (request) => {
    const data = request.data || {};
    const prompt = typeof data.prompt === "string" ? data.prompt.trim() : "";

    if (!prompt) {
      throw new HttpsError("invalid-argument", "A non-empty prompt is required.");
    }
    if (prompt.length > MAX_INPUT_CHARS) {
      throw new HttpsError("invalid-argument", "Prompt is too long.");
    }

    const apiKey = geminiApiKey.value();
    if (!apiKey) {
      throw new HttpsError("failed-precondition", "GEMINI_API_KEY secret is not configured.");
    }

    let response;
    try {
      response = await fetch(
        `https://generativelanguage.googleapis.com/v1beta/models/${MODEL}:generateContent`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            "x-goog-api-key": apiKey,
          },
          body: JSON.stringify({
            systemInstruction: {
              parts: [{
                text: `You are Master Gohari (استاد گوهری), the private AI English tutor for BH Self Development (خودسازی / BH Exclusive Academy).
Motto: از صفر شروع کن، تا تسلط کامل ادامه بده!
Be patient, kind, respectful and professional. Use a warm supportive tone.
If the learner writes in Dari/Persian, explain in Afghan Dari with clear English examples and pronunciation hints.
Correct English gently: praise effort, then explain corrections.
For learning responses, provide a short natural English answer, a faithful Afghan Dari explanation, one useful grammar/vocabulary tip when relevant, and one practice question suitable for the learner's level.
For self-development, habits and goals, give practical age-appropriate advice and one small actionable exercise.
Adapt to the supplied learner name and level. Never invent canned answers; generate a fresh response to every request.`
              }]
            },
            contents: [{ role: "user", parts: [{ text: prompt }] }],
            generationConfig: {
              temperature: 0.7,
              maxOutputTokens: 2048,
            },
          }),
        }
      );
    } catch (error) {
      console.error("Gemini network failure:", error);
      throw new HttpsError("unavailable", "Could not reach the online AI service.");
    }

    const payload = await response.json().catch(() => ({}));
    if (!response.ok) {
      const status = response.status;
      console.error("Gemini API rejected request:", status, payload?.error?.message || "No details");
      if (status === 429) {
        throw new HttpsError("resource-exhausted", "Gemini request quota or rate limit reached.");
      }
      if (status === 401 || status === 403) {
        throw new HttpsError("failed-precondition", "Gemini API key or API access is not valid.");
      }
      throw new HttpsError("unavailable", "Gemini did not complete the request.");
    }

    const text = (payload.candidates || [])
      .flatMap(candidate => candidate.content?.parts || [])
      .map(part => part.text || "")
      .join("")
      .trim();

    if (!text) {
      throw new HttpsError("internal", "Gemini returned an empty response.");
    }

    return { text, model: MODEL };
  }
);
