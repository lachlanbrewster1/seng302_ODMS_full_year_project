package server.model.database.user;

import java.sql.SQLException;
import java.util.List;
import odms.commons.model.user.User;
import odms.commons.model.user.UserNotFoundException;

/**
 * Interface containg UserDAO methods.
 */
public interface UserDAO {

    /**
     * Gets all users from the database.
     */
    List<User> getAll() throws SQLException;

    /**
     * Gets a single user from the database by id.
     * @param userId of the user.
     * @return the specified user.
     * @throws UserNotFoundException error.
     */
    User get(int userId) throws UserNotFoundException, SQLException;

    /**
     * Gets a single user from the database by username.
     * @param username of the user.
     * @return the specified user.
     * @throws UserNotFoundException error.
     */
    User get(String username) throws UserNotFoundException, SQLException;

    /**
     * Adds a new user to the database.
     * @param user to add.
     */
    void add(User user) throws SQLException;

    /**
     * Checks if a username already exists in the database.
     * @param username to check.
     * @return true is the username does not already exist.
     */
    boolean isUniqueUsername(String username) throws SQLException;

    /**
     * Removes a user from the database.
     * @param user to remove.
     */
    void remove(User user) throws SQLException;

    /**
     * Updates a users information in the database.
     * @param user to updateCountries.
     */
    void update(User user) throws SQLException;

    /**
     * Checks a users credentials.
     * @param username username.
     * @param password password.
     * @return True if user is valid.
     * @throws SQLException thrown on sql error.
     * @throws UserNotFoundException thrown when the user is not found.
     */
    Boolean checkCredentials(String username, String password) throws
            SQLException, UserNotFoundException;

}
