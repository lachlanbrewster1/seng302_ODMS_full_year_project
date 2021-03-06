package odms.server;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import server.model.database.DatabaseConnection;

public abstract class CommonTestUtils {

    @BeforeClass
    public static void configureDatabase() {
        System.setProperty("com.mchange.v2.log.MLog", "com.mchange.v2.log.FallbackMLog");
        System.setProperty("com.mchange.v2.log.FallbackMLog.DEFAULT_CUTOFF_LEVEL", "WARNING");

        DatabaseConnection.setTestDb();
    }

    @AfterClass
    public static void resetDb() {
        DatabaseConnection instance = DatabaseConnection.getInstance();
        instance.resetTestDb();
    }
}
