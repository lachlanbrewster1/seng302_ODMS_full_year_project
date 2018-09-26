package odms.controller.data.database;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.sql.SQLException;
import java.time.LocalDate;
import odms.commons.model.enums.UserType;
import odms.commons.model.profile.Profile;
import odms.commons.model.user.User;
import odms.commons.model.user.UserNotFoundException;
import odms.server.CommonTestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;
import server.model.database.DAOFactory;
import server.model.database.PasswordUtilities;
import server.model.database.middleware.MiddlewareDAO;
import server.model.database.profile.ProfileDAO;
import server.model.database.user.UserDAO;

public class MySqlMiddlewareDAOTest extends CommonTestUtils {

    // Data access objects required.
    private ProfileDAO profileDAO = DAOFactory.getProfileDao();
    private UserDAO userDAO = DAOFactory.getUserDao();
    private MiddlewareDAO middleware = DAOFactory.getMiddlewareDAO();

    // Profile variables.
    private Profile profileA;
    private Profile profileB;
    private Profile profileC;

    // User variables.
    private User userA;
    private User userB;
    private User userC;

    // Token variables.
    int validToken = 32873;
    int invalidToken = 784834;

    // General variables.
    private int invalidId = 0;

    @Before
    public void setup() throws SQLException, UserNotFoundException {
        profileA = new Profile("Brooke", "Rakowitz",
                LocalDate.of(1998, 3, 3), "LPO7236");
        profileDAO.add(profileA);
        profileA = profileDAO.get(profileA.getNhi());

        profileB = new Profile("Tim", "Hamblin",
                LocalDate.of(1998, 3, 3), "YSL9939");
        profileDAO.add(profileB);
        profileB = profileDAO.get(profileB.getNhi());

        profileC = new Profile("Bob", "Marshall",
                LocalDate.of(1998, 3, 3), "GSK726");
        profileDAO.add(profileC);
        profileC = profileDAO.get(profileC.getNhi());

        userA = new User(UserType.CLINICIAN, "Brooke", "Canterbury");
        userA.setUsername("brooker");
        userA.setPassword("test");
        userDAO.add(userA);
        userA = userDAO.get(userA.getUsername());

        userB = new User(UserType.CLINICIAN, "Tim", "Hamblin");
        userB.setUsername("timh");
        userB.setPassword("test");

        userDAO.add(userB);
        userB = userDAO.get(userB.getUsername());

        userC = new User(UserType.CLINICIAN, "Josh", "Wyllie");
        userC.setUsername("joshw");
        userC.setPassword("test");
        userDAO.add(userC);
        userC = userDAO.get(userC.getUsername());

        PowerMockito.stub(
                PowerMockito.method(PasswordUtilities.class, "getSaltedHash")
        ).toReturn("test");
    }

    @Test
    public void testSetProfileTokenValid() throws SQLException {
        middleware.setProfileToken(profileA.getId(), validToken);
        assertTrue(middleware.isProfileAuthenticated(profileA.getId(), validToken));
    }

    @Test
    public void testSetUserTokenValid() throws SQLException {
        middleware.setUserToken(userA.getStaffID(), validToken);
        assertTrue(middleware.isUserAuthenticated(userA.getStaffID(), validToken));
    }

    @Test
    public void testSetProfileTokenInvalid() throws SQLException {
        middleware.setProfileToken(invalidId, validToken);
        assertFalse(middleware.isProfileAuthenticated(invalidId, validToken));
    }

    @Test
    public void testSetUserTokenInvalid() throws SQLException {
        middleware.setUserToken(invalidId, validToken);
        assertFalse(middleware.isUserAuthenticated(invalidId, validToken));
    }

    @Test
    public void testIsProfileAuthenticatedValid() throws SQLException {
        middleware.setProfileToken(profileB.getId(), validToken);
        assertTrue(middleware.isProfileAuthenticated(profileB.getId(), validToken));
    }

    @Test
    public void testIsUserAuthenticatedValid() throws SQLException {
        middleware.setUserToken(userB.getStaffID(), validToken);
        assertTrue(middleware.isUserAuthenticated(userB.getStaffID(), validToken));
    }

    @Test
    public void testIsProfileAuthenticatedInvalid() throws SQLException {
        middleware.setProfileToken(profileB.getId(), validToken);
        assertFalse(middleware.isProfileAuthenticated(profileB.getId(), invalidToken));
    }

    @Test
    public void testIsUserAuthenticatedInvalid() throws SQLException {
        middleware.setUserToken(userB.getStaffID(), validToken);
        assertFalse(middleware.isUserAuthenticated(userB.getStaffID(), invalidToken));
    }

    @Test
    public void testDeleteProfileTokenValid() throws SQLException {
        middleware.setProfileToken(profileC.getId(), validToken);
        assertTrue(middleware.isProfileAuthenticated(profileC.getId(), validToken));
        middleware.deleteProfileToken(profileC.getId());
        assertFalse(middleware.isProfileAuthenticated(profileC.getId(), validToken));
    }

    @Test
    public void testDeleteUserTokenValid() throws SQLException {
        middleware.setUserToken(userC.getStaffID(), validToken);
        assertTrue(middleware.isUserAuthenticated(userC.getStaffID(), validToken));
        middleware.deleteUserToken(userC.getStaffID());
        assertFalse(middleware.isUserAuthenticated(userC.getStaffID(), validToken));
    }

    @Test
    public void testDeleteProfileTokenInvalid() throws SQLException {
        middleware.setProfileToken(invalidId, validToken);
        assertFalse(middleware.isProfileAuthenticated(invalidId, validToken));
        middleware.deleteProfileToken(invalidId);
        assertFalse(middleware.isProfileAuthenticated(invalidId, validToken));
    }

    @Test
    public void testDeleteUserTokenInvalid() throws SQLException {
        middleware.setUserToken(invalidId, validToken);
        assertFalse(middleware.isUserAuthenticated(invalidId, validToken));
        middleware.deleteUserToken(invalidId);
        assertFalse(middleware.isUserAuthenticated(invalidId, validToken));
    }

    @After
    public void tearDown() throws SQLException {
        // Profile teardown.
        profileDAO.remove(profileA);
        profileDAO.remove(profileB);
        profileDAO.remove(profileC);

        // User teardown.
        userDAO.remove(userA);
        userDAO.remove(userB);
        userDAO.remove(userC);
    }

}