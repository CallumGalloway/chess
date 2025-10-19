package dataaccess;

import datamodel.*;

public interface DataAccess {
    void clear();
    void createUser(UserData user);
    UserData getUser(String username);
    void addAuth(AuthData auth);
    String getAuthUser(String auth);
    void delAuth(String auth);
}
