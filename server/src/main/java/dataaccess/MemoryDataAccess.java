package dataaccess;

import datamodel.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class MemoryDataAccess implements DataAccess {
    private final HashMap<String, UserData> users = new HashMap<>();
    private final HashMap<String, String> authentifier = new HashMap<>();
    private final HashMap<Integer, GameData> games = new HashMap();
    @Override
    public void clear() throws DataAccessException {
        users.clear();
        authentifier.clear();
        games.clear();
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        users.put(user.username(), user);
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        return users.get(username);
    }

    @Override
    public void addAuth(AuthData auth) throws DataAccessException { authentifier.put(auth.authToken(), auth.username()); }

    @Override
    public String getAuthUser(String auth) throws DataAccessException { return authentifier.get(auth); }

    @Override
    public void delAuth(String auth) throws DataAccessException { authentifier.remove(auth); }

    @Override
    public GameList listGames() throws DataAccessException {
        ArrayList<GameData> list = new ArrayList<>();
        list.addAll(games.values());
        return new GameList(list);
    }

    @Override
    public void addGame(GameData game) throws DataAccessException {
        games.put(game.gameID(),game);
    }

    @Override
    public void joinGame(Integer gameID, String color, String auth) throws DataAccessException {
        var game = games.get(gameID);
        String user = getAuthUser(auth);
        if (color.equals("WHITE") && game.whiteUsername() == null){
            GameData updated = new GameData(gameID, user, game.blackUsername(), game.gameName(), game.game());
            games.put(gameID, updated);
        } else if (color.equals("BLACK") && game.blackUsername() == null) {
            GameData updated = new GameData(gameID, game.whiteUsername(), user, game.gameName(), game.game());
            games.put(gameID, updated);
        } else {
            throw new DataAccessException("already taken");
        }

    }

    public GameData getGameFromID(Integer gameID) throws DataAccessException {
        return games.get(gameID);
    }
}
