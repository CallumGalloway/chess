package server.websocket;

import com.google.gson.Gson;
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
                case CONNECT -> connect(ctx.session);
                case MAKE_MOVE -> make_move(ctx.session);
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

    private void connect(Session session) throws IOException {
        connections.add(session);
        // var content = String.format("Joined Game: %s", gameName);
        var message = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME);
        connections.broadcast(session, message);
    }

    private void make_move(Session session) throws IOException {

    }

    private void leave(Session session, Integer gameID) throws IOException {
        var notification = new ServerNotification(ServerMessage.ServerMessageType.NOTIFICATION, "Left game");
        connections.broadcast(session, notification);
        //connections.remove(session);
    }

    private void resign(Session session) throws IOException {

    }

}