package odms.controller.user;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import lombok.extern.slf4j.Slf4j;
import odms.commons.model.profile.Profile;
import odms.controller.database.DAOFactory;
import odms.commons.model.enums.OrganEnum;
import odms.controller.database.profile.ProfileDAO;

/**
 * Search class handles searching for profiles and sorting the results.
 */
@Slf4j
public class Search {

    private List<Profile> profileSearchResults = new ArrayList<>();

    /**
     * Gets a sorted list of profiles, the list contains profiles based off of the criteria set by
     * the params.
     * @param selectedOrgans Organs to search by
     * @param selectedType string value that contains 'donor' or 'receiver'
     * @param selectedGender gender to search by
     * @param searchString name to search by
     * @param regionSearchString region to search by
     * @param ageSearchString age to search by
     * @param ageRangeString upper age range to search by
     * @param selected true if upper age range should used
     * @return sorted list of profiles
     */
    public List<Profile> performSearch(
            ObservableList<OrganEnum> selectedOrgans, String selectedType,
            String selectedGender, String searchString, String regionSearchString,
            String ageSearchString, String ageRangeString, boolean selected) {

        if (selectedOrgans.isEmpty() && selectedType.equals("any") && selectedGender.equals("any") &&
                searchString.equals("") && regionSearchString.equals("") &&
                ageRangeString.equals("")) {
            return new ArrayList<>();
        }

        int ageRangeSearchInt;
        int ageSearchInt;

        if(!ageSearchString.equals("")) {
            ageSearchInt = Integer.parseInt(ageSearchString);
        } else {
            ageSearchInt = -999;
        }

        if (selected) {
            if (!ageRangeString.equals("")) {
                ageRangeSearchInt = Integer.parseInt(ageRangeString);
            } else {
                ageRangeSearchInt = -999;
            }
        } else {
            ageRangeSearchInt = -999;
        }


        profileSearchResults.clear();

        try {
             profileSearchResults.addAll(DAOFactory.getProfileDao().search(
                    searchString,
                    ageSearchInt,
                    ageRangeSearchInt,
                    regionSearchString,
                    selectedGender,
                    selectedType,
                    new HashSet<>(selectedOrgans)
            ));
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }

        return sortSearchResults(searchString);

    }

    /**
     * Returns the string value to populate the Donor/Receiver column in the clinician search
     * table.
     *
     * @param profile profile object to pull data from
     * @return a string depicting whether to profile is a donor, receiver, or both.
     */
    public SimpleStringProperty donorReceiverProperty(Profile profile) {
        SimpleStringProperty result = new SimpleStringProperty();
        if ((profile.getDonor() != null) && profile.getDonor()) {
            if (profile.isReceiver()) {
                result.setValue("Donor/Receiver");
            } else {
                result.setValue("Donor");
            }
        } else if (profile.isReceiver()) {
            result.setValue("Receiver");

        }
        return result;
    }

    /**
     * Takes the list of search results and orders them by last, preferred and first name. The
     * results whose names don't contain the search string and the start of their name are added
     * last. Theses subsets are also alphabetically ordered.
     * @param searchString the string that names are being checked against
     * @return Ordered list of profiles
     */
    private List<Profile> sortSearchResults(String searchString) {
        List<Profile> results = new ArrayList<>();
        List<Profile> profilesMatchesLast = new ArrayList<>();
        List<Profile> profilesMatchesPreferred = new ArrayList<>();
        List<Profile> profilesMatchesFirst = new ArrayList<>();
        List<Profile> profilesMatchesOther = new ArrayList<>();

        for (Profile profile : profileSearchResults) {
            if (profile.getLastNames().toLowerCase().startsWith(searchString.toLowerCase())) {
                profilesMatchesLast.add(profile);
            } else if (profile.getPreferredName() != null &&
                    profile.getPreferredName().toLowerCase().startsWith(searchString.toLowerCase())) {
                profilesMatchesPreferred.add(profile);
            } else if (profile.getGivenNames().toLowerCase().startsWith(searchString.toLowerCase())) {
                profilesMatchesFirst.add(profile);
            } else {
                profilesMatchesOther.add(profile);
            }
        }

        Collections.sort(profilesMatchesLast);
        Collections.sort(profilesMatchesPreferred);
        Collections.sort(profilesMatchesFirst);
        Collections.sort(profilesMatchesOther);

        results.addAll(profilesMatchesLast);
        results.addAll(profilesMatchesPreferred);
        results.addAll(profilesMatchesFirst);
        results.addAll(profilesMatchesOther);

        return results;
    }

    public String getNumberOfProfiles() throws SQLException {
        ProfileDAO database = DAOFactory.getProfileDao();
        return String.valueOf(database.size());
    }
}
