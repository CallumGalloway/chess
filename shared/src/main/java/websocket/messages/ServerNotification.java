package websocket.messages;

public class ServerNotification extends ServerMessage {

    private String message = null;

    public ServerNotification(ServerMessageType type, String message) {
        super(type);
        this.message = message;
    }

    public String getMessage(){
        return  this.message;
    }
}
