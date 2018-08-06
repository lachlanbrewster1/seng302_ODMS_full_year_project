package odms.controller.data.database;

import odms.controller.database.DatabaseConnection;
import org.junit.After;
import org.junit.BeforeClass;

public abstract class MySqlCommonTests {

    @BeforeClass
    public static void configureDatabase() {
        DatabaseConnection.setConfig("/config/db_test.config");
    }

    @After
    public void resetDb() {
        DatabaseConnection instance = DatabaseConnection.getInstance();
        instance.resetTestDb();
        DatabaseConnection.setConfig("/config/db_test.config");
    }
}
