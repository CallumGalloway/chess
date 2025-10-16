package service;

import dataaccess.DataAccess;
import datamodel.AuthData;
import datamodel.UserData;

public class UserService {
    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public AuthData register(UserData user) throws Exception {
        if (dataAccess.getUser(user.username()) != null) {
            throw new Exception("already exists");
        }
        return new AuthData(user.username(), generateAuthToken());
    }

    private String generateAuthToken() {
        return "placeholder";
    }
}
