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
            UserGameCommand command = new Gson().fromJson(ctx.message(), UserGameCommand.class);
            switch (command.getCommandType()) {
                case CONNECT -> connect(command.getAuthToken(), command.getGameID(), ctx.session);
                case MAKE_MOVE -> {
                    command = (MakeMoveCommand) command;
                    make_move(command.getAuthToken(), ((MakeMoveCommand) command).getMove(), ctx.session);
                }
                case LEAVE -> leave(ctx.session, command.getGameID());
                case RESIGN -> resign(ctx.session);
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
                    var message = new ServerLoadGame(ServerMessage.ServerMessageType.LOAD_GAME, dataAccess.getGameFromID(gameID));
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

    private void make_move(String authToken, ChessMove move, Session session) throws IOException {

    }

    private void leave(Session session, Integer gameID) throws IOException {
        var notification = new ServerNotification("Left game");
        connections.broadcast(gameID, session, notification);
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