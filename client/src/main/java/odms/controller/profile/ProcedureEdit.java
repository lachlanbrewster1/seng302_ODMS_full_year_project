package odms.controller.profile;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import odms.commons.model.enums.OrganEnum;
import odms.commons.model.history.History;
import odms.commons.model.profile.Procedure;
import odms.commons.model.profile.Profile;
import odms.controller.database.DAOFactory;
import odms.controller.database.procedure.HttpProcedureDAO;
import odms.controller.database.procedure.ProcedureDAO;
import odms.view.profile.ProcedureDetailed;
import odms.controller.history.CurrentHistory;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Controller for the procedure edit scene.
 */
public class ProcedureEdit {

    private ProcedureDetailed view;
    private ProcedureDAO procedureDAO = DAOFactory.getProcedureDao();

    /**
     * constructor for ProcedureEdit class. Sets the view component of the controller.
     * @param v the view
     */
    public ProcedureEdit(ProcedureDetailed v) {
        view = v;
    }

    /**
     * Saves the updated procedure.
     * @throws IllegalArgumentException thrown if procedure date is before profiles dob
     */
    public void save() throws IllegalArgumentException {
        Profile profile = view.getProfile();
        Procedure procedure = view.getCurrentProcedure();
        List<OrganEnum> oldAffectedOrgans = procedure.getOrgansAffected();
        List<OrganEnum> newAffectedOrgans = view.getAffectedOrgansListView();
        History action = new History("profile ", profile.getId(), "EDITED",
                "",
                profile.getAllProcedures().indexOf(procedure),
                LocalDateTime.now());
        String oldValues =
                " PREVIOUS(" + procedure.getSummary() + "," + procedure.getDate()
                        + "," +
                        procedure.getLongDescription() + ")" + " OLDORGANS"
                        + procedure.getOrgansAffected();
        procedure.setLongDescription(view.getDescEntry());
        procedure.setSummary(view.getSummaryEntry());

        // date validation
        LocalDate dateOfProcedure = view.getDateOfProcedure();
        LocalDate dob = profile.getDateOfBirth();
        if (dob.isAfter(dateOfProcedure)) {
            throw new IllegalArgumentException();
        } else {
            procedure.setDate(dateOfProcedure);
        }

        procedure.setOrgansAffected(view.getAffectedOrgansListView());
        String newValues =
                " CURRENT(" + procedure.getSummary() + "," + procedure.getDate() + ","
                        +
                        procedure.getLongDescription() + ")" + " NEWORGANS"
                        + procedure.getOrgansAffected();
        action.setHistoryData(oldValues + newValues);
        CurrentHistory.updateHistory(action);

        procedureDAO.update(profile, procedure);
        updateAffectedOrgans(newAffectedOrgans, oldAffectedOrgans);
    }

    /**
     * Checks what affected organs have be added and removed from the procedure, and applies these
     * changes to the database.
     * @param newAffectedOrgans List of the affected organs after the edit
     * @param oldAffectedOrgans List of the affected organs before the edit
     */
    private void updateAffectedOrgans(
            List<OrganEnum> newAffectedOrgans,
            List<OrganEnum> oldAffectedOrgans) {
        Procedure procedure = view.getCurrentProcedure();
        ProcedureDAO procedureDAO = DAOFactory.getProcedureDao();
        List<OrganEnum> organsToBeAdded = new ArrayList<>(newAffectedOrgans);
        organsToBeAdded.removeAll(oldAffectedOrgans);
        List<OrganEnum> organsToBeRemoved = new ArrayList<>(oldAffectedOrgans);
        organsToBeRemoved.removeAll(newAffectedOrgans);

        for (OrganEnum organEnum : organsToBeRemoved) {
            procedureDAO.removeAffectedOrgan(procedure, organEnum);
        }
        for (OrganEnum organEnum : organsToBeAdded) {
            procedureDAO.addAffectedOrgan(procedure, organEnum);
        }
    }

    public Set<OrganEnum> getDonatedOrgans() {
        return view.getProfile().getOrgansDonated();
    }
}
