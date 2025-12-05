package websocket.messages;

import datamodel.GameData;

public class ServerLoadGame extends ServerMessage {

    private GameData gameData;
    private String color;
    public ServerLoadGame(ServerMessageType type, GameData gameData, String color) {
        super(type);
        this.gameData = gameData;
        this.color = color;
    }

    public GameData getGameData() {
        return gameData;
    }

    public String getColor() {
        return color;
    }
}
