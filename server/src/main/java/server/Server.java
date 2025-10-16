package server;

import com.google.gson.Gson;
import dataaccess.MemoryDataAccess;
import datamodel.UserData;
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

    }

    private void register(Context ctx) {
        try {
            var serializer = new Gson();
            String reqJson = ctx.body();
            var user = serializer.fromJson(reqJson, UserData.class);

            var authData = userService.register(user);
        } catch (Exception ex) {
            var msg = String.format("{ \"message\": \"Error: %s\" }", ex.getMessage());
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
