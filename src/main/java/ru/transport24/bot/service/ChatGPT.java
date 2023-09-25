package ru.transport24.bot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.transport24.bot.config.ChatGPTConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatGPT {
    final ChatGPTConfig chatGPTConfig;

    String askChatGPT(String message) {
        String url = chatGPTConfig.getGPTUrl();
        String apiKey = chatGPTConfig.getGPTApiKey();
        String model = chatGPTConfig.getGPTModel();

        try {
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Authorization", "Bearer " + apiKey);
            con.setRequestProperty("Content-Type", "application/json");

            String body = "{\"model\": \"" + model + "\", \"messages\": [{\"role\": \"user\", \"content\": \"" + message + "\"}]}";
            con.setDoOutput((true));
            OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
            writer.write(body);
            writer.flush();
            writer.close();

            BufferedReader in = new BufferedReader(new InputStreamReader((con.getInputStream())));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            return ("Вам отвечает искусственный интеллект:\n"
                    + response.toString()
                    .split("\"content\":\"")[1].split("\"},")[0])
                    .replaceAll("\\\\\"", " ")
                    .replaceAll("\\\\n", "\n");

        } catch (IOException e) {
            log.info("\nОшибка получения ответа от chatGPT => " + e);
            throw new RuntimeException(e);
        }
    }
}
