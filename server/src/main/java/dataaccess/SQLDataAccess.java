package dataaccess;

import datamodel.*;

import java.util.HashMap;

public class SQLDataAccess implements DataAccess {

    @Override
    public void clear() {

    }

    @Override
    public void createUser(UserData user) {

    }

    @Override
    public UserData getUser(String username) {
        return null;
    }

    @Override
    public void addAuth(AuthData auth) {

    }

    @Override
    public String getAuthUser(String auth) {
        return "";
    }

    @Override
    public void delAuth(String auth) {

    }

    @Override
    public HashMap listGames() {
        return null;
    }

    @Override
    public void addGame(GameData game) {

    }

    @Override
    public void joinGame(Integer gameID, String color, String auth) throws Exception {

    }
}
