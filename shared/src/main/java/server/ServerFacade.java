package server;

import com.google.gson.Gson;

import java.net.*;
import java.net.http.*;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Arrays;

import datamodel.*;

public class ServerFacade {

    private final HttpClient client = HttpClient.newHttpClient();
    private final String serverUrl;
    public State state;
    public String authToken;
    public Integer gameID;

    public ServerFacade(String url) {
        serverUrl = url;
        state = State.SIGNED_OUT;
        authToken = null;
        gameID = null;
    }

    public String login(String[] params) throws Exception {
        if (params.length >= 2) {
            var user = new UserData(params[0],params[1],null);
            var request = buildRequest("POST", "/session", user,null,null);
            var response = sendRequest(request);
            AuthData auth = handleResponse(response, AuthData.class);
            authToken = auth.authToken();
            state = State.SIGNED_IN;
            return "logged in!";
        }
        throw new Exception("Expected: <username> <password>\n");
    }

    public String register(String[] params) throws Exception {
        if (params.length >= 3) {
            var user = new UserData(params[0],params[1],params[2]);
            var request = buildRequest("POST", "/user", user, null,null);
            var response = sendRequest(request);
            AuthData auth = handleResponse(response, AuthData.class);
            authToken = auth.authToken();
            state = State.SIGNED_IN;
            return "registered!";
        }
        throw new Exception("Expected: <username> <password> <email>\n");
    }

    public String logout() throws Exception {
        var request = buildRequest("DELETE","/session",null,"authorization",authToken);
        var response = sendRequest(request);
        handleResponse(response,null);
        state = State.SIGNED_OUT;
        authToken = null;
        return "logged out!";
    }

    public GameList listGames() throws Exception {
        var request = buildRequest("GET","/game",null,"authorization",authToken);
        var response = sendRequest(request);
        GameList games = handleResponse(response, GameList.class);
        return games;
    }

    public String createGame(String[] params) throws Exception {
        if (params.length >= 1) {
            var gameName = new GameName(params[0]);
            if (!gameName.equals("")) {
                var request = buildRequest("POST", "/game", gameName, "authorization", authToken);
                var response = sendRequest(request);
                var game = handleResponse(response, GameData.class);
                return "game created: " + params[0] + ", with ID: " + game.gameID();
            } else throw new Exception("Bad Request");
        }
        throw new Exception("Expected: <NAME>\n");
    }

    public JoinData joinGame(String[] params) throws Exception {
        if (params.length >= 2) {
            int id = Integer.parseInt(params[0]);
            String color = params[1].toUpperCase();

            var gameList = listGames();
            GameData gameToJoin = findGame(id, gameList);

            gameID = gameToJoin.gameID();

            JoinData joinData = null;
            if (color.toUpperCase().equals("WHITE") || color.toUpperCase().equals("BLACK")) {
                joinData = new JoinData(color,gameToJoin.gameID());
            } else {
                return new JoinData("Observer",gameToJoin.gameID());
            }

            var request = buildRequest("PUT","/game",joinData,"authorization",authToken);
            var response = sendRequest(request);
            handleResponse(response, null);

            state = State.IN_GAME;
            return joinData;
        }
        throw new Exception("Expected: <NUMBER> <WHITE/BLACK>");

    }

    public GameData findGame(int id, GameList gameList) throws Exception {
        var list = gameList.games();
        GameData gameFound = null;
        if (id > list.size()) {
            for (int game = 0; game < list.size(); game++) {
                if (list.get(game).gameID() == id){
                    gameFound = list.get(game);
                    return gameFound;
                }
            }
        } else {
            gameFound = list.get(id-1);
            return gameFound;
        }
        throw new Exception("game not found.");
    }

    public JoinData observeGame(String[] params) throws Exception {
        String[] observerParams = Arrays.copyOf(params, params.length +1);
        observerParams[observerParams.length - 1] = "OBSERVER";
        state = State.OBSERVING;
        return joinGame(observerParams);
    }

    public String updateGameData() throws Exception {
        return "";
    }

    public void clear() throws Exception {
        var request = buildRequest("DELETE", "/db", null, null, null);
        var response = sendRequest(request);
        handleResponse(response, null);
    }

    private HttpRequest buildRequest(String method, String path, Object body, String header, String value) {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + path))
                .method(method, makeRequestBody(body));
        if (header != null) {
            request.setHeader(header, value);
        }
        else if (body != null) {
            request.setHeader("placeholder","unused");
        }
        return request.build();
    }

    private BodyPublisher makeRequestBody(Object request) {
        if (request != null) {
            return BodyPublishers.ofString(new Gson().toJson(request));
        } else {
            return BodyPublishers.noBody();
        }
    }

    private HttpResponse<String> sendRequest(HttpRequest request) throws Exception {
        try {
            return client.send(request, BodyHandlers.ofString());
        } catch (Exception ex) {
            throw new Exception(ex.getMessage());
        }
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