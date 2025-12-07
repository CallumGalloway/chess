package server.websocket;

import chess.*;
import com.google.gson.Gson;
import dataaccess.DataAccess;
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
            var user = dataAccess.getAuthUser(authToken);
            if (user != null) {
                var game = dataAccess.getGameFromID(gameID);
                if (game != null) {
                    connections.add(gameID, session);
                    var message = new ServerLoadGame(game);
                    connections.send(session, message);
                    connections.broadcast(gameID, session, new ServerNotification(String.format("Player %s joined the game.", user)));
                } else {
                    var error = new ServerError("Game does not exist!");
                    connections.send(session, error);
                }
            } else {
                var error = new ServerError("unauthorized");
                connections.send(session, error);
            }

        } catch (Exception ex) {
            throw new IOException(ex.getMessage());
        }
    }

    private void makeMove(String authToken, Integer gameID, ChessMove move, Session session) throws IOException {
        try {
            if (dataAccess.getAuthUser(authToken) != null) {
                if (dataAccess.getGameFromID(gameID) != null) {
                    var gameData = dataAccess.getGameFromID(gameID);
                    var turn = gameData.game().getTeamTurn();
                    if (!gameData.game().isInCheckmate(turn) && !gameData.game().isInStalemate(turn)) {
                        if (!gameData.game().checkGameFinished()) {
                            var user = dataAccess.getAuthUser(authToken);
                            var blackPlayer = gameData.blackUsername();
                            var whitePlayer = gameData.whiteUsername();
                            if ((turn == ChessGame.TeamColor.WHITE && user.equals(whitePlayer)) || (turn == ChessGame.TeamColor.BLACK && user.equals(blackPlayer))) {
                                var pieceColor = gameData.game().getBoard().getPiece(move.getStartPosition()).getTeamColor();
                                if (pieceColor == turn) {
                                    var newGame = gameData.game().copyGame();
                                    try {
                                        newGame.makeMove(move);
                                        var newGameData = new GameData(gameID, gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), newGame);
                                        dataAccess.addGame(newGameData);
                                        var load = new ServerLoadGame(newGameData);
                                        connections.broadcast(newGameData.gameID(), null, load);
                                        var msg = new ServerNotification(String.format("%s made their move %s to %s.", user, move.getStartPosition().toString(), move.getEndPosition().toString()));
                                        connections.broadcast(gameID, session, msg);
                                    } catch (InvalidMoveException ex) {
                                        var error = new ServerError("Invalid move.");
                                        connections.send(session, error);
                                    }
                                } else {
                                    var error = new ServerError("That is not your piece.");
                                    connections.send(session, error);
                                }
                            } else {
                                var error = new ServerError("It is not your turn.");
                                connections.send(session, error);
                            }
                        } else {
                            var message = gameData.game().isInCheckmate(turn) ? "You are in checkmate. " : "You are in stalemate. ";
                            var error = new ServerError(message + "The game is over.");
                            connections.send(session, error);
                        }
                    } else {
                        var error = new ServerError("The game is over.");
                        connections.send(session, error);
                    }
                } else {
                    var error = new ServerError("Game does not exist!");
                    connections.send(session, error);
                }
            } else {
                var error = new ServerError("unauthorized");
                connections.send(session, error);
            }
        } catch (Exception ex) {
            throw new IOException(ex.getMessage());
        }
    }

    private void leave(String authToken, Integer gameID, Session session) throws IOException {
        try {
            var user = dataAccess.getAuthUser(authToken);
            if (user != null) {
                var gameData = dataAccess.getGameFromID(gameID);
                if (gameData != null) {
                    var blackPlayer = gameData.blackUsername();
                    var whitePlayer = gameData.whiteUsername();
                    if (user.equals(whitePlayer)) {
                        var notification = new ServerNotification(String.format("White player %s left the game.", user));
                        connections.broadcast(gameID, session, notification);
                    } else if (user.equals(blackPlayer)) {
                        var notification = new ServerNotification(String.format("Black player %s left the game.", user));
                        connections.broadcast(gameID, session, notification);
                    } else {
                        var notification = new ServerNotification(String.format("%s stopped observing the game.", user));
                        connections.broadcast(gameID, session, notification);
                    }
                    dataAccess.joinGame(gameID, null, authToken);
                    connections.remove(gameID, session);
                } else {
                var error = new ServerError("Game does not exist!");
                connections.send(session, error);
                }
            } else {
                var error = new ServerError("unauthorized");
                connections.send(session, error);
            }
        } catch (Exception ex) {
            throw new IOException(ex.getMessage());
        }
    }

    private void resign(String authToken, Integer gameID, Session session) throws IOException {
        try {
        var user = dataAccess.getAuthUser(authToken);
            if (user != null) {
                var gameData = dataAccess.getGameFromID(gameID);
                if (gameData != null) {
                    if (!gameData.game().checkGameFinished()) {
                        var blackPlayer = gameData.blackUsername();
                        var whitePlayer = gameData.whiteUsername();
                        if (user.equals(whitePlayer)) {
                            var newGameData = new GameData(gameID, gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), gameData.game().copyGame());
                            newGameData.game().setGameFinished(true);
                            dataAccess.addGame(newGameData);
                            var notification = new ServerNotification(String.format("White player %s resigned.", user));
                            connections.broadcast(gameID, null, notification);
                        } else if (user.equals(blackPlayer)) {
                            var newGameData = new GameData(gameID, gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), gameData.game().copyGame());
                            newGameData.game().setGameFinished(true);
                            dataAccess.addGame(newGameData);
                            var notification = new ServerNotification(String.format("Black player %s resigned.", user));
                            connections.broadcast(gameID, null, notification);
                        } else {
                            var error = new ServerError("non-players cannot resign.");
                            connections.send(session, error);
                        }
                    } else {
                        var error = new ServerError("Game is already over!");
                        connections.send(session, error);
                    }
                } else {
                    var error = new ServerError("Game does not exist!");
                    connections.send(session, error);
                }
            } else {
                var error = new ServerError("unauthorized");
                connections.send(session, error);
            }
        } catch (Exception ex) {
            throw new IOException(ex.getMessage());
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

}