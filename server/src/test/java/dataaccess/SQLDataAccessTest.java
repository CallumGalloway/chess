package dataaccess;

import datamodel.UserData;
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

//    @Test
//    void addAuth() {
//
//    }
//
//    @Test
//    void addAuthFail() {
//
//    }
//
//    @Test
//    void getAuthUser() {
//
//    }
//
//    @Test
//    void getAuthUserFail() {
//
//    }
//
//    @Test
//    void delAuth() {
//
//    }
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
