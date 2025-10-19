package server;

import com.google.gson.Gson;
import dataaccess.MemoryDataAccess;
import datamodel.*;
import io.javalin.*;
import io.javalin.http.Context;
import service.*;

public class Server {

    private final Javalin javalin;
    private UserService userService;

    public Server() {
        var dataAccess = new MemoryDataAccess();
        userService = new UserService(dataAccess);

        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        // Register your endpoints and exception handlers here.
        javalin.post("/user", ctx -> register(ctx));
        javalin.delete("/db", ctx -> clear(ctx));
        javalin.post("/session", ctx -> login(ctx));
        javalin.delete("/session", ctx -> logout(ctx));
    }

    private void register(Context ctx) {
        try {
            var serializer = new Gson();
            String reqJson = ctx.body();
            var user = serializer.fromJson(reqJson, UserData.class);

            var authData = userService.register(user);

            ctx.status(200);
            ctx.result(String.format("{\"username\": \"%s\", \"authToken\": \"%s\"}",authData.username(),authData.authToken()));

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
        }
        catch (Exception ex){
            String msg = String.format("{ \"message\": \"Error: %s\" }", ex.getMessage());
            ctx.status(500);
            ctx.result(msg);
        }
        ctx.status(200);
        ctx.result("{}");
    }

    private void login(Context ctx) {
        try {
            var serializer = new Gson();
            String reqJson = ctx.body();
            var user = serializer.fromJson(reqJson, UserData.class);

            var authData = userService.login(user);

            ctx.status(200);
            ctx.result(String.format("{\"username\": \"%s\", \"authToken\": \"%s\"}",authData.username(),authData.authToken()));

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

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
