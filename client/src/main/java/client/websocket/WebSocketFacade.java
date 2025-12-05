package client.websocket;

import chess.ChessMove;
import chess.ChessPosition;
import server.State;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import websocket.messages.*;
import websocket.commands.*;

import jakarta.websocket.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

//need to extend Endpoint for websocket to work properly
public class WebSocketFacade extends Endpoint {

    Session session;
    NotificationHandler notificationHandler;
    public State state;
    private String authToken;

    public WebSocketFacade(String url, NotificationHandler notificationHandler) throws Exception {
        try {
            url = url.replace("http", "ws");
            URI socketURI = new URI(url + "/ws");
            this.notificationHandler = notificationHandler;

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);

            //set message handler
            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    ServerMessage notification = setMessageType(message);
                    notificationHandler.notify(notification);
                }
            });
        } catch (DeploymentException | IOException | URISyntaxException ex) {
            throw new Exception(ex.getMessage());
        }
        state = State.SIGNED_OUT;
        authToken = null;
    }

    //Endpoint requires this method, but you don't have to do anything
    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }

    public void startWebSocket(String auth, Integer gameID) throws Exception {
        try {
            var command = new UserGameCommand(UserGameCommand.CommandType.CONNECT, auth, gameID);
            this.session.getBasicRemote().sendText(new Gson().toJson(command));
        } catch (IOException ex) {
            throw new Exception(ex.getMessage());
        }
    }

    public void endWebSocket() throws Exception {
        try {
            this.session.close();
        } catch (IOException ex) {
        throw new Exception(ex.getMessage());
        }
    }

    public String makeMove(String auth, Integer gameID, String[] params) throws Exception {
        ChessPosition startPos = toPosition(params[0]);
        ChessPosition endPos = toPosition(params[1]);
        ChessMove move = posToMove(startPos, endPos);
        var command = new MakeMoveCommand(UserGameCommand.CommandType.MAKE_MOVE, auth, gameID, move);
        this.session.getBasicRemote().sendText(new Gson().toJson(command));
        return "";
    }

    public String resign() throws Exception {
        return "";
    }

    public String leave(String auth, Integer gameID) throws Exception {
        var command = new UserGameCommand(UserGameCommand.CommandType.LEAVE, auth, gameID);
        this.session.getBasicRemote().sendText(new Gson().toJson(command));
        endWebSocket();
        return "LEAVE";
    }

    public ServerMessage setMessageType(String jsonString) {
        var jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();
        var type = jsonObject.get("serverMessageType").getAsString();

        var serializer = new Gson();

        switch (type) {
            case "LOAD_GAME" -> {
                return serializer.fromJson(jsonString, ServerLoadGame.class);
            }
            case "NOTIFICATION" -> {
                return serializer.fromJson(jsonString, ServerNotification.class);
            }
            case "ERROR" -> {
                return serializer.fromJson(jsonString, ServerNotification.class);
            }
            default -> {
                return serializer.fromJson(jsonString, ServerMessage.class);
            }
        }
    }

    public ChessPosition toPosition(String target) {
        if (target.length() == 2) {
            char colChar = target.toLowerCase().charAt(0);
            char rowChar = target.charAt(1);

            int col = colChar - 'a' + 1;
            int row = rowChar - '1' + 1;

            if (col >= 1 && col <= 8 && row >= 1 && row <= 8) {
                return new ChessPosition(row, col);
            }
        }
        return null; // Return null if invalid
    }

    public ChessMove posToMove(ChessPosition startPos, ChessPosition endPos) {
            if (startPos != null && endPos != null) {
            return new ChessMove(startPos, endPos, null);
        }
        return null;
    }
}
