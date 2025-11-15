package server;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.*;
import datamodel.*;
import io.javalin.*;
import io.javalin.http.Context;
import service.*;

import java.util.HashMap;

public class Server {

    private final Javalin javalin;
    private UserService userService;
    private GameService gameService;

    public Server() {
        DataAccess dataAccess;
        try {
            dataAccess = new SQLDataAccess();
            System.out.println("SQL connection successful");
        } catch (Exception ex) {
//            dataAccess = new MemoryDataAccess();
//            System.out.println("SQL connection failed, using memory");
            throw new RuntimeException(String.format("SQL connection failed, %s",ex));
        }
        userService = new UserService(dataAccess);

        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        // Register your endpoints and exception handlers here.
        javalin.post("/user", ctx -> register(ctx));
        javalin.delete("/db", ctx -> clear(ctx));
        javalin.post("/session", ctx -> login(ctx));
        javalin.delete("/session", ctx -> logout(ctx));
        javalin.get("/game",ctx -> listGames(ctx));
        javalin.post("/game",ctx-> createGame(ctx));
        javalin.put("/game",ctx -> joinGame(ctx));
    }

    private void register(Context ctx) {
        try {
            var serializer = new Gson();
            String reqJson = ctx.body();
            var user = serializer.fromJson(reqJson, UserData.class);

            var authData = userService.register(user);

            ctx.status(200);
            ctx.result(String.format("{\"username\": \"%s\", \"authToken\": \"%s\"}",authData.username(),authData.authToken()));

        } catch (DataAccessException ex) {
            String msg = String.format("{ \"message\": \"Error: %s\" }", ex.getMessage());
            ctx.status(500);
            ctx.result(msg);
        } catch (Exception ex) {
            String msg = String.format("{ \"message\": \"Error: %s\" }", ex.getMessage());
            if (msg.contains("username already taken")) {
                ctx.status(403);
                ctx.result(msg);
            }
            else {
                ctx.status(400);
                ctx.result(msg);
            }
        }
    }

    private void clear(Context ctx) {
        try {
            userService.clear();
            ctx.status(200);
            ctx.result("{}");
        } catch (DataAccessException ex) {
            String msg = String.format("{ \"message\": \"Error: %s\" }", ex.getMessage());
            ctx.status(500);
            ctx.result(msg);
        } catch (Exception ex){
            String msg = String.format("{ \"message\": \"Error: %s\" }", ex.getMessage());
            ctx.status(500);
            ctx.result(msg);
        }
    }

    private void login(Context ctx) {
        try {
            var serializer = new Gson();
            String reqJson = ctx.body();
            var user = serializer.fromJson(reqJson, UserData.class);

            var authData = userService.login(user);

            ctx.status(200);
            ctx.result(String.format("{\"username\": \"%s\", \"authToken\": \"%s\"}",authData.username(),authData.authToken()));

        } catch (DataAccessException ex) {
            String msg = String.format("{ \"message\": \"Error: %s\" }", ex.getMessage());
            ctx.status(500);
            ctx.result(msg);
        } catch (Exception ex) {
            String msg = String.format("{ \"message\": \"Error: %s\" }", ex.getMessage());
            if (msg.contains("unauthorized")) {
                ctx.status(401);
                ctx.result(msg);
            }
            else {
                ctx.status(400);
                ctx.result(msg);
            }
        }
    }

    private void logout(Context ctx) {
        try {
            var auth = ctx.header("authorization");

            userService.logout(auth);

            ctx.status(200);
            ctx.result("{}");

        } catch (DataAccessException ex) {
            String msg = String.format("{ \"message\": \"Error: %s\" }", ex.getMessage());
            ctx.status(500);
            ctx.result(msg);
        } catch (Exception ex) {
            String msg = String.format("{ \"message\": \"Error: %s\" }", ex.getMessage());
            if (msg.contains("unauthorized")) {
                ctx.status(401);
                ctx.result(msg);
            }
            else {
                ctx.status(400);
                ctx.result(msg);
            }
        }
    }

    private void listGames(Context ctx) {
        try {
            var auth = ctx.header("authorization");

            HashMap list = gameService.listGames(auth);

            var serializer = new Gson();
            String send = serializer.toJson(list);

            ctx.status(200);
            ctx.result(send);

        } catch (DataAccessException ex) {
            String msg = String.format("{ \"message\": \"Error: %s\" }", ex.getMessage());
            ctx.status(500);
            ctx.result(msg);
        } catch (Exception ex) {
            String msg = String.format("{ \"message\": \"Error: %s\" }", ex.getMessage());
            if (msg.contains("unauthorized")) {
                ctx.status(401);
                ctx.result(msg);
            }
            else {
                ctx.status(400);
                ctx.result(msg);
            }
        }
    }

    private void createGame(Context ctx) {
        try {
            var auth = ctx.header("authorization");
            var serializer = new Gson();
            String reqJson = ctx.body();
            var gameName = serializer.fromJson(reqJson, GameName.class);
            String name = gameName.gameName();

            var gameID = gameService.addGame(name, auth);
            // var formatted = String.format("{\"gameID\": %d}", gameID);
            var formatted = new HashMap<String, Integer>();
            formatted.put("gameID",gameID);
            var retVar = serializer.toJson(formatted);

            ctx.status(200);
            ctx.result(retVar);
        } catch (DataAccessException ex) {
            String msg = String.format("{ \"message\": \"Error: %s\" }", ex.getMessage());
            ctx.status(500);
            ctx.result(msg);
        } catch (Exception ex) {
            String msg = String.format("{ \"message\": \"Error: %s\" }", ex.getMessage());
            if (msg.contains("unauthorized")) {
                ctx.status(401);
                ctx.result(msg);
            }
            else {
                ctx.status(400);
                ctx.result(msg);
            }
        }
    }

    private void joinGame(Context ctx) {
        try {
            var auth = ctx.header("authorization");
            var serializer = new Gson();
            String reqJson = ctx.body();
            var gameInfo = serializer.fromJson(reqJson, JoinData.class);
            Integer gameID = gameInfo.gameID();
            String color = gameInfo.playerColor();

            gameService.joinGame(gameID, color, auth);

            ctx.status(200);
            ctx.result("{}");

        } catch (DataAccessException ex) {
            String msg = String.format("{ \"message\": \"Error: %s\" }", ex.getMessage());
            ctx.status(500);
            ctx.result(msg);
        } catch (Exception ex) {
            String msg = String.format("{ \"message\": \"Error: %s\" }", ex.getMessage());
            if (msg.contains("unauthorized")) {
                ctx.status(401);
                ctx.result(msg);
            } else if (msg.contains("already taken")) {
                ctx.status(403);
                ctx.result(msg);
            } else {
                ctx.status(400);
                ctx.result(msg);
            }
        }
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
