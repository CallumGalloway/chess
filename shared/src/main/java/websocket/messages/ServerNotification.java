package websocket.messages;

public class ServerNotification extends ServerMessage {

    private String message = null;

    public ServerNotification(String message) {
        super(ServerMessageType.NOTIFICATION);
        this.message = message;
    }

    public String getMessage(){
        return  this.message;
    }
}
