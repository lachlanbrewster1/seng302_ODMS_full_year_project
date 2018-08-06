package odms.cli.commands;

import java.sql.SQLException;
import java.util.List;
import odms.cli.CommandUtils;
import odms.controller.database.DAOFactory;
import odms.controller.database.ProfileDAO;
import odms.controller.history.HistoryController;
import odms.controller.profile.ProfileGeneralControllerTODOContainsOldProfileMethods;
import odms.model.data.NHIConflictException;
import odms.model.data.ProfileDatabase;
import odms.model.history.History;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;

public class Profile extends CommandUtils {

    /**
     * Add history for profile.
     * @param id profile ID.
     */
    protected static void addProfileHistory(Integer id) {
        History action = new History("profile", id, "added", "", -1, LocalDateTime.now());
        HistoryController.updateHistory(action);
    }

    /**
     * Create profile.
     * @param rawInput raw command input.
     */
    public static void createProfile(String rawInput) throws SQLException {
        ProfileDAO database = DAOFactory.getProfileDao();

        try {
            String[] attrList = rawInput.substring(15).split("\"\\s");
            ArrayList<String> attrArray = new ArrayList<>(Arrays.asList(attrList));
            odms.model.profile.Profile newProfile = new odms.model.profile.Profile(attrArray);
            if (database.isUniqueNHI(newProfile.getNhi()) == 0) {
                database.add(newProfile);
                addProfileHistory(newProfile.getId());
                System.out.println("profile created.");
            }
            else {
                throw new NHIConflictException("NHI already in use.",
                        String.valueOf(database.isUniqueNHI(newProfile.getNhi())));
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Please enter the required attributes correctly.");

        } catch (NHIConflictException e) {
            String errorNhiNumber = e.getNHI();
            odms.model.profile.Profile errorProfile = database.get(database
                    .isUniqueNHI(errorNhiNumber));

            System.out.println("Error: NHI " + errorNhiNumber +
                    " already in use by profile " +
                    errorProfile.getGivenNames() + " " +
                    errorProfile.getLastNames());

        } catch (Exception e) {
            System.out.println("Please enter a valid command.");
        }
    }

    /**
     * Delete profiles from the database.
     * @param expression search expression.
     */
    public static void deleteProfileBySearch(String expression) {
        List<odms.model.profile.Profile> profiles = search(expression);
        try {
            deleteProfiles(profiles);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Delete profiles.
     * @param profileList list of profiles.
     */
    private static void deleteProfiles(List<odms.model.profile.Profile> profileList) throws SQLException {
        ProfileDAO database = DAOFactory.getProfileDao();
        boolean result;
        if (profileList.size() > 0) {
            for (odms.model.profile.Profile profile : profileList) {
                database.remove(profile);
                HistoryController.deletedProfiles.add(profile);
                HistoryController.updateHistory(new History("profile", profile.getId(),
                        "deleted", "", -1, LocalDateTime.now()));
            }
        } else {
            System.out.println(searchNotFoundText);
        }
    }

    /**
     * Update profile attributes.
     * @param profileList list of profiles
     * @param attrList attributes to be updated and their values
     */
    private static void updateProfileAttr(List<odms.model.profile.Profile> profileList,
            String[] attrList) {
        if (profileList.size() > 0) {
            ArrayList<String> attrArray = new ArrayList<>(Arrays.asList(attrList));
            for (odms.model.profile.Profile profile : profileList) {
                History action = new History("profile", profile.getId(), "update",
                        profile.getAttributesSummary(), -1, null);
                ProfileGeneralControllerTODOContainsOldProfileMethods.setExtraAttributes(attrArray, profile);
                action.setHistoryData(action.getHistoryData() + profile.getAttributesSummary());
                action.setHistoryTimestamp(LocalDateTime.now());
                HistoryController.updateHistory(action);

            }
        } else {
            System.out.println(searchNotFoundText);
        }
    }

    /**
     * Update profile attributes.
     * @param expression search expression.
     */
    public static void updateProfilesBySearch(String expression) {
        String[] attrList = expression.substring(expression.indexOf('>') + 1)
                .trim()
                .split("\"\\s");
        List<odms.model.profile.Profile> profiles = search(expression);
        updateProfileAttr(profiles, attrList);
    }

    /**
     * Displays profiles attributes via the search methods.
     * @param expression search expression being used for searching.
     */
    public static void viewAttrBySearch(String expression) {
        List<odms.model.profile.Profile> profiles = search(expression);
        Print.printProfileSearchResults(profiles);
    }

    /**
     * view date and time of profile creation.
     * @param expression search expression.
     */
    public static void viewDateTimeCreatedBySearch(String expression) {
        List<odms.model.profile.Profile> profiles = search(expression);
        Print.printProfileList(profiles);
    }

    private static List<odms.model.profile.Profile> search(String expression) {
        ProfileDAO database = DAOFactory.getProfileDao();
        List<odms.model.profile.Profile> profiles = new ArrayList<>();
        if (expression.lastIndexOf("=") == expression.indexOf("=")) {
            String attr = expression.substring(expression.indexOf("\"") + 1,
                    expression.lastIndexOf("\""));

            if (expression.substring(8, 8 + "given-names".length()).equals("given-names") ||
                    expression.substring(8, 8 + "last-names".length()).equals("last-names") ||
                    expression.substring(8, 8 + "nhi".length()).equals("nhi")) {

                try {
                     profiles = database.search(attr, 0,
                            0, null, null, null, null);

                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } else {
            System.out.println(searchErrorText);
        }
        return profiles;
    }

    /**
     * view organs available for donation.
     * @param expression search expression.
     */
    public static void viewDonationsBySearch(String expression) {
        List<odms.model.profile.Profile> profiles = search(expression);

        if (profiles != null && !profiles.isEmpty()) {
            Print.printProfileDonations(profiles);
        } else {
            System.out.println("No matching profiles found.");
        }
    }
}
