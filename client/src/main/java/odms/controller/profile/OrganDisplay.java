package odms.controller.profile;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import odms.commons.model.enums.OrganEnum;
import odms.commons.model.profile.Profile;
import odms.controller.CommonController;
import odms.controller.database.DAOFactory;

/**
 * Controller class for the organ display view.
 */
@Slf4j
public class OrganDisplay extends CommonController {

    private final odms.view.profile.OrganDisplay view;

    /**
     * Constructs a OrganDisplay object. Sets the view variable.
     *
     * @param view the OrganDisplay view.
     */
    public OrganDisplay(odms.view.profile.OrganDisplay view) {
        this.view = view;
    }

    /**
     * Gets profile data from the database. This is to confirm is the donating organs are expired or
     * not.
     *
     * @param p current profile that is being viewed
     * @return the updated profile object
     */
    public Profile getUpdatedProfileDetails(Profile p) {
        Profile profile = null;
        try {
            profile = DAOFactory.getProfileDao().get(p.getId());
            profile.addOrgansRequired(DAOFactory.getOrganDao().getRequired(p));
            odms.controller.user.AvailableOrgans controller = new odms.controller.user.AvailableOrgans();
            List<Map.Entry<Profile, OrganEnum>> availableOrgans =
                    controller.getAllOrgansAvailable();
            for (Map.Entry<Profile, OrganEnum> m : availableOrgans) {
                controller.checkOrganExpired(m.getValue(), m.getKey());
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }

        return profile;
    }
}
