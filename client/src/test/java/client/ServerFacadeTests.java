package client;

import datamodel.*;
import org.junit.jupiter.api.*;
import server.Server;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ServerFacadeTests {

    private static Server server;
    private static String serverUrl;
    private ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        serverUrl = "http://localhost:" + port;
        System.out.println("Started test HTTP server on " + port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    public void setup() throws Exception {
        facade = new ServerFacade(serverUrl);
        facade.clear();
    }


    @Test
    public void registerSuccess() {
        Assertions.assertDoesNotThrow(() -> {
            String result = facade.register(new String[]{"new", "new", "new@new.com"});
            assertEquals("registered!", result);
            assertEquals(State.SIGNED_IN, facade.state);
        });
    }

    @Test
    public void registerFailureExistingUser() throws Exception {
        facade.register(new String[]{"new2", "new", "new@new.com"});
        Exception exception = Assertions.assertThrows(Exception.class, () -> {
            facade.register(new String[]{"new2", "new", "new@new.com"});
        });
        assertTrue(exception.getMessage().contains("username already taken"));
    }

    @Test
    public void registerFailureBadRequest() {
        Exception exception = Assertions.assertThrows(Exception.class, () -> {
            facade.register(new String[]{"new3", "new"}); // Missing email
        });

        assertEquals("Expected: <username> <password> <email>\n", exception.getMessage());
    }

    @Test
    public void loginSuccess() throws Exception {
        facade.register(new String[]{"new", "new", "new@new.com"});
        facade.logout();

        String result = facade.login(new String[]{"new", "new"});
        assertEquals("logged in!", result);
        assertEquals(State.SIGNED_IN, facade.state);
    }

    @Test
    public void loginFailureBadPassword() throws Exception {
        facade.register(new String[]{"new2", "new", "new@new.com"});
        facade.logout();

        Exception exception = Assertions.assertThrows(Exception.class, () -> {
            facade.login(new String[]{"new2", "badpass"});
        });

        assertTrue(exception.getMessage().contains("unauthorized"));
    }

    @Test
    public void loginFailureBadUser() {
        Exception exception = Assertions.assertThrows(Exception.class, () -> {
            facade.login(new String[]{"new", "new"});
        });

        assertTrue(exception.getMessage().contains("unauthorized"));
    }

    @Test
    public void logoutSuccess() throws Exception {
        facade.register(new String[]{"new", "new", "new@new.com"});

        String result = facade.logout();
        assertEquals("logged out!", result);
        assertEquals(State.SIGNED_OUT, facade.state);
    }

    @Test
    public void listGamesSuccessEmpty() throws Exception {
        facade.register(new String[]{"new", "new", "new@new.com"});

        GameList gameList = facade.listGames();
        assertNotNull(gameList);
        assertEquals(0, gameList.list().size());
    }

    @Test
    public void listGamesSuccessNotEmpty() throws Exception {
        facade.register(new String[]{"new2", "new", "new@new.com"});
        facade.createGame(new String[]{"Game 1"});
        facade.createGame(new String[]{"Game 2"});

        GameList gameList = facade.listGames();
        assertNotNull(gameList);
        assertEquals(2, gameList.list().size());
    }

    @Test
    public void createGameSuccess() throws Exception {
        facade.register(new String[]{"new", "new", "new@new.com"});

        String result = facade.createGame(new String[]{"My New Game"});
        assertTrue(result.startsWith("game created: My New Game, with ID: "));

        GameList gameList = facade.listGames();
        assertEquals(1, gameList.list().size());
        assertEquals("My New Game", gameList.list().get(0).gameName());
    }

    @Test
    public void createGameFailure() {
        Exception exception = Assertions.assertThrows(Exception.class, () -> {
            facade.createGame(new String[]{"Test Game"});
        });
    }

    @Test
    public void createGameFailureBadRequest() throws Exception {
        facade.register(new String[]{"new", "new", "new@new.com"});

        Exception exception = Assertions.assertThrows(Exception.class, () -> {
            facade.createGame(new String[]{""});
        });
    }

    @Test
    public void joinGameSuccess() throws Exception {
        facade.register(new String[]{"new", "new", "new@new.com"});
        facade.createGame(new String[]{"GameToJoin"});
        GameList games = facade.listGames();
        int gameID = games.list().get(0).gameID();
        facade.logout();

        ServerFacade facade2 = new ServerFacade(serverUrl);
        facade2.login(new String[]{"new", "new"});
        JoinData joinData = facade2.joinGame(new String[]{String.valueOf(gameID), "WHITE"});

        assertEquals("WHITE", joinData.playerColor());
        assertEquals(gameID, joinData.gameID());
    }

    @Test
    public void observeGameSuccess() throws Exception {
        facade.register(new String[]{"new", "new", "new@new.com"});
        facade.createGame(new String[]{"GameToObserve"});
        GameList games = facade.listGames();
        int gameID = games.list().get(0).gameID();
        facade.logout();

        ServerFacade facade2 = new ServerFacade(serverUrl);
        facade2.login(new String[]{"new", "new"});

        JoinData joinData = facade2.observeGame(new String[]{String.valueOf(gameID)});

        assertEquals("Observer", joinData.playerColor());
        assertEquals(gameID, joinData.gameID());
    }

    @Test
    public void joinGameFailureBadID() throws Exception {
        facade.register(new String[]{"new", "new", "new@new.com"});

        Exception exception = Assertions.assertThrows(Exception.class, () -> {
            facade.joinGame(new String[]{"99999", "WHITE"});
        });

        assertTrue(exception.getMessage().contains("game not found"));
    }

    @Test
    public void joinGameFailureSlotTaken() throws Exception {
        facade.register(new String[]{"whitenew", "new", "new@new.com"});
        facade.createGame(new String[]{"Full Game"});
        int gameID = facade.listGames().list().get(0).gameID();
        facade.joinGame(new String[]{String.valueOf(gameID), "WHITE"});
        facade.logout();

        ServerFacade facade2 = new ServerFacade(serverUrl);
        facade2.register(new String[]{"blacknew", "new", "new@new.com"});

        Exception exception = Assertions.assertThrows(Exception.class, () -> {
            facade2.joinGame(new String[]{String.valueOf(gameID), "WHITE"});
        });

        assertTrue(exception.getMessage().contains("already taken"));
    }

    @Test
    public void findGameSuccess() throws Exception {
        GameData game1 = new GameData(101, "UserA", "UserB", "Game 1", null);
        GameData game2 = new GameData(102, "UserC", "UserD", "Game 2", null);
        ArrayList arrayList = new ArrayList();
        arrayList.add(game1);
        arrayList.add(game2);
        GameList gameList = new GameList(arrayList);

        GameData foundGame = facade.findGame(102, gameList);
        assertEquals(102, foundGame.gameID());
        assertEquals("Game 2", foundGame.gameName());
    }

    @Test
    public void findGameFailure() {
        GameData game1 = new GameData(101, "UserA", "UserB", "Game 1", null);
        ArrayList arrayList = new ArrayList();
        arrayList.add(game1);
        GameList gameList = new GameList(arrayList);

        Exception exception = Assertions.assertThrows(Exception.class, () -> {
            facade.findGame(999, gameList);
        });

        assertEquals("game not found.", exception.getMessage());
    }
}