package dataaccess;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.HashMap;

import datamodel.*;

import static org.junit.jupiter.api.Assertions.*;

class SQLDataAccessTest {
    @BeforeEach
    void setup() throws Exception {
        DataAccess db = new SQLDataAccess();
        db.clear();
    }

    @Test
    void clear() throws Exception {
        DataAccess db = new SQLDataAccess();
        db.createUser(new UserData("new", "password", "new@new.com"));
        db.clear();
        assertNull(db.getUser("new"));
    }

    @Test
    void createUser() throws Exception {
        DataAccess db = new SQLDataAccess();
        var user = new UserData("new", "password", "new@new.com");
        db.createUser(user);
        assertEquals(user, db.getUser(user.username()));
    }

    @Test
    void getUser() throws Exception {
        DataAccess db = new SQLDataAccess();
        var user = new UserData("new", "password", "new@new.com");
        db.createUser(user);
        UserData results = db.getUser("new");
        assertNotNull(results);
        assertEquals(user, results);
    }

    @Test
    void getUserFail() throws Exception {
        DataAccess db = new SQLDataAccess();
        var user = new UserData("new", "password", "new@new.com");
        db.createUser(user);
        UserData results = db.getUser("notAUser");
        assertNull(results);
    }

    @Test
    void addAuth() throws Exception {
        DataAccess db = new SQLDataAccess();
        var user = new UserData("new", "password", "new@new.com");
        db.createUser(user);
        var auth = new AuthData("new", "token123");

        db.addAuth(auth);

        assertEquals("new", db.getAuthUser("token123"));
    }

    @Test
    void addAuthFail() throws Exception {
        DataAccess db = new SQLDataAccess();
        var user = new UserData("new", "password", "new@new.com");

        var auth1 = new AuthData("new", "token123");
        db.addAuth(auth1);

        var auth2 = new AuthData("newer", "token123");

        assertThrows(DataAccessException.class, () -> db.addAuth(auth2));
    }

    @Test
    void getAuthUser() throws Exception {
        DataAccess db = new SQLDataAccess();
        var user = new UserData("new", "password", "new@new.com");
        db.createUser(user);
        var auth = new AuthData("new", "token123");
        db.addAuth(auth);

        String retrievedUser = db.getAuthUser("token123");

        assertEquals("new", retrievedUser);
    }

    @Test
    void getAuthUserFail() throws Exception {
        DataAccess db = new SQLDataAccess();
        String retrievedUser = db.getAuthUser("nonExistentToken");
        assertNull(retrievedUser);
    }

    @Test
    void delAuth() throws Exception {
        DataAccess db = new SQLDataAccess();
        var user = new UserData("new", "password", "new@new.com");
        db.createUser(user);
        var auth = new AuthData("new", "token123");
        db.addAuth(auth);
        assertNotNull(db.getAuthUser("token123"));
        db.delAuth("token123");
        assertNull(db.getAuthUser("token123"));
    }

    @Test
    void listGames() throws Exception {
        DataAccess db = new SQLDataAccess();
        var game1 = new GameData(1234, null, null, "Game One", null);
        var game2 = new GameData(5678, null, null, "Game Two", null);
        db.addGame(game1);
        db.addGame(game2);
        var gameList = db.listGames();
        assertNotNull(gameList);
        var games = (Collection<GameData>) gameList.list();
        assertEquals(2, games.size());
        var names = games.stream().map(GameData::gameName).toList();
        assertTrue(names.contains("Game One"));
        assertTrue(names.contains("Game Two"));
    }

    @Test
    void listGamesEmpty() throws Exception {
        DataAccess db = new SQLDataAccess();
        GameList gameList = db.listGames();
        assertNotNull(gameList);
        Collection games = gameList.list();
        assertTrue(games.isEmpty());
    }

    @Test
    void addGame() throws Exception {
        DataAccess db = new SQLDataAccess();
        var game = new GameData(1234, null, null, "My Game", null);
        db.addGame(game);
        var games = (Collection<GameData>) db.listGames().list();
        assertEquals(1, games.size());
        GameData retrievedGame = games.iterator().next();
        assertEquals("My Game", retrievedGame.gameName());
        assertNotNull(retrievedGame.gameID());
    }

    @Test
    void joinGame() throws Exception {
        DataAccess db = new SQLDataAccess();
        db.createUser(new UserData("new", "password", "new@new.com"));
        var auth1 = new AuthData("new", "authToken1");
        db.addAuth(auth1);
        db.createUser(new UserData("newer", "password", "new@new.com"));
        var auth2 = new AuthData("newer", "authToken2");
        db.addAuth(auth2);
        var game = new GameData(1234, null, null, "Empty Game", null);
        db.addGame(game);
        var games = (Collection<GameData>) db.listGames().list();
        assertNotNull(1234);
        db.joinGame(1234, "WHITE", "authToken1");
        var updatedGames = (Collection<GameData>) db.listGames().list();
        GameData updatedGame = updatedGames.iterator().next();
        assertEquals("new", updatedGame.whiteUsername());
        assertNull(updatedGame.blackUsername());
        db.joinGame(1234, "BLACK", "authToken2");
        var finalGames = (Collection<GameData>) db.listGames().list();
        GameData finalGame = finalGames.iterator().next();
        assertEquals("new", finalGame.whiteUsername());
        assertEquals("newer", finalGame.blackUsername());
    }

    @Test
    void joinGameFail() throws Exception {
        DataAccess db = new SQLDataAccess();
        db.createUser(new UserData("new", "password", "new@new.com"));
        var auth1 = new AuthData("new", "authToken1");
        db.addAuth(auth1);
        db.createUser(new UserData("newer", "password", "new@new.com"));
        var auth2 = new AuthData("newer", "authToken2");
        db.addAuth(auth2);
        var game = new GameData(1234, null, null, "Test Game", null);
        db.addGame(game);
        var games = (Collection<GameData>) db.listGames().list();
        db.joinGame(1234, "WHITE", "authToken1");
        assertThrows(DataAccessException.class, () -> {
            db.joinGame(1234, "WHITE", "authToken2");
        });
    }
}
