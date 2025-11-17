package service;

import dataaccess.DataAccess;
import datamodel.*;
import dataaccess.DataAccessException;
import org.mindrot.jbcrypt.BCrypt;

import java.util.UUID;

public class UserService {
    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public datamodel.AuthData register(UserData user) throws Exception {
        if (user.username() == null || user.password() == null || user.username() == "" || user.password() == "") {
            throw new Exception("bad request");
        }
        if (dataAccess.getUser(user.username()) != null) {
            throw new Exception("username already taken");
        }
        UserData encrypted = new UserData(user.username(),encryptPass(user.password()), user.email());
        dataAccess.createUser(encrypted);
        AuthData auth = new AuthData(user.username(), generateAuthToken());
        dataAccess.addAuth(auth);
        return auth;
    }

    public datamodel.AuthData login(UserData user) throws Exception {
        if (user.username() == null || user.password() == null || user.username() == "" || user.password() == "") {
            throw new Exception("bad request");
        }
        if (dataAccess.getUser(user.username()) == null) {
            throw new Exception("unauthorized");
        }
        String entered = user.password();
        String encrypted = dataAccess.getUser(user.username()).password();
        if (BCrypt.checkpw(entered,encrypted)) {
            AuthData auth = new AuthData(user.username(), generateAuthToken());
            dataAccess.addAuth(auth);
            return auth;
        }
        else {
            throw new Exception("unauthorized");
        }
    }

    public void logout(String auth) throws Exception {
        if (auth == null){
            throw new Exception("unauthorized");
        }
        if (dataAccess.getAuthUser(auth) != null) {
            dataAccess.delAuth(auth);
        }
        else {
            throw new Exception("unauthorized");
        }
    }

    public void clear() throws Exception {
        try {
            dataAccess.clear();
        } catch (DataAccessException ex){
            throw new Exception(String.format("Data Access Exception: %s",ex.getMessage()));
        }
    }

    private String generateAuthToken() {
        return UUID.randomUUID().toString();
    }

    private String encryptPass(String pass) {
        String salty = BCrypt.gensalt();
        return BCrypt.hashpw(pass,salty);
    }
}
