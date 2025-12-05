package client.websocket;

import client.State;
import com.google.gson.Gson;
import datamodel.UserData;
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
                    ServerMessage notification = new Gson().fromJson(message, ServerMessage.class);
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

    public String redraw() throws Exception {
        return "";
    }

    public String highlight(String[] params) {
        return "";
    }

    public String makeMove(String[] params) throws Exception {
        return "";
    }

    public String resign() throws Exception {
        return "";
    }

    public String leave(String auth, Integer gameID) throws Exception {
        var command = new UserGameCommand(UserGameCommand.CommandType.LEAVE, auth, gameID);
        this.session.getBasicRemote().sendText(new Gson().toJson(command));
        endWebSocket();
        return "";
    }
}
