package server.websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    public final ConcurrentHashMap<Integer, ArrayList<Session>> connections = new ConcurrentHashMap<>();

    public void add(Integer gameID, Session session) {
        if (!connections.contains(gameID)) {
            connections.put(gameID, new ArrayList<>());
        }
        var sessionList = connections.get(gameID);
        sessionList.add(session);
        connections.put(gameID, sessionList);
    }

    public void remove(Integer gameID, Session session) throws Exception {
        var sessionList = connections.get(gameID);
        if (sessionList.contains(session)) {
            sessionList.remove(session);
        } else {
            throw new Exception("Error: session not found for removal");
        }

    }

    public void broadcast(Integer gameID, Session excludeSession, ServerMessage notification) throws IOException {
        var serializer = new Gson();
        String msg = serializer.toJson(notification);
        var sessionList = connections.get(gameID);
        for (Session c : sessionList) {
            if (c.isOpen()) {
                if (!c.equals(excludeSession)) {
                    c.getRemote().sendString(msg);
                }
            }
        }
    }
}