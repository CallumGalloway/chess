package client;

import chess.*;

import com.google.gson.Gson;

import java.net.*;
import java.net.http.*;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Arrays;

public class ServerFacade {

    private final HttpClient client = HttpClient.newHttpClient();
    private final String serverUrl;
    public State state;

    public ServerFacade(String url) {
        serverUrl = url;
        state = State.SIGNED_OUT;
    }

    public String login(String[] args) throws Exception {
        state = State.SIGNED_IN;
        return "logged in!";
    }

    public String register(String[] args) throws Exception {
        state = State.SIGNED_IN;
        return "registered!";
    }

    public String logout() throws Exception {
        state = State.SIGNED_OUT;
        return "logged out!";
    }

    public String listGames() throws Exception {
        return "games!";
    }

    public String createGame(String[] args) throws Exception {
        return "game made heeheehoohoo";
    }

    public String joinGame(String[] args) throws Exception {
        return "game joined heeheehoohoo";
    }

    public String observeGame(String[] args) throws Exception {
        String[] observerArgs = Arrays.copyOf(args, args.length +1);
        observerArgs[observerArgs.length - 1] = "observer";
        return joinGame(observerArgs);
    }

    public String retrieveGameData(String[] args) throws Exception {
        return "game data heeheehoohoo";
    }

    public String updateGameData() throws Exception {
        return "";
    }

    private <T> T handleResponse(HttpResponse<String> response, Class<T> responseClass) throws Exception {
        var status = response.statusCode();
        if (!isSuccessful(status)) {
            var body = response.body();
            if (body != null) {
                throw new Exception(body);
            }
            throw new Exception("other failure: " + status);
        }

        if (responseClass != null) {
            return new Gson().fromJson(response.body(), responseClass);
        }

        return null;
    }

    private boolean isSuccessful(int status) {
        return status / 100 == 2;
    }
}