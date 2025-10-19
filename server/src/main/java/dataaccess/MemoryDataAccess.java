package dataaccess;

import datamodel.AuthData;
import datamodel.UserData;

import java.util.HashMap;

public class MemoryDataAccess implements DataAccess {
    private final HashMap<String, UserData> users = new HashMap<>();
    private final HashMap<String, AuthData> authentifier = new HashMap<>();
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
    public void addAuth(AuthData auth) { authentifier.put(auth.username(), auth); }

    @Override
    public AuthData getAuth(String username) { return authentifier.get(username); }
}
