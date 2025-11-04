package dataaccess;

import com.google.gson.Gson;
import datamodel.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.sql.Types.NULL;

public class SQLDataAccess implements DataAccess {

    public SQLDataAccess() throws DataAccessException {
        configureDatabase();
    }

    @Override
    public void clear() throws DataAccessException {
        var tables = new String[]{"users", "authentifier", "games"};
        try (Connection conn = DatabaseManager.getConnection()) {
            try (Statement s = conn.createStatement()) {
                for (String table : tables) {
                    s.executeUpdate("TRUNCATE TABLE " + table);
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException(String.format("Failed to clear database: %s", ex.getMessage()));
        }
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        var statement = "INSERT INTO users (username, password, json) VALUES (?, ?, ?)";
        var serializer = new Gson();
        String serialized = serializer.toJson(user);
        executeUpdate(statement, user.username(), user.password(), serialized);
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "SELECT username, json FROM users WHERE username=?";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return readUser(rs);
                    }
                }
            }
        } catch (Exception ex) {
            throw new DataAccessException(String.format("Unable to read data: %s", ex.getMessage()));
        }
        return null;
    }

    @Override
    public void addAuth(AuthData auth) throws DataAccessException {
        var statement = "INSERT INTO authentifier (token, username) VALUES (?, ?)";
        executeUpdate(statement, auth.authToken(), auth.username());
    }

    @Override
    public String getAuthUser(String auth) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "SELECT token, username FROM authentifier WHERE token=?";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setString(1, auth);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return readAuth(rs).username();
                    }
                }
            }
        } catch (Exception ex) {
            throw new DataAccessException(String.format("Unable to read data: %s", ex.getMessage()));
        }
        return null;
    }

    @Override
    public void delAuth(String auth) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            try (Statement s = conn.createStatement()) {
                    s.executeUpdate("DELETE FROM authentifier WHERE token = '" + auth + "'");
            }
        } catch (SQLException ex) {
            throw new DataAccessException(String.format("Failed to delete authentifier token: %s", ex.getMessage()));
        }
    }

    @Override
    public HashMap listGames() throws DataAccessException {
        var gamesList = new ArrayList<GameData>();
        var statement = "SELECT json FROM games";
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        GameData game = readGame(rs);
                        gamesList.add(game);
                    }
                }
            }
        } catch (Exception ex) {
            throw new DataAccessException(String.format("Unable to read data: %s", ex.getMessage()));
        }
        HashMap<String, Collection> list = new HashMap<>();
        list.put("games", gamesList);
        return list;
    }

    @Override
    public void addGame(GameData game) throws DataAccessException {
        var statement = "INSERT INTO games (id, name, json) VALUES (?, ?, ?)";
        var serializer = new Gson();
        String serialized = serializer.toJson(game);
        executeUpdate(statement, game.gameID(), game.gameName(), serialized);
    }

    @Override
    public void joinGame(Integer gameID, String color, String auth) throws DataAccessException {
        GameData game = null;
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "SELECT json FROM games WHERE id=?";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setInt(1, gameID);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        game = readGame(rs);
                    }
                }
            }
        } catch (Exception ex) {
            throw new DataAccessException(String.format("Unable to read data: %s", ex.getMessage()));
        }
        String user = getAuthUser(auth);

        String statement = "UPDATE games SET json = ? WHERE id = ?";
        if (color.equals("WHITE") && game.whiteUsername() == null){
            GameData updated = new GameData(gameID, user, game.blackUsername(), game.gameName(), game.game());
            var serializer = new Gson();
            String serialized = serializer.toJson(updated);
            executeUpdate(statement, serialized, gameID);
        } else if (color.equals("BLACK") && game.blackUsername() == null) {
            GameData updated = new GameData(gameID, game.whiteUsername(), user, game.gameName(), game.game());
            var serializer = new Gson();
            String serialized = serializer.toJson(updated);
            executeUpdate(statement, serialized, gameID);
        } else {
            throw new DataAccessException("already taken");
        }
    }

    @Override
    public GameData getGameFromID(Integer gameID) throws DataAccessException {
        return null;
    }

    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS users (
              `username` varchar(256) NOT NULL,
              `password` varchar(256) NOT NULL,
              `json` TEXT DEFAULT NULL,
              PRIMARY KEY (`username`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """
            ,
            """
            CREATE TABLE IF NOT EXISTS authentifier (
              `token` varchar(256) NOT NULL,
              `username` varchar(256) NOT NULL,
              PRIMARY KEY (`token`),
              INDEX(token),
              INDEX(username)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """
            ,
            """
            CREATE TABLE IF NOT EXISTS games (
              `id` int NOT NULL,
              `name` varchar(256) NOT NULL,
              `json` TEXT DEFAULT NULL,
              PRIMARY KEY (`id`),
              INDEX(name)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """
    };

    private void configureDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();
        try (Connection conn = DatabaseManager.getConnection()) {
            for (String statement : createStatements) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException(ex.getMessage());
        }
    }

    private void executeUpdate(String statement, Object... params) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(statement, RETURN_GENERATED_KEYS)) {
                for (int i = 0; i < params.length; i++) {
                    Object param = params[i];
                    if (param instanceof String p) ps.setString(i + 1, p);
                    else if (param instanceof Integer p) ps.setInt(i + 1, p);
                    else if (param instanceof GameData p) ps.setString(i + 1, p.toString());
                    else if (param instanceof UserData p) ps.setString(i + 1, p.toString());
                    else if (param == null) ps.setNull(i + 1, NULL);
                }
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    private UserData readUser(ResultSet rs) throws SQLException {
        var username = rs.getString("username");
        var json = rs.getString("json");
        UserData user = new Gson().fromJson(json, UserData.class);
        return user;
    }

    private AuthData readAuth(ResultSet rs) throws SQLException {
        var token = rs.getString("token");
        var username = rs.getString("username");
        AuthData auth = new AuthData(username,token);
        return auth;
    }

    private GameData readGame(ResultSet rs) throws SQLException {
        var json = rs.getString("json");
        GameData game = new Gson().fromJson(json, GameData.class);
        return game;
    }
}
