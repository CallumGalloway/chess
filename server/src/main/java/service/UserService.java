package service;

import dataaccess.DataAccess;
import datamodel.AuthData;
import datamodel.UserData;

import java.util.UUID;

public class UserService {
    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public AuthData register(UserData user) throws Exception {
        if (user.username() == null || user.password() == null || user.username() == "" || user.password() == "") {
            throw new Exception("bad request");
        }
        if (dataAccess.getUser(user.username()) != null) {
            throw new Exception("username already taken");
        }
        dataAccess.createUser(user);
        AuthData auth = new AuthData(user.username(), generateAuthToken());
        dataAccess.addAuth(auth);
        return auth;
    }

    public AuthData login(UserData user) throws Exception {
        if (user.username() == null || user.password() == null || user.username() == "" || user.password() == "") {
            throw new Exception("bad request");
        }
        if (dataAccess.getUser(user.username()) == null) {
            throw new Exception("unauthorized");
        }
        String entered = user.password();
        String actual = dataAccess.getUser(user.username()).password();
        if (entered.equals(actual)) {
            AuthData auth = new AuthData(user.username(), generateAuthToken());
            dataAccess.addAuth(auth);
            return auth;
        }
        else {
            throw new Exception("unauthorized");
        }
    }

    public void logout(String auth) throws Exception {
        if (dataAccess.getAuthUser(auth) != null) {
            dataAccess.delAuth(auth);
        }
        else {
            throw new Exception("unauthorized");
        }
    }

    public void clear() {
        dataAccess.clear();
    }

    private String generateAuthToken() {
        return UUID.randomUUID().toString();
    }
}
