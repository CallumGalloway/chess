package server.websocket;

import chess.*;
import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.SQLDataAccess;
import datamodel.*;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsCloseHandler;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsConnectHandler;
import io.javalin.websocket.WsMessageContext;
import io.javalin.websocket.WsMessageHandler;
import org.eclipse.jetty.websocket.api.Session;
import websocket.commands.*;
import websocket.messages.*;

import java.io.IOException;

public class WebSocketHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler {

    private final ConnectionManager connections = new ConnectionManager();
    private final DataAccess dataAccess;

    public WebSocketHandler(){
        try {
            dataAccess = new SQLDataAccess();
            System.out.println("SQL connection successful");
        } catch (Exception ex) {
//            dataAccess = new MemoryDataAccess();
//            System.out.println("SQL connection failed, using memory");
            throw new RuntimeException(String.format("SQL connection in WebSocket failed, %s",ex));
        }
    }

    @Override
    public void handleConnect(WsConnectContext ctx) {
        System.out.println("WebSocket connected");
        ctx.enableAutomaticPings();
    }

    @Override
    public void handleMessage(WsMessageContext ctx) {
        try {
            var serializer = new Gson();
            UserGameCommand command = serializer.fromJson(ctx.message(), UserGameCommand.class);
            switch (command.getCommandType()) {
                case CONNECT -> {
                    UserGameCommand connectCommand = command;
                    connect(connectCommand.getAuthToken(), connectCommand.getGameID(), ctx.session);
                }
                case MAKE_MOVE -> {
                    MakeMoveCommand moveCommand = serializer.fromJson(ctx.message(), MakeMoveCommand.class);
                    makeMove(moveCommand.getAuthToken(), moveCommand.getGameID(), moveCommand.getMove(), ctx.session);
                }
                case LEAVE -> {
                    leave(command.getAuthToken(), command.getGameID(), ctx.session);
                }
                case RESIGN -> {
                    resign(command.getAuthToken(), command.getGameID(), ctx.session);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void handleClose(WsCloseContext ctx) {
        System.out.println("WebSocket closed");
    }

    private void connect(String authToken, Integer gameID, Session session) throws IOException {
        try {
            // check user, game
            checkAuth(authToken);
            checkGame(gameID);
            var user = dataAccess.getAuthUser(authToken);
            var game = dataAccess.getGameFromID(gameID);

            connections.add(gameID, session);
            var message = new ServerLoadGame(game);
            connections.send(session, message);
            connections.broadcast(gameID, session, new ServerNotification(String.format("Player %s joined the game.", user)));

        } catch (DataAccessException ex) {
            throw new IOException(ex.getMessage());
        } catch (Exception ex) {
            var error = new ServerError(ex.getMessage());
            connections.send(session, error);
        }
    }

    private void makeMove(String authToken, Integer gameID, ChessMove move, Session session) throws IOException {
        try {
            // check user, game, observer, piece
            checkAuth(authToken);
            checkGame(gameID);
            checkGameEnded(gameID);
            checkObserver(authToken,gameID);
            checkTurn(authToken, gameID);
            checkMove(authToken, gameID, move);
            checkOwnership(authToken, gameID, move.getStartPosition());

            var gameData = dataAccess.getGameFromID(gameID);
            var turn = gameData.game().getTeamTurn();
            var user = dataAccess.getAuthUser(authToken);
            var blackPlayer = gameData.blackUsername();
            var whitePlayer = gameData.whiteUsername();
            var pieceColor = gameData.game().getBoard().getPiece(move.getStartPosition()).getTeamColor();
            var newGame = gameData.game().copyGame();

            try {
                newGame.makeMove(move);
                var newGameData = new GameData(gameID, gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), newGame);
                dataAccess.addGame(newGameData);
                var load = new ServerLoadGame(newGameData);
                connections.broadcast(newGameData.gameID(), null, load);
                var msg = new ServerNotification(
                        String.format("%s made their move %s to %s.", user,
                                coordToReadable(move.getStartPosition()), coordToReadable(move.getEndPosition())));
                connections.broadcast(gameID, session, msg);
            } catch (InvalidMoveException ex) {
                var error = new ServerError("Invalid move.");
                connections.send(session, error);
            }

            //check and checkmate messages
            try {
                checkMate(gameID);
            } catch (Exception ex) {
                var msg = new ServerNotification(ex.getMessage());
                connections.broadcast(gameID, null, msg);
            }

        } catch (DataAccessException ex) {
            throw new IOException(ex.getMessage());
        } catch (Exception ex) {
            var error = new ServerError(ex.getMessage());
            connections.send(session, error);
        }
    }

    private void leave(String authToken, Integer gameID, Session session) throws IOException {
        try {
            // check user, game
            checkAuth(authToken);
            checkGame(gameID);
            var user = dataAccess.getAuthUser(authToken);
            var gameData = dataAccess.getGameFromID(gameID);
            var blackPlayer = gameData.blackUsername();
            var whitePlayer = gameData.whiteUsername();

            try {
                checkObserver(authToken, gameID);
            } catch (Exception ex) {
                var notification = new ServerNotification(String.format("%s stopped observing the game.", user));
                connections.broadcast(gameID, session, notification);
            }

            if (user.equals(whitePlayer)) {
                var notification = new ServerNotification(String.format("White player %s left the game.", user));
                connections.broadcast(gameID, session, notification);
            }
            if (user.equals(blackPlayer)) {
                var notification = new ServerNotification(String.format("Black player %s left the game.", user));
                connections.broadcast(gameID, session, notification);
            }
            dataAccess.joinGame(gameID, "leave", authToken);
            connections.remove(gameID, session);
        } catch (DataAccessException ex) {
            throw new IOException(ex.getMessage());
        } catch (Exception ex) {
            var error = new ServerError(ex.getMessage());
            connections.send(session, error);
        }
    }

    private void resign(String authToken, Integer gameID, Session session) throws IOException {
        try {
            // check user, game, ended, observer
            checkAuth(authToken);
            checkGame(gameID);
            checkGameEnded(gameID);
            checkObserver(authToken, gameID);
            var user = dataAccess.getAuthUser(authToken);
            var gameData = dataAccess.getGameFromID(gameID);
            var blackPlayer = gameData.blackUsername();
            var whitePlayer = gameData.whiteUsername();

            if (user.equals(whitePlayer)) {
                var newGameData = new GameData(gameID, gameData.whiteUsername(), gameData.blackUsername(),
                        gameData.gameName(), gameData.game().copyGame());
                newGameData.game().setGameFinished(true);
                dataAccess.addGame(newGameData);
                var notification = new ServerNotification(String.format("White player %s resigned.", user));
                connections.broadcast(gameID, null, notification);
            } else if (user.equals(blackPlayer)) {
                var newGameData = new GameData(gameID, gameData.whiteUsername(), gameData.blackUsername(),
                        gameData.gameName(), gameData.game().copyGame());
                newGameData.game().setGameFinished(true);
                dataAccess.addGame(newGameData);
                var notification = new ServerNotification(String.format("Black player %s resigned.", user));
                connections.broadcast(gameID, null, notification);
            }
        } catch (DataAccessException ex) {
            throw new IOException(ex.getMessage());
        } catch (Exception ex) {
            var error = new ServerError(ex.getMessage());
            connections.send(session, error);
        }
    }

    public void notifyOthers(Session session, String message, Integer gameID) throws IOException {
        var notification = new ServerNotification(message);
        connections.broadcast(gameID, session, notification);
    }

    public void notifyAll(Session session, String message, Integer gameID) throws IOException {
        var notification = new ServerNotification(message);
        connections.broadcast(gameID, null, notification);
    }

    private void checkAuth(String authToken) throws Exception {
        var user = dataAccess.getAuthUser(authToken);
        if (user == null) {
            throw new Exception("Unauthorized");
        }
    }

    private void checkGame(Integer gameID) throws Exception {
        var gameData = dataAccess.getGameFromID(gameID);
        if (gameData == null) {
            throw new Exception("Game does not exist!");
        }
    }

    private ChessGame.TeamColor checkPlayerColor(String authToken, Integer gameID) throws Exception {
        checkObserver(authToken,gameID);
        var user = dataAccess.getAuthUser(authToken);
        var gameData = dataAccess.getGameFromID(gameID);
        var blackPlayer = gameData.blackUsername();
        var whitePlayer = gameData.whiteUsername();
        var playerTeam = user.equals(whitePlayer) ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;
        return playerTeam;
    }

    private void checkTurn(String authToken, Integer gameID) throws Exception {
        var gameData = dataAccess.getGameFromID(gameID);
        var team = checkPlayerColor(authToken, gameID);
        if (!gameData.game().getTeamTurn().equals(team)) {
            throw new Exception("It is not your turn.");
        }
    }

    private void checkMove(String authToken, Integer gameID, ChessMove move) throws Exception {
        try {
            var gameData = dataAccess.getGameFromID(gameID);
            var game = gameData.game();
            var testGame = game.copyGame();
            if (move == null) {
                throw new Exception("Invalid Coordinates");
            }
            testGame.makeMove(move);
        } catch (Exception ex) {
            throw new Exception("Invalid move.");
        }
    }

    private void checkGameEnded(Integer gameID) throws Exception {
        var gameData = dataAccess.getGameFromID(gameID);
        var game = gameData.game();
        if (game.checkGameFinished()) {
            throw new Exception("Game has ended.");
        }
    }

    private void checkObserver(String authToken, Integer gameID) throws Exception {
        var user = dataAccess.getAuthUser(authToken);
        var gameData = dataAccess.getGameFromID(gameID);
        var blackPlayer = gameData.blackUsername();
        var whitePlayer = gameData.whiteUsername();
        if (!user.equals(blackPlayer) && !user.equals(whitePlayer)) {
            throw new Exception("You cannot do that as an observer.");
        }
    }

    private void checkOwnership(String authToken, Integer gameID, ChessPosition position) throws Exception {
        var gameData = dataAccess.getGameFromID(gameID);
        var pieceColor = gameData.game().getBoard().getPiece(position).getTeamColor();
        var player = checkPlayerColor(authToken, gameID);
        if (pieceColor != player) {
            throw new Exception("That is not your piece.");
        }
    }

    private void checkMate(Integer gameID) throws Exception {
        var gameData = dataAccess.getGameFromID(gameID);
        var game = gameData.game();
        if (game.isInCheckmate(ChessGame.TeamColor.WHITE)) {
            throw new Exception("White player is in Checkmate.");
        }
        if (game.isInCheckmate(ChessGame.TeamColor.BLACK)) {
            throw new Exception("Black player is in Checkmate.");
        }
        if (game.isInCheck(ChessGame.TeamColor.WHITE)) {
            throw new Exception("White player is in Check.");
        }
        if (game.isInCheck(ChessGame.TeamColor.BLACK)) {
            throw new Exception("Black player is in Check.");
        }
    }

    private String coordToReadable(ChessPosition position) {
        var string = position.toString();
        int colIndex = Character.getNumericValue(string.charAt(0));
        int rowIndex = Character.getNumericValue(string.charAt(1));

        char colChar = (char) ('a' + colIndex - 1);

        return String.valueOf(colChar) + rowIndex;
    }

}