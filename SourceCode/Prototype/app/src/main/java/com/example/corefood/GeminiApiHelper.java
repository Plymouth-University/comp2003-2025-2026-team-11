package com.example.corefood;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class GeminiApiHelper {

    public interface GeminiCallback {
        void onSuccess(String reply);
        void onError(Exception e);
    }

    private static final String API_KEY = "AIzaSyCXd1lsae5svv_MAMyZn9dELm0xVBKN72A";// Should be replaced with the Gemini API key

    private static final String[] MODEL_CHAIN = new String[]{
            "gemini-3.1-flash-lite-preview",
            "gemini-3-flash",
            "gemini-2.5-flash-lite",
            "gemini-2.5-flash"
    };

    private static final int CONNECT_TIMEOUT_MS = 15000;
    private static final int READ_TIMEOUT_MS = 30000;
    private static final int MAX_OUTPUT_TOKENS = 1024;
    private static final double TEMPERATURE = 0.5;

    public static void generateReply(String prompt, GeminiCallback callback) {
        new Thread(() -> {
            Exception lastException = null;

            try {
                if (API_KEY == null || API_KEY.trim().isEmpty()) {
                    throw new Exception("Gemini API key is missing.");
                }

                for (String modelName : MODEL_CHAIN) {
                    try {
                        String reply = tryModelWithRetry(modelName, prompt, 2);
                        if (reply != null && !reply.trim().isEmpty()) {
                            callback.onSuccess(reply.trim());
                            return;
                        }
                    } catch (Exception e) {
                        lastException = e;
                    }
                }

                if (lastException == null) {
                    lastException = new Exception("All Gemini models failed with unknown error.");
                }

                callback.onError(lastException);

            } catch (Exception e) {
                callback.onError(e);
            }
        }).start();
    }

    private static String tryModelWithRetry(String modelName, String prompt, int maxAttempts) throws Exception {
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return callGenerateContent(modelName, prompt);
            } catch (Exception e) {
                lastException = e;

                boolean shouldRetry = shouldRetry(e);
                boolean hasAnotherAttempt = attempt < maxAttempts;

                if (!shouldRetry || !hasAnotherAttempt) {
                    break;
                }

                try {
                    Thread.sleep(1200L * attempt);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    throw new Exception("Retry interrupted.", interruptedException);
                }
            }
        }

        throw lastException != null ? lastException : new Exception("Unknown model call failure.");
    }

    private static boolean shouldRetry(Exception e) {
        String message = e.getMessage();
        if (message == null) return false;

        String lower = message.toLowerCase();

        return lower.contains("http 429")
                || lower.contains("http 500")
                || lower.contains("http 502")
                || lower.contains("http 503")
                || lower.contains("http 504")
                || lower.contains("timeout")
                || lower.contains("timed out")
                || lower.contains("temporarily unavailable");
    }

    private static String callGenerateContent(String modelName, String prompt) throws Exception {
        HttpURLConnection connection = null;

        try {
            String endpoint =
                    "https://generativelanguage.googleapis.com/v1beta/models/"
                            + modelName
                            + ":generateContent?key="
                            + API_KEY.trim();

            URL url = new URL(endpoint);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);

            JSONObject textPart = new JSONObject();
            textPart.put("text", prompt);

            JSONArray partsArray = new JSONArray();
            partsArray.put(textPart);

            JSONObject userContent = new JSONObject();
            userContent.put("role", "user");
            userContent.put("parts", partsArray);

            JSONArray contentsArray = new JSONArray();
            contentsArray.put(userContent);

            JSONObject generationConfig = new JSONObject();
            generationConfig.put("temperature", TEMPERATURE);
            generationConfig.put("maxOutputTokens", MAX_OUTPUT_TOKENS);

            JSONObject root = new JSONObject();
            root.put("contents", contentsArray);
            root.put("generationConfig", generationConfig);

            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8)
            );
            writer.write(root.toString());
            writer.flush();
            writer.close();

            int responseCode = connection.getResponseCode();

            InputStream stream = (responseCode >= 200 && responseCode < 300)
                    ? connection.getInputStream()
                    : connection.getErrorStream();

            if (stream == null) {
                throw new Exception("Model " + modelName + " returned no response stream. HTTP " + responseCode);
            }

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(stream, StandardCharsets.UTF_8)
            );

            StringBuilder responseBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                responseBuilder.append(line);
            }
            reader.close();

            String responseText = responseBuilder.toString();

            if (responseCode < 200 || responseCode >= 300) {
                throw new Exception("Model " + modelName + " failed. HTTP " + responseCode + ": " + responseText);
            }

            JSONObject jsonResponse = new JSONObject(responseText);
            JSONArray candidates = jsonResponse.optJSONArray("candidates");

            if (candidates == null || candidates.length() == 0) {
                throw new Exception("Model " + modelName + " returned no candidates: " + responseText);
            }

            JSONObject firstCandidate = candidates.getJSONObject(0);

            JSONObject content = firstCandidate.optJSONObject("content");
            if (content == null) {
                throw new Exception("Model " + modelName + " returned no content object: " + responseText);
            }

            JSONArray parts = content.optJSONArray("parts");
            if (parts == null || parts.length() == 0) {
                throw new Exception("Model " + modelName + " returned no parts: " + responseText);
            }

            StringBuilder replyBuilder = new StringBuilder();

            for (int i = 0; i < parts.length(); i++) {
                JSONObject part = parts.getJSONObject(i);
                String text = part.optString("text", "");
                if (!text.isEmpty()) {
                    replyBuilder.append(text);
                }
            }

            String reply = replyBuilder.toString().trim();

            if (reply.isEmpty()) {
                throw new Exception("Model " + modelName + " returned empty text: " + responseText);
            }

            return reply;

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}