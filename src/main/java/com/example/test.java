package com.example;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class test {
    private static final int MAX_RETRIES = 3;
    private static final int RETRY_INTERVAL_MS = 1000;

    public static void main(String[] args) {
        try {
            String apiUrl = "https://api-inference.huggingface.co/models/czearing/article-title-generator";
            String question = "The cloud refers to a network of remote servers hosted on the internet, offering various services and resources to users on-demand. Cloud services encompass a wide range of offerings, including computing power, storage, networking, databases, analytics, and more. These services are typically provided on a pay-as-you-go basis, allowing users to scale resources up or down as needed without the hassle of managing physical infrastructure. Cloud computing enables businesses to innovate rapidly, improve agility, and reduce costs by outsourcing IT infrastructure and leveraging flexible and scalable solutions tailored to their specific requirements."
                    +"Give the titel of the text";

            String response = sendRequest(apiUrl, question);
            System.out.println("Response: " + response);
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