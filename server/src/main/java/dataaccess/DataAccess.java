package dataaccess;

import chess.ChessGame;
import com.google.gson.JsonObject;
import datamodel.*;

import java.util.HashMap;

public interface DataAccess {
    void clear() throws DataAccessException;
    void createUser(UserData user) throws DataAccessException;
    UserData getUser(String username) throws DataAccessException;
    void addAuth(AuthData auth) throws DataAccessException;
    String getAuthUser(String auth) throws DataAccessException;
    void delAuth(String auth) throws DataAccessException;
    HashMap listGames() throws DataAccessException;
    void addGame(GameData game) throws DataAccessException;
    void joinGame(Integer gameID, String color, String auth) throws DataAccessException;
    GameData getGameFromID(Integer gameID) throws DataAccessException;
}
