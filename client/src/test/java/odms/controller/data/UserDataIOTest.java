//package odms.controller.data;
//
//import static org.junit.Assert.assertEquals;
//
//import java.sql.SQLException;
//import odms.commons.model.enums.UserType;
//import odms.commons.model.user.user;
//import odms.controller.data.database.MySqlCommonTests;
//import odms.controller.database.DAOFactory;
//import odms.controller.database.user.UserDAO;
//import org.junit.Before;
//import org.junit.Test;
//
//public class UserDataIOTest extends MySqlCommonTests {
//
//    private UserDAO userDb;
//    private user user1;
//    private user user2;
//
//    @Before
//    public void setup() throws SQLException {
//        userDb = DAOFactory.getUserDao();
//
//        user1 = new user(UserType.CLINICIAN, "John Smith", "Christchurch");
//        user2 = new user(UserType.CLINICIAN, "Matt Smith", "Auckland");
//        userDb.add(user1);
//        userDb.add(user2);
//    }
//
//    @Test
//    public void testSaveAndLoad() throws Exception {
//        assertEquals(userDb.getAll().get(0).getName(), userDb.getAll().get(0).getName());
//    }
//}
////