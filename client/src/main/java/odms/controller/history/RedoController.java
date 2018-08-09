package odms.controller.history;

import java.util.Collections;

import odms.controller.profile.ProfileUndoRedoCLIServiceController;
import odms.model.data.ProfileDatabase;
import odms.model.enums.OrganEnum;
import odms.model.history.History;
import odms.model.medications.Drug;
import odms.model.profile.Condition;
import odms.model.profile.OrganConflictException;
import odms.model.profile.Profile;
import odms.model.user.User;
import odms.view.LoginView;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class RedoController extends UndoRedoController {

    private static ArrayList<Profile> unaddedProfiles = new ArrayList<>();
    private static int historyPosition;
    private static ArrayList<History> currentSessionHistory;

    /**
     * Redoes an action
     *
     * @param currentDatabase
     */
    public void redo(ProfileDatabase currentDatabase) {
        //todo change the methods to controller functions
        try {
            currentSessionHistory = HistoryController.getHistory();
            historyPosition = HistoryController.getPosition();
            if (historyPosition != currentSessionHistory.size() - 1) {
                historyPosition += 1;
                History action;
                if (historyPosition == 0) {
                    historyPosition = 1;
                    action = currentSessionHistory.get(historyPosition);
                    historyPosition = 0;
                } else {
                    action = currentSessionHistory.get(historyPosition);
                }
                redirect(currentDatabase, action);
                HistoryController.setPosition(historyPosition);
                System.out.println("Command redone");
            } else {
                System.out.println("There are no commands to redo");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("No commands have been entered.");
        }
    }

    /**
     * Redoes a donation being done
     *
     * @param currentDatabase
     * @param action
     */
    public void addedDonated(ProfileDatabase currentDatabase, History action) {
        Profile profile = currentDatabase.getProfile(action.getHistoryId());
        String organ = action.getHistoryData();
        ProfileUndoRedoCLIServiceController.addOrganDonated(OrganEnum.valueOf(organ), profile);
    }

    /**
     * Redoes a received organ being added
     *
     * @param currentDatabase
     * @param action
     */
    public void addedReceived(ProfileDatabase currentDatabase, History action) {
        Profile profile = currentDatabase.getProfile(action.getHistoryId());
        String organ = action.getHistoryData();
        ProfileUndoRedoCLIServiceController.addOrgansReceived(new HashSet<OrganEnum>(
                Collections.singletonList(OrganEnum.valueOf(organ))), profile);
    }

    /**
     * Redoes a conditon being removed
     *
     * @param currentDatabase
     * @param action
     */
    public void removedCondition(ProfileDatabase currentDatabase, History action) {
        Profile profile = currentDatabase.getProfile(action.getHistoryId());
        int c = action.getHistoryDataIndex();
        Condition condition = ProfileUndoRedoCLIServiceController.getCurrentConditions(profile).get(c);
        ProfileUndoRedoCLIServiceController.removeCondition(condition, profile);
    }

    /**
     * redoes an condition being added
     *
     * @param currentDatabase
     * @param action
     */
    public void addCondition(ProfileDatabase currentDatabase, History action) {
        Profile profile = currentDatabase.getProfile(action.getHistoryId());
        String s = action.getHistoryData();
        String[] values = s.split(",");
        Condition condition = new Condition(values[0], values[1], null, Boolean.valueOf(values[2]));
        ProfileUndoRedoCLIServiceController.addCondition(condition, profile);
    }

    /**
     * Redoes a drug being deleted
     *
     * @param currentDatabase
     * @param action
     */
    public void deleteDrug(ProfileDatabase currentDatabase, History action)
            throws IndexOutOfBoundsException {
        Profile profile = currentDatabase.getProfile(action.getHistoryId());
        int d = action.getHistoryDataIndex();
        List<Drug> drugs = profile.getCurrentMedications();
        ProfileUndoRedoCLIServiceController.deleteDrug(drugs.get(d), profile);
    }

    /**
     * Redoes a drug being added to history
     *
     * @param currentDatabase
     * @param action
     */
    public void stopDrug(ProfileDatabase currentDatabase, History action)
            throws IndexOutOfBoundsException {
        Profile profile = currentDatabase.getProfile(action.getHistoryId());
        int d = action.getHistoryDataIndex();
        List<Drug> drugs = profile.getCurrentMedications();
        Drug drug = drugs.get(d);
        ProfileUndoRedoCLIServiceController.moveDrugToHistory(drug, profile);
        LocalDateTime currentTime = LocalDateTime.now();
        History data = new History("profile", profile.getId(), "stopped", drug.getDrugName(),
                profile.getHistoryOfMedication().indexOf(drug), currentTime);
        HistoryController.currentSessionHistory.set(historyPosition - 1, data);
    }

    /**
     * Redoes a drug being added to current
     *
     * @param currentDatabase
     * @param action
     */
    public void renewDrug(ProfileDatabase currentDatabase, History action)
            throws IndexOutOfBoundsException {
        Profile profile = currentDatabase.getProfile(action.getHistoryId());
        int d = action.getHistoryDataIndex();
        List<Drug> drugs = profile.getHistoryOfMedication();
        Drug drug = drugs.get(d);
        ProfileUndoRedoCLIServiceController.moveDrugToCurrent(drug, profile);
        LocalDateTime currentTime = LocalDateTime.now();
        History data = new History("profile", profile.getId(), "started", drug.getDrugName(),
                profile.getCurrentMedications().indexOf(drug), currentTime);
        HistoryController.currentSessionHistory.set(historyPosition - 1, data);
    }

    /**
     * Redoes a drug being added
     *
     * @param currentDatabase
     * @param action
     */
    public void addDrug(ProfileDatabase currentDatabase, History action)
            throws IndexOutOfBoundsException {
        Profile profile = currentDatabase.getProfile(action.getHistoryId());
        if (action.getHistoryAction().contains("history")) {
            String drug = action.getHistoryData();
            Drug d = new Drug(drug);
            ProfileUndoRedoCLIServiceController.addDrug(d, profile);
            ProfileUndoRedoCLIServiceController.moveDrugToHistory(d, profile);
        } else {
            String drug = action.getHistoryData();
            ProfileUndoRedoCLIServiceController.addDrug(new Drug(drug), profile);
        }
    }

    /**
     * Redoes a clinician being updated
     *
     * @param action
     */
    public void updated(History action) {
        User user = LoginView.getCurrentUser();
        String newString = action.getHistoryData()
                .substring(action.getHistoryData().indexOf("new ") + 4);
        String[] newValues = newString.split(",");
        user.setName(newValues[1].replace("name=", ""));
        user.setStaffID(Integer.valueOf(newValues[0].replace("staffId=", "").replace(" ", "")));
        user.setWorkAddress(newValues[2].replace("workAddress=", ""));
        user.setRegion(newValues[3].replace("region=", ""));
    }

    /**
     * Redoes a profile being added
     *
     * @param currentDatabase
     * @param action
     */
    public void added(ProfileDatabase currentDatabase, History action) {
        int oldid = action.getHistoryId();
        int id = currentDatabase
                .restoreProfile(oldid, unaddedProfiles.get(unaddedProfiles.size() - 1));
        unaddedProfiles.remove(unaddedProfiles.get(unaddedProfiles.size() - 1));
        for (int i = 0; i < currentSessionHistory.size() - 1; i++) {
            if (currentSessionHistory.get(i).getHistoryId() == oldid) {
                currentSessionHistory.get(i).setHistoryId(id);
            }
        }
    }

    /**
     * Redoes a profile being deleted
     *
     * @param currentDatabase
     * @param action
     */
    public void deleted(ProfileDatabase currentDatabase, History action) {
        Profile profile = currentDatabase.getProfile(action.getHistoryId());
        currentDatabase.deleteProfile(action.getHistoryId());
        HistoryController.deletedProfiles.add(profile);
    }

    /**
     * Redoes a organ being removed
     *
     * @param currentDatabase
     * @param action
     * @throws Exception
     */
    public void removed(ProfileDatabase currentDatabase, History action) throws Exception {
        Profile profile = currentDatabase.getProfile(action.getHistoryId());
        ProfileUndoRedoCLIServiceController.removeOrgansDonating(OrganEnum.stringListToOrganSet(
                Arrays.asList(action.getHistoryData().replace(' ', '_').split(","))), profile);
    }

    /**
     * Redoes a organ being set
     *
     * @param currentDatabase
     * @param action
     * @throws OrganConflictException
     */
    public void set(ProfileDatabase currentDatabase, History action) throws OrganConflictException {
        Profile profile = currentDatabase.getProfile(action.getHistoryId());
        ProfileUndoRedoCLIServiceController.addOrgansDonating(OrganEnum.stringListToOrganSet(
                Arrays.asList(action.getHistoryData().replace(' ', '_').split(",")
                )), profile);
    }

    /**
     * Redoes an organ being donated
     *
     * @param currentDatabase
     * @param action
     */
    public void donate(ProfileDatabase currentDatabase, History action) {
        Profile profile = currentDatabase.getProfile(action.getHistoryId());
        ProfileUndoRedoCLIServiceController.addOrgansDonated(OrganEnum.stringListToOrganSet(
                Arrays.asList(action.getHistoryData().replace(' ', '_').split(","))), profile);
    }

    /**
     * Redoes a profile being updated
     *
     * @param currentDatabase
     * @param action
     */
    public void update(ProfileDatabase currentDatabase, History action) {
        Profile profile = currentDatabase.getProfile(action.getHistoryId());
        String newInfo = action.getHistoryData()
                .substring(action.getHistoryData().indexOf("new ") + 4);
        ProfileUndoRedoCLIServiceController.setExtraAttributes(new ArrayList<String>(Arrays.asList(newInfo.split(","))), profile);
    }

    /**
     * Redoes a procedure being edited
     *
     * @param currentDatabase
     * @param action
     */
    public void edited(ProfileDatabase currentDatabase, History action) {
        Profile profile = currentDatabase.getProfile(action.getHistoryId());
        int procedurePlace = action.getHistoryDataIndex();
        String previous = action.getHistoryData()
                .substring(action.getHistoryData().indexOf("CURRENT(") + 8);
        String[] previousValues = previous.split(",");
        String organs;
        List<OrganEnum> organList = new ArrayList<>();
        organs = action.getHistoryData();
        List<String> list = new ArrayList<>(Arrays.asList(organs.split(",")));
        for (String organ : list) {
            System.out.println(organ);
            organList.add(OrganEnum.valueOf(organ.replace(" ", "").replace("NEWORGANS[", "")));
        }
        profile.getAllProcedures().get(procedurePlace).setSummary(previousValues[0]);
        profile.getAllProcedures().get(procedurePlace).setDate(LocalDate.parse(previousValues[1]));
        if (previousValues.length == 3) {
            profile.getAllProcedures().get(procedurePlace)
                    .setLongDescription(previousValues[2]);
        }
        profile.getAllProcedures().get(procedurePlace).setOrgansAffected(organList);
    }
}