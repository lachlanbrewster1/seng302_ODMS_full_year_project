package odms.controller.data.database;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

import java.sql.SQLException;
import java.time.LocalDate;

import java.util.List;
import odms.controller.database.DatabaseConnection;
import odms.controller.database.MySqlProcedureDAO;
import odms.controller.database.MySqlProfileDAO;
import odms.model.enums.OrganEnum;
import odms.model.profile.Procedure;
import odms.model.profile.Profile;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MySqlProcedureDaoTest {
    private MySqlProcedureDAO mySqlProcedureDAO;
    private MySqlProfileDAO mySqlProfileDAO;

    private Profile testProfile0 = new Profile("Joshua", "Wyllie", LocalDate.of(1997, 7, 18), "ABC1234");
    private Procedure testProcedurePending = new Procedure("Head Amputation", LocalDate.of(2020, 2, 22), "Head will be removed from neck. Fatal Procedure");
    private Procedure testProcedureNotPending = new Procedure("Head Amputation", LocalDate.of(2001, 2, 22), "Head will be removed from neck. Fatal Procedure");

    /**
     * Sets the Database to the test database and Initialises the DBO
     */
    @Before
    public void setUp() throws SQLException {
        DatabaseConnection.setConfig("/config/db_test.config");
        mySqlProcedureDAO = new MySqlProcedureDAO();
        mySqlProfileDAO = new MySqlProfileDAO();

        mySqlProfileDAO.add(testProfile0);
        testProfile0 = mySqlProfileDAO.get("ABC1234");
        mySqlProcedureDAO.add(testProfile0, testProcedurePending);

    }

    /**
     * Tests adding and getting a procedure by id
     */
    @Test
    public void testAddProcedure() {
        mySqlProcedureDAO.add(testProfile0, testProcedureNotPending);
        assertEquals(1, mySqlProcedureDAO.getAll(testProfile0, false).size());
    }

    /**
     * Tests adding an affected organ to a procedure
     */
    @Test
    public void testAddAffectedOrgan() {
        Procedure procedure= mySqlProcedureDAO.getAll(testProfile0, true).get(0);
        mySqlProcedureDAO.addAffectedOrgan(procedure, OrganEnum.LIVER);
        List<OrganEnum> affectedOrgans = mySqlProcedureDAO.getAffectedOrgans(procedure.getId());
        assertTrue(affectedOrgans.contains(OrganEnum.LIVER));
    }

    /**
     * Tests removing an affected organ to a procedure
     */
    @Test
    public void testRemoveAffectedOrgans() {
        Procedure testProcedure = mySqlProcedureDAO.getAll(testProfile0, true).get(0);
        mySqlProcedureDAO.addAffectedOrgan(testProcedure, OrganEnum.LIVER);
        mySqlProcedureDAO.removeAffectedOrgan(testProcedure, OrganEnum.LIVER);
        int procedureId = testProcedure.getId();
        List<OrganEnum> affectedOrgans = mySqlProcedureDAO.getAffectedOrgans(procedureId);
        assertEquals(0, affectedOrgans.size());
    }

    /**
     * Tests removing a procedure
     */
    @Test
    public void testRemove() {
        mySqlProcedureDAO.remove(mySqlProcedureDAO.getAll(testProfile0, true).get(0));

        List<Procedure> allProcedures = mySqlProcedureDAO.getAll(testProfile0, true);
        assertTrue(allProcedures.isEmpty());
    }

    @Test
    public void testUpdate() {
        Procedure testProcedure = mySqlProcedureDAO.getAll(testProfile0, true).get(0);
        testProcedure.setSummary("gg no re");
        mySqlProcedureDAO.update(testProcedure);
        assertEquals(testProcedure.getSummary(),
                mySqlProcedureDAO.getAll(testProfile0, true).get(0).getSummary());
    }

    /**
     * Sets the database back to the production database
     */
    @After
    public void cleanUp() {
        DatabaseConnection connectionInstance = DatabaseConnection.getInstance();
        connectionInstance.resetTestDb();
    }
}
