package dataaccess;

import com.google.gson.JsonObject;
import datamodel.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class MemoryDataAccess implements DataAccess {
    private final HashMap<String, UserData> users = new HashMap<>();
    private final HashMap<String, String> authentifier = new HashMap<>();
    private final HashMap<Integer, GameData> games = new HashMap();
    @Override
    public void clear() {
        users.clear();
        authentifier.clear();
    }

    @Override
    public void createUser(UserData user) {
        users.put(user.username(), user);
    }

    @Override
    public UserData getUser(String username) {
        return users.get(username);
    }

    @Override
    public void addAuth(AuthData auth) { authentifier.put(auth.authToken(), auth.username()); }

    @Override
    public String getAuthUser(String auth) { return authentifier.get(auth); }

    @Override
    public void delAuth(String auth) { authentifier.remove(auth); }

    @Override
    public HashMap<String, Collection> listGames() {
        HashMap<String, Collection> list = new HashMap<>();
        list.put("games",games.values());
        return list;
    }

    @Override
    public void addGame(GameData game) {
        games.put(game.gameID(),game);
    }
}
