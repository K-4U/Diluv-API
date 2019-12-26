package com.diluv.api.database;

import java.sql.Timestamp;
import java.util.List;

import com.diluv.api.database.dao.UserDAO;
import com.diluv.api.database.record.TempUserRecord;
import com.diluv.api.database.record.UserRecord;
import com.diluv.api.utils.FileReader;

public class UserTestDatabase implements UserDAO {

    private final List<UserRecord> userList;
    private final List<TempUserRecord> tempUsersList;

    public UserTestDatabase () {

        this.userList = FileReader.readJsonFolder("records/users", UserRecord.class);
        this.tempUsersList = FileReader.readJsonFolder("records/temp_users", TempUserRecord.class);
    }

    @Override
    public Long findUserIdByEmail (String email) {

        for (UserRecord userRecord : userList) {
            if (userRecord.getEmail().equals(email)) {
                return userRecord.getId();
            }
        }

        return null;
    }

    @Override
    public Long findUserIdByUsername (String username) {

        for (UserRecord userRecord : userList) {
            if (userRecord.getUsername().equals(username)) {
                return userRecord.getId();
            }
        }

        return null;
    }

    @Override
    public UserRecord findOneByUsername (String username) {

        for (UserRecord userRecord : userList) {
            if (userRecord.getUsername().equals(username)) {
                return userRecord;
            }
        }

        return null;
    }

    @Override
    public boolean insertUser (String email, String username, String password, String passwordType, Timestamp timestamp) {

        return true;
    }

    @Override
    public boolean insertUserRefresh (long userId, String randomKey, Timestamp time) {

        return true;
    }

    @Override
    public boolean existTempUserByEmail (String email) {

        for (TempUserRecord userRecord : this.tempUsersList) {
            if (userRecord.getEmail().equals(email)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean existTempUserByUsername (String username) {

        for (TempUserRecord userRecord : this.tempUsersList) {
            if (userRecord.getUsername().equals(username)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean insertTempUser (String email, String username, String password, String passwordType, String verificationCode) {

        return true;
    }

    @Override
    public TempUserRecord findTempUserByEmailAndUsername (String email, String username) {

        for (TempUserRecord record : this.tempUsersList) {
            if (record.getEmail().equalsIgnoreCase(email) &&
                record.getUsername().equalsIgnoreCase(username)) {
                return record;
            }
        }
        return null;
    }

    @Override
    public TempUserRecord findTempUserByEmailAndUsernameAndCode (String email, String username, String code) {

        for (TempUserRecord record : this.tempUsersList) {
            if (record.getEmail().equalsIgnoreCase(email) &&
                record.getUsername().equalsIgnoreCase(username) &&
                record.getVerificationCode().equalsIgnoreCase(code)) {
                return record;
            }
        }
        return null;
    }

    @Override
    public boolean deleteTempUser (String email, String username) {

        return true;
    }
}
