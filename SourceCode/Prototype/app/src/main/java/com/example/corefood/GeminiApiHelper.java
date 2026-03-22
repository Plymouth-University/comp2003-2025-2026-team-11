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

    public static void generateReply(String prompt, GeminiCallback callback) {
        new Thread(() -> {
            HttpURLConnection connection = null;

            try {
                String apiKey = "COREFOOD API KEY"; // Replace with the API key to work

                if (apiKey == null || apiKey.trim().isEmpty()) {
                    throw new Exception("Gemini API key is missing.");
                }

                String endpoint =
                        "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-flash-lite-preview:generateContent?key="
                                + apiKey.trim();

                URL url = new URL(endpoint);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                connection.setRequestProperty("Accept", "application/json");
                connection.setDoOutput(true);
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(20000);

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
                generationConfig.put("temperature", 0.5);
                generationConfig.put("maxOutputTokens", 512);

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
                    throw new Exception("No response stream. HTTP " + responseCode);
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
                    throw new Exception("HTTP " + responseCode + ": " + responseText);
                }

                JSONObject jsonResponse = new JSONObject(responseText);
                JSONArray candidates = jsonResponse.optJSONArray("candidates");

                if (candidates == null || candidates.length() == 0) {
                    throw new Exception("No candidates in response: " + responseText);
                }

                JSONObject firstCandidate = candidates.getJSONObject(0);
                JSONObject content = firstCandidate.optJSONObject("content");

                if (content == null) {
                    throw new Exception("No content object in response: " + responseText);
                }

                JSONArray parts = content.optJSONArray("parts");

                if (parts == null || parts.length() == 0) {
                    throw new Exception("No parts in response: " + responseText);
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
                    throw new Exception("Empty text reply: " + responseText);
                }

                callback.onSuccess(reply);

            } catch (Exception e) {
                callback.onError(e);
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }
}