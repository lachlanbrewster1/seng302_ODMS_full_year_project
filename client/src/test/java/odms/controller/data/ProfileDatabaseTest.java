package odms.controller.data;

import java.util.List;
import odms.model.data.NHIConflictException;
import odms.model.data.ProfileDatabase;
import odms.model.profile.Profile;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class ProfileDatabaseTest {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();
    private ProfileDatabase profileDb;
    private Profile profileOne;
    private Profile profileTwo;
    private Profile profileThree;
    private Profile profileFour;
    private Profile profileFive;

    @Before
    public void setup() {
        // Create profile Database with basic profile
        profileDb = new ProfileDatabase();

        List<String> profileOneAttr = new ArrayList<>();
        profileOneAttr.add("given-names=\"John\"");
        profileOneAttr.add("last-names=\"Wayne\"");
        profileOneAttr.add("dob=\"17-01-1998\"");
        profileOneAttr.add("nhi=\"123456879\"");

        List<String> profileTwoAttr = new ArrayList<>();
        profileTwoAttr.add("given-names=\"Sam\"");
        profileTwoAttr.add("last-names=\"Sick\"");
        profileTwoAttr.add("dob=\"17-01-1997\"");
        profileTwoAttr.add("nhi=\"123456878\"");

        List<String> profileThreeAttr = new ArrayList<>();
        profileThreeAttr.add("given-names=\"Sam\"");
        profileThreeAttr.add("last-names=\"Vladko\"");
        profileThreeAttr.add("dob=\"17-01-1997\"");
        profileThreeAttr.add("nhi=\"123456877\"");

        List<String> profileFourAttr = new ArrayList<>();
        profileFourAttr.add("given-names=\"Reece\"");
        profileFourAttr.add("last-names=\"Smith\"");
        profileFourAttr.add("dob=\"17-01-1997\"");
        profileFourAttr.add("nhi=\"123456876\"");

        List<String> profileFiveAttr = new ArrayList<>();
        profileFiveAttr.add("given-names=\"Zu\"");
        profileFiveAttr.add("last-names=\"Tiu\"");
        profileFiveAttr.add("dob=\"17-01-1997\"");
        profileFiveAttr.add("nhi=\"123456875\"");

        try {
            profileOne = new Profile(profileOneAttr);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        try {
            profileTwo = new Profile(profileTwoAttr);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        try {
            profileThree = new Profile(profileThreeAttr);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        try {
            profileFour = new Profile(profileFourAttr);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        try {
            profileFive = new Profile(profileFiveAttr);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testAddProfile() {
        try {
            profileDb.addProfile(profileOne);

            assertEquals(profileDb.getProfile(0).getGivenNames(), "John");
            assertEquals(profileDb.getProfile(0).getLastNames(), "Wayne");
            profileDb.addProfile(profileTwo);

            assertEquals(profileDb.getProfile(1).getGivenNames(), "Sam");
            assertEquals(profileDb.getProfile(1).getLastNames(), "Sick");
            assertTrue("Population should be 2", profileDb.getProfilePopulation() == 2);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testDeleteProfile() {
        try {
            profileDb.addProfile(profileOne);
            profileDb.addProfile(profileTwo);
            assertTrue("Population should be 2", profileDb.getProfilePopulation() == 2);

            profileDb.deleteProfile(0);
            assertNull(profileDb.getProfile(0));

            assertEquals(profileDb.getProfile(1).getGivenNames(), "Sam");
            assertEquals(profileDb.getProfile(1).getLastNames(), "Sick");
            assertTrue("Population should be 1", profileDb.getProfilePopulation() == 1);

            profileDb.deleteProfile(1);
            assertTrue("Population should be 0", profileDb.getProfilePopulation() == 0);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetProfilePopulation() {
        try {
            profileDb.addProfile(profileOne);
            profileDb.addProfile(profileTwo);
            assertTrue("Population should be 2", profileDb.getProfilePopulation() == 2);

            profileDb.deleteProfile(0);
            assertTrue("Population should be 1", profileDb.getProfilePopulation() == 1);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetProfiles() {
        List<Profile> testResults;

        try {
            profileDb.addProfile(profileOne);
            profileDb.addProfile(profileTwo);
            testResults = profileDb.getProfiles(false);
            assertTrue("Should be 2 results", testResults.size() == 2);

            // Check sorting works as intended
            assertEquals(testResults.get(0).getLastNames(), "Sick");
            assertEquals(testResults.get(1).getLastNames(), "Wayne");

            testResults = profileDb.getProfiles(true);
            assertTrue("Should be 0 results", testResults.size() == 0);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCheckNhiExists() throws NHIConflictException {
        thrown.expect(NHIConflictException.class);
        thrown.expectMessage("IRD number already in use");

        profileDb.addProfile(profileOne);
        profileDb.addProfile(profileOne);
    }

    //TODO Add new test cases when preferred names are added
    @Test
    public void TestSearchProfiles_noProfiles() {
        // Since the fuzzy search is implemented by an external library it will be hard to test what names match the
        // string. So tests will be based on strings that definitely should or shouldn't be matched.
        List<Profile> testResults;

        try {
            // No profiles in db, so no results
            testResults = profileDb.searchProfiles("Sam Sick", -999, -999, "", "", "", null);
            assertTrue(testResults.size() == 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void TestSearchProfiles_fullName() {
        // Since the fuzzy search is implemented by an external library it will be hard to test what names match the
        // string. So tests will be based on strings that definitely should or shouldn't be matched.
        List<Profile> testResults;

        try {
            profileDb.addProfile(profileOne);
            profileDb.addProfile(profileTwo);
            profileDb.addProfile(profileThree);
            profileDb.addProfile(profileFour);
            profileDb.addProfile(profileFive);

            // Top result should be profile Sam Sick, next result Sam Vladko. No other results.
            testResults = profileDb.searchProfiles("Sam Sick", -999, -999, "", "", "", null);
            assertTrue(testResults.size() == 2);
            assertEquals(profileTwo, testResults.get(0));
            assertEquals(profileThree, testResults.get(1));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void TestSearchProfiles_singleCharNoResults() {
        // Since the fuzzy search is implemented by an external library it will be hard to test what names match the
        // string. So tests will be based on strings that definitely should or shouldn't be matched.
        List<Profile> testResults;

        try {
            profileDb.addProfile(profileOne);
            profileDb.addProfile(profileTwo);
            profileDb.addProfile(profileThree);
            profileDb.addProfile(profileFour);
            profileDb.addProfile(profileFive);

            // Should contain no results because no names start with 'a'
            testResults = profileDb.searchProfiles("a", -999, -999, "", "", "", null);
            assertTrue(testResults.size() == 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void TestSearchProfiles_singleCharResults() {
        // Since the fuzzy search is implemented by an external library it will be hard to test what names match the
        // string. So tests will be based on strings that definitely should or shouldn't be matched.
        List<Profile> testResults;

        try {
            profileDb.addProfile(profileOne);
            profileDb.addProfile(profileTwo);
            profileDb.addProfile(profileThree);
            profileDb.addProfile(profileFour);
            profileDb.addProfile(profileFive);

            // Should contain sam sick, reece smith then sam vladko
            testResults = profileDb.searchProfiles("s", -999, -999, "", "", "", null);
            assertTrue(testResults.size() == 3);
            assertEquals(testResults.get(0), profileFour);
            assertEquals(testResults.get(1), profileTwo);
            assertEquals(testResults.get(2), profileThree);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void TestSearchProfiles_lastName() {
        // Since the fuzzy search is implemented by an external library it will be hard to test what names match the
        // string. So tests will be based on strings that definitely should or shouldn't be matched.
        List<Profile> testResults;

        try {
            profileDb.addProfile(profileOne);
            profileDb.addProfile(profileTwo);
            profileDb.addProfile(profileThree);
            profileDb.addProfile(profileFour);
            profileDb.addProfile(profileFive);

            // Should contain Zu Tiu, but no other profiles.
            testResults = profileDb.searchProfiles("Tiu", -999, -999, "", "", "", null);
            assertTrue(testResults.size() == 1);
            assertEquals(testResults.get(0), profileFive);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void TestSearchProfiles_firstName() {
        // Since the fuzzy search is implemented by an external library it will be hard to test what names match the
        // string. So tests will be based on strings that definitely should or shouldn't be matched.
        List<Profile> testResults;

        try {
            profileDb.addProfile(profileOne);
            profileDb.addProfile(profileTwo);
            profileDb.addProfile(profileThree);
            profileDb.addProfile(profileFour);
            profileDb.addProfile(profileFive);

            // Should contain sam sick, reece smith then sam vladko
            testResults = profileDb.searchProfiles("sam", -999, -999, "", "", "", null);
            assertTrue(testResults.size() == 3);
            assertEquals(testResults.get(0), profileTwo);
            assertEquals(testResults.get(1), profileThree);
            assertEquals(testResults.get(2), profileFour);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void TestSearchProfiles_preferredName() {
        // Since the fuzzy search is implemented by an external library it will be hard to test what names match the
        // string. So tests will be based on strings that definitely should or shouldn't be matched.
        List<Profile> testResults;

        try {
            profileFive.setPreferredName("dragon");
            profileDb.addProfile(profileOne);
            profileDb.addProfile(profileTwo);
            profileDb.addProfile(profileThree);
            profileDb.addProfile(profileFour);
            profileDb.addProfile(profileFive);

            // Should contain Zu Tiu only, because the preferred name will be matched.
            testResults = profileDb.searchProfiles("dragon", -999, -999, "", "", "", null);
            assertTrue(testResults.size() == 1);
            assertEquals(testResults.get(0), profileFive);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}