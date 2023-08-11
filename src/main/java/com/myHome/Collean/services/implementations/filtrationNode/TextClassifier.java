package com.myHome.Collean.services.implementations.filtrationNode;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.myHome.Collean.constants.FiltrationNodeConstants;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Path;
import java.util.*;

@Component
public class TextClassifier {

    private final Set<String> badWords;

    private void init(){
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(Path.of(FiltrationNodeConstants.BAD_WORDS_PATH).toFile()));
            while (bufferedReader.ready()){
                String[] parts = bufferedReader.readLine().split(",");
                for(String part:parts) badWords.add(part.toLowerCase());
            }
        }catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }
    }

    TextClassifier(){
        badWords = new HashSet<>();
        init();
    }

    private boolean hasNetConnection(){
        try (Socket socket = new Socket()) {
            int timeout = 3000;
            InetSocketAddress address = new InetSocketAddress("8.8.8.8", 53);
            socket.connect(address, timeout);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private boolean checkCommentOffline(String message){
        // Check offline Not efficient
        String[] parts = message.split(" ");
        int noOfWords = parts.length;
        int noOfBadWords = 0;
        for(String word:parts)
            if(badWords.contains(word)) noOfBadWords++;
        return (Double.compare(((noOfBadWords * 100.0)/noOfWords),7.0)<0);
    }
    @SuppressWarnings("deprecation")
    private Map<String,String> checkComment(String message){
        try {
            String command = "node " + FiltrationNodeConstants.FILTER_SCRIPT_PATH +
                    " "+ message +
                    " "+ FiltrationNodeConstants.PERSISTENCE_DISCOVERY_URL +
                    " "+ FiltrationNodeConstants.PERSISTENCE_API_KEY;
            Process process = Runtime.getRuntime().exec(command);
            if (process.waitFor() == 0) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                StringBuilder output = new StringBuilder();
                while (reader.ready()) output.append(reader.readLine());
                return new Gson().fromJson(
                        output.toString(),
                        new TypeToken<Map<String, String>>() {}.getType()
                );
            } else return Map.of("success","false");
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("success","false");
        }
    }
    public boolean isGoodComment(String message){
        if(hasNetConnection()) {
            Map<String, String> results = checkComment(message);
            if (results.get("success").equals("true")) {
                if (Double.compare(
                        Double.parseDouble(results.get(FiltrationNodeConstants.ClassificationConstants.IDENTITY_ATTACK)), 0.6
                ) > 0) return false;
                if (Double.compare(
                        Double.parseDouble(results.get(FiltrationNodeConstants.ClassificationConstants.INSULT)), 0.6
                ) > 0) return false;
                if (Double.compare(
                        Double.parseDouble(results.get(FiltrationNodeConstants.ClassificationConstants.THREAT)), 0.5
                ) > 0) return false;
                if (Double.compare(
                        Double.parseDouble(results.get(FiltrationNodeConstants.ClassificationConstants.PROFANITY)), 0.5
                ) > 0) return false;
                if (Double.compare(
                        Double.parseDouble(results.get(FiltrationNodeConstants.ClassificationConstants.SEVERE_TOXICITY)), 0.4
                ) > 0) return false;
                if (Double.compare(
                        Double.parseDouble(results.get(FiltrationNodeConstants.ClassificationConstants.SEXUALLY_EXPLICIT)), 0.4
                ) > 0) return false;
                if (Double.compare(
                        Double.parseDouble(results.get(FiltrationNodeConstants.ClassificationConstants.TOXICITY)), 0.6
                ) > 0) return false;
                return true;
            }
        }
        return checkCommentOffline(message);
    }
}
