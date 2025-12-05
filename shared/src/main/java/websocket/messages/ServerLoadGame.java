package websocket.messages;

import datamodel.GameData;

public class ServerLoadGame extends ServerMessage {

    private GameData game;
    private String color;
    public ServerLoadGame(ServerMessageType type, GameData game) {
        super(type);
        this.game = game;
    }

    public GameData getGameData() {
        return game;
    }
}
