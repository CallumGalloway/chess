package websocket.messages;

public class ServerError extends ServerMessage {

    private String errorMessage = null;

    public ServerError(String message) {
        super(ServerMessageType.ERROR);
        this.errorMessage = message;
    }

    public String getErrorMessage(){
        return  this.errorMessage;
    }
}