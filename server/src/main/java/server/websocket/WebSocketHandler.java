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
import java.util.ArrayList;

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
        System.out.println("Websocket connected");
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
                    make_move(moveCommand.getAuthToken(), moveCommand.getGameID(), moveCommand.getMove(), ctx.session);
                }
                case LEAVE -> {
                    leave(ctx.session, command.getGameID());
                }
                case RESIGN -> {
                    resign(ctx.session);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void handleClose(WsCloseContext ctx) {
        System.out.println("Websocket closed");
    }

    private void connect(String authToken, Integer gameID, Session session) throws IOException {
        try {
            if (dataAccess.getAuthUser(authToken) != null) {
                if (dataAccess.getGameFromID(gameID) != null) {
                    connections.add(gameID, session);
                    var message = new ServerLoadGame(dataAccess.getGameFromID(gameID));
                    connections.send(session, message);
                    connections.broadcast(gameID, session, new ServerNotification("Player joined"));
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

    private void make_move(String authToken, Integer gameID, ChessMove move, Session session) throws IOException {
        try {
            if (dataAccess.getAuthUser(authToken) != null) {
                if (dataAccess.getGameFromID(gameID) != null) {
                    var gameData = dataAccess.getGameFromID(gameID);
                    var turn = gameData.game().getTeamTurn();
                    if (!gameData.game().isInCheckmate(turn) && !gameData.game().isInStalemate(turn)) {
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
                                    var msg = new ServerNotification(String.format("%s made their move", user));
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

    private void leave(Session session, Integer gameID) throws IOException {
        var notification = new ServerNotification("A player left the game.");
        connections.broadcast(gameID, session, notification);
        session.close();
    }

    private void resign(Session session) throws IOException {

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