package websocket.messages;

import datamodel.GameData;

public class ServerLoadGame extends ServerMessage {

    private GameData game;

    public ServerLoadGame(GameData game) {
        super(ServerMessageType.LOAD_GAME);
        this.game = game;
    }

    public GameData getGameData() {
        return game;
    }
}
