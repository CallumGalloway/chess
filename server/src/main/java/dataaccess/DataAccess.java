package dataaccess;

import com.google.gson.JsonObject;
import datamodel.*;

import java.util.HashMap;

public interface DataAccess {
    void clear();
    void createUser(UserData user);
    UserData getUser(String username);
    void addAuth(AuthData auth);
    String getAuthUser(String auth);
    void delAuth(String auth);
    HashMap listGames();
    void addGame(GameData game);
}
