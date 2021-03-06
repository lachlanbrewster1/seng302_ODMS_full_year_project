package server.model.database.condition;

import java.util.List;
import odms.commons.model.profile.Condition;

public interface ConditionDAO {

    /**
     * Get all conditions for the profile.
     * @param profile to get the conditions for.
     * @param chronic true if the conditions required are chronic.
     * @return the list of conditions for the profile.
     */
    List<Condition> getAll(int profile, boolean chronic);

    /**
     * Get all conditions for the profile.
     * @param profile to get the conditions for.
     * @return the list of conditions for the profile.
     */
    List<Condition> getAll(int profile);

    /**
     * Add a new condition to a profile.
     * @param profile to add the condition to.
     * @param condition to add.
     */
    void add(int profile, Condition condition);

    /**
     * Remove a condition from a profile.
     * @param condition to remove.
     */
    void remove(Condition condition);

    /**
     * Update a condition for the profile.
     * @param condition to updateCountries.
     */
    void update(Condition condition);
}
