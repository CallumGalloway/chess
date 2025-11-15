package service;

import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;
import datamodel.*;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    @Test
    void registerPositive() throws Exception {
        DataAccess db = new MemoryDataAccess();
        var user = new UserData("new","password","new@new.com");
        var userService = new UserService(db);
        var authData = userService.register(user);
        assertNotNull(authData);
        assertEquals(user.username(),authData.username());
        assertTrue(!authData.authToken().isEmpty());
    }

    @Test
    void registerNegative() throws Exception {
        DataAccess db = new MemoryDataAccess();
        var user = new UserData("new", null, "new@new.com"); // Null password
        var userService = new UserService(db);

        Exception exception = assertThrows(Exception.class, () -> {
            userService.register(user);
        });
        assertEquals("bad request", exception.getMessage());

        var userGood = new UserData("new", "notnull", "new@new.com");
        userService.register(userGood);

        var userDup = new UserData("new", "password", "second@new.com");
        Exception exception2 = assertThrows(Exception.class, () -> {
            userService.register(userDup);
        });
        assertEquals("username already taken", exception2.getMessage());
    }

    @Test
    void loginPositive() throws Exception {
        DataAccess db = new MemoryDataAccess();
        var user = new UserData("new","password","new@new.com");
        var userService = new UserService(db);
        userService.register(user);
        var loginUser = new UserData("new", "password", null);
        var authData = userService.login(loginUser);
        assertNotNull(authData);
        assertEquals(user.username(), authData.username());
        assertNotNull(authData.authToken());
    }

    @Test
    void loginNegative() {
        DataAccess db = new MemoryDataAccess();
        var userService = new UserService(db);
        var loginUser = new UserData("", "password", null);

        Exception exception = assertThrows(Exception.class, () -> {
            userService.login(loginUser);
        });
        assertEquals("bad request", exception.getMessage());

        var loginUser2 = new UserData("nonexistent", "password", null);

        Exception exception2 = assertThrows(Exception.class, () -> {
            userService.login(loginUser2);
        });
        assertEquals("unauthorized", exception2.getMessage());
    }

    @Test
    void logoutPositive() throws Exception {
        DataAccess db = new MemoryDataAccess();
        var user = new UserData("new","password","new@new.com");
        var userService = new UserService(db);
        var authData = userService.register(user);
        assertNotNull(db.getAuthUser(authData.authToken()));
        userService.logout(authData.authToken());
        assertNull(db.getAuthUser(authData.authToken()));
    }

    @Test
    void logoutNegative() {
        DataAccess db = new MemoryDataAccess();
        var userService = new UserService(db);

        Exception exception = assertThrows(Exception.class, () -> {
            userService.logout("fakeAuthToken");
        });
        assertEquals("unauthorized", exception.getMessage());
    }

    @Test
    void listGamesPositive() throws Exception {
        DataAccess db = new MemoryDataAccess();
        var userService = new UserService(db);
        var gameService = new GameService(db);
        var authData = userService.register(new UserData("new", "password", "new@new.com"));
        db.addGame(new GameData(123, null, null, "testGame", null));
        HashMap games = gameService.listGames(authData.authToken());
        assertNotNull(games);
        assertNotNull(games.get("games"));
        assertEquals(1, ((java.util.Collection)games.get("games")).size());
    }

    @Test
    void listGamesNegativeUnauthorized() {
        DataAccess db = new MemoryDataAccess();
        var userService = new UserService(db);
        var gameService = new GameService(db);

        Exception exception = assertThrows(Exception.class, () -> {
            gameService.listGames("fakeAuthToken");
        });
        assertEquals("unauthorized", exception.getMessage());
    }

    @Test
    void addGamePositive() throws Exception {
        DataAccess db = new MemoryDataAccess();
        var userService = new UserService(db);
        var gameService = new GameService(db);
        var authData = userService.register(new UserData("new", "password", "new@new.com"));
        Integer games = gameService.addGame("newGame", authData.authToken());
        assertNotNull(games);
    }

    @Test
    void addGameNegative() throws Exception {
        DataAccess db = new MemoryDataAccess();
        var userService = new UserService(db);
        var gameService = new GameService(db);
        var authData = userService.register(new UserData("player", "pass", "p@p.com"));

        Exception exception = assertThrows(Exception.class, () -> {
            gameService.addGame(null, authData.authToken()); // Null game name
        });
        assertEquals("Bad Request", exception.getMessage());

        Exception exception2 = assertThrows(Exception.class, () -> {
            gameService.addGame("newGame", "fakeAuthToken");
        });
        assertEquals("unauthorized", exception2.getMessage());
    }

    @Test
    void joinGamePositive() throws Exception {
        DataAccess db = new MemoryDataAccess();
        var userService = new UserService(db);
        var gameService = new GameService(db);
        var authData = userService.register(new UserData("new", "password", "new@new.com"));
        Integer gameID = gameService.addGame("gameToJoin", authData.authToken());
        gameService.joinGame(gameID, "WHITE", authData.authToken());
        GameData game = db.getGameFromID(gameID);
        assertEquals("new", game.whiteUsername());
    }

    @Test
    void joinGameNegative() throws Exception {
        DataAccess db = new MemoryDataAccess();
        var userService = new UserService(db);
        var gameService = new GameService(db);
        var authData = userService.register(new UserData("new", "password", "new@new.com"));
        Integer gameID = gameService.addGame("gameToJoin", authData.authToken());
// bad auth
        Exception exception = assertThrows(Exception.class, () -> {
            gameService.joinGame(gameID, "WHITE", "fakeAuthToken");
        });
        assertEquals("unauthorized", exception.getMessage());
// bad color
        Exception exception2 = assertThrows(Exception.class, () -> {
            gameService.joinGame(gameID, "ORANGE", authData.authToken());
        });
        assertEquals("Bad Request", exception2.getMessage());
    }

    @Test
    void clearPositive() throws Exception {
        DataAccess db = new MemoryDataAccess();
        var user = new UserData("new","password","new@new.com");
        var userService = new UserService(db);
        var authData = userService.register(user);
        userService.clear();
        var check = db.getUser(user.username());
        assertNull(check);
    }
}