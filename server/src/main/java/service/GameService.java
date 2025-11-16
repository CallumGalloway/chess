package service;

import chess.ChessGame;
import dataaccess.DataAccess;
import datamodel.*;

import java.util.HashMap;
import java.util.UUID;

public class GameService {

    private final DataAccess dataAccess;

    public GameService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public GameList listGames(String auth) throws Exception {
        if (dataAccess.getAuthUser(auth) != null) {
            var games = dataAccess.listGames();
            return games;
        }
        else {
            throw new Exception("unauthorized");
        }
    }

    public Integer addGame(String name, String auth) throws Exception {
        if (dataAccess.getAuthUser(auth) != null) {
            if (name != null && name != "") {
                var gameID = generateGameID();
                var game = new GameData(gameID, null, null, name, new ChessGame());
                dataAccess.addGame(game);
                return gameID;
            } else {
                throw new Exception("Bad Request");
            }
        }
        else {
            throw new Exception("unauthorized");
        }
    }

    public void joinGame(JoinData joinData, String auth) throws Exception {
        if (dataAccess.getAuthUser(auth) != null) {
            var gameID = joinData.gameID();
            var color = joinData.playerColor();
            if (gameID != null && color != null && (color.equals("WHITE") || color.equals("BLACK"))) {
                try {
                    dataAccess.joinGame(gameID, color, auth);
                } catch (Exception ex){
                    throw new Exception(ex);
                }
            } else {
                throw new Exception("Bad Request");
            }
        }
        else {
            throw new Exception("unauthorized");
        }
    }

    private Integer generateGameID() {
        return Math.abs(UUID.randomUUID().toString().hashCode());
    }
}
