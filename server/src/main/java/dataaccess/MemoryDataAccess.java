package dataaccess;

import datamodel.AuthData;
import datamodel.UserData;

import java.util.HashMap;

public class MemoryDataAccess implements DataAccess {
    private final HashMap<String, UserData> users = new HashMap<>();
    private final HashMap<String, String> authentifier = new HashMap<>();
    @Override
    public void clear() {
        users.clear();
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
}
