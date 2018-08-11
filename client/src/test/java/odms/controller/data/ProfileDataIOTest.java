package odms.controller.data;

import odms.data.NHIConflictException;
import odms.data.ProfileDatabase;
import odms.model.profile.Profile;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class ProfileDataIOTest {

    private ProfileDatabase profileDb;
    private Profile profileOne;

    @Before
    public void setup() {
        // Create profile Database with basic profile
        profileDb = new ProfileDatabase();

        ArrayList<String> profileOneAttr = new ArrayList<>();
        profileOneAttr.add("given-names=\"John\"");
        profileOneAttr.add("last-names=\"Wayne\"");
        profileOneAttr.add("dob=\"17-01-1998\"");
        profileOneAttr.add("nhi=\"123456879\"");

        try {
            profileOne = new Profile(profileOneAttr);
            profileDb.addProfile(profileOne);

        } catch (NHIConflictException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testSaveAndLoad() {
        ProfileDatabase loadedDb;
        ProfileDataIO.saveData(profileDb, "CommandUtilsTest.json");

        loadedDb = ProfileDataIO.loadDataFromJSON("CommandUtilsTest.json");

        assertEquals(
                profileDb.getProfile(0).getGivenNames(),
                loadedDb.getProfile(0).getGivenNames()
        );

        try {
            Files.deleteIfExists(Paths.get("CommandUtilsTest.json"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
