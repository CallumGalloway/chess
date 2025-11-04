package dataaccess;

import datamodel.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SQLDataAccessTest {
    @BeforeEach
    void setup() throws Exception {
        DataAccess db = new SQLDataAccess();
        db.clear();
    }

    @Test
    void clear() throws Exception {
        DataAccess db = new SQLDataAccess();
        db.createUser(new UserData("new", "password", "new@new.com"));
        db.clear();
        assertNull(db.getUser("new"));
    }

    @Test
    void createUser() throws Exception {
        DataAccess db = new SQLDataAccess();
        var user = new UserData("new", "password", "new@new.com");
        db.createUser(user);
        assertEquals(user, db.getUser(user.username()));
    }

    @Test
    void getUser() throws Exception {
        DataAccess db = new SQLDataAccess();
        var user = new UserData("new", "password", "new@new.com");
        db.createUser(user);
        UserData results = db.getUser("new");
        assertNotNull(results);
        assertEquals(user, results);
    }

    @Test
    void getUserFail() throws Exception {
        DataAccess db = new SQLDataAccess();
        var user = new UserData("new", "password", "new@new.com");
        db.createUser(user);
        UserData results = db.getUser("notAUser");
        assertNull(results);
    }

    @Test
    void addAuth() throws Exception {
        DataAccess db = new SQLDataAccess();
        var user = new UserData("new", "password", "new@new.com");
        db.createUser(user);
        var auth = new AuthData("new", "token123");

        db.addAuth(auth);

        assertEquals("new", db.getAuthUser("token123"));
    }

    @Test
    void addAuthFail() throws Exception {
        DataAccess db = new SQLDataAccess();
        var user = new UserData("new", "password", "new@new.com");

        var auth1 = new AuthData("new", "token123");
        db.addAuth(auth1);

        var auth2 = new AuthData("newer", "token123");

        assertThrows(DataAccessException.class, () -> db.addAuth(auth2));
    }

    @Test
    void getAuthUser() throws Exception {
        DataAccess db = new SQLDataAccess();
        var user = new UserData("new", "password", "new@new.com");
        db.createUser(user);
        var auth = new AuthData("new", "token123");
        db.addAuth(auth);

        String retrievedUser = db.getAuthUser("token123");

        assertEquals("new", retrievedUser);
    }

    @Test
    void getAuthUserFail() throws Exception {
        DataAccess db = new SQLDataAccess();
        String retrievedUser = db.getAuthUser("nonExistentToken");
        assertNull(retrievedUser);
    }

    @Test
    void delAuth() throws Exception {
        DataAccess db = new SQLDataAccess();
        var user = new UserData("new", "password", "new@new.com");
        db.createUser(user);
        var auth = new AuthData("new", "token123");
        db.addAuth(auth);
        assertNotNull(db.getAuthUser("token123"));
        db.delAuth("token123");
        assertNull(db.getAuthUser("token123"));
    }
//
//    @Test
//    void listGames() {
//
//    }
//
//    @Test
//    void listGamesEmpty() {
//
//    }
//
//    @Test
//    void addGame() {
//
//    }
//
//    @Test
//    void joinGame() {
//
//    }
//
//    @Test
//    void joinGameFail() {
//
//    }
}
