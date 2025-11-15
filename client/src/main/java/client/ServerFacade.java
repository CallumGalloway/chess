package client;

import chess.*;

import com.google.gson.Gson;

import java.net.*;
import java.net.http.*;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;

public class ServerFacade {

    private final HttpClient client = HttpClient.newHttpClient();
    private final String serverUrl;

    public ServerFacade(String url) {
        serverUrl = url;
    }

    public void login(String[] args) throws Exception {

    }

    public void logout() throws Exception {

    }

    public void listGames() throws Exception {

    }

    public ChessGame createGame() throws Exception {
        return new ChessGame();
    }

    public ChessGame joinGame() throws Exception {
        return new ChessGame();
    }

    public ChessGame retrieveGameData() throws Exception {
        return new ChessGame();
    }

    public void updateGameData() throws Exception {

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