package com.example;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
class Prediction {
    String label;
    double score;
}
public class test {
    private static final int MAX_RETRIES = 3;
    private static final int RETRY_INTERVAL_MS = 1000;

    public static void main(String[] args) {
        try {
            String apiUrl = "https://api-inference.huggingface.co/models/cardiffnlp/twitter-roberta-base-dec2021-tweet-topic-multi-all";
            String question = "Cloud computing, a revolutionary paradigm in technology, enables the storage, management, and access of data and applications over the internet. By leveraging remote servers hosted on the \"cloud,\" users can enjoy scalability, flexibility, and cost-effectiveness in their computing needs. From individual users to large enterprises, cloud computing has transformed the way we store and process information, offering convenience and efficiency in the digital age"
                    +"Give the topic of the text";

            String response = sendRequest(apiUrl, question);
//            String[]response1=response.split(",");
            JsonArray jsonArray = JsonParser.parseString(response).getAsJsonArray();
            Gson gson = new Gson();
            JsonElement predictionsElement = jsonArray.get(0);
            Prediction[] predictions = gson.fromJson(predictionsElement, Prediction[].class);

// Find the label with the maximum score
            String maxLabel = "";
            double maxScore = Double.MIN_VALUE;
            for (Prediction prediction : predictions) {
                if (prediction.score > maxScore) {
                    maxScore = prediction.score;
                    maxLabel = prediction.label;
                }
            }

            System.out.println("Response: " + maxLabel);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String sendRequest(String apiUrl, String question) throws IOException {
        int retries = 0;
        while (retries < MAX_RETRIES) {
            try {
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//                connection.setRequestMethod("GET");
//                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Authorization", "Bearer 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");
                connection.setDoOutput(true);

                String requestBody = "{\"inputs\": \"" + question + "\"}";
                try (OutputStream outputStream = connection.getOutputStream()) {
                    outputStream.write(requestBody.getBytes());
                }

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        return response.toString();
                    }
                } else if (responseCode == HttpURLConnection.HTTP_UNAVAILABLE) {
                    // Server is temporarily unavailable, retry after interval
                    System.out.println("Server temporarily unavailable, retrying...");
                    Thread.sleep(RETRY_INTERVAL_MS);
                    retries++;
                } else {
                    throw new IOException("HTTP error: " + responseCode);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        throw new IOException("Maximum retries exceeded");
    }
}