//package odms.controller.history;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Set;
//import odms.commons.model.enums.OrganEnum;
//import odms.commons.model.history.History;
//import odms.commons.model.medications.Drug;
//import odms.commons.model.profile.Condition;
//import odms.commons.model.profile.profile;
//import odms.commons.model.user.user;
//import odms.controller.profile.UndoRedoCLIService;
//import odms.view.LoginView;
//
//public class Undo extends UndoRedo {
//
//    private static ArrayList<profile> unaddedProfiles = new ArrayList<>();
//    private static int historyPosition;
//    private static ArrayList<History> currentSessionHistory;
//
//    /**
//     * Performs logic for undoes
//     *
//     * @param currentDatabase
//     */
//    public void undo(ProfileDatabase currentDatabase) {
//        //todo change the methods to controller functions
//        historyPosition = CurrentHistory.getPosition();
//        currentSessionHistory = CurrentHistory.getHistory();
//        try {
//            History action = currentSessionHistory.get(historyPosition);
//            if (action != null) {
//                redirect(currentDatabase, action);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            System.out.println("No commands have been entered");
//        }
//    }
//
//    /**
//     * Undoes organs being donated
//     *
//     * @param currentDatabase
//     * @param action
//     */
//    public void addedDonated(ProfileDatabase currentDatabase, History action) {
//        profile profile = currentDatabase.getProfile(action.getHistoryId());
//        String organ = action.getHistoryData();
//        UndoRedoCLIService.removeOrganDonated(OrganEnum.valueOf(organ), profile);
//    }
//
//    /**
//     * Undoes organs being received
//     *
//     * @param currentDatabase
//     * @param action
//     */
//    public void addedReceived(ProfileDatabase currentDatabase, History action) {
//        profile profile = currentDatabase.getProfile(action.getHistoryId());
//        String organ = action.getHistoryData();
//        UndoRedoCLIService.removeOrganReceived(OrganEnum.valueOf(organ), profile);
//    }
//
//    /**
//     * Undoes removed conditions
//     *
//     * @param currentDatabase
//     * @param action
//     */
//    public void removedCondition(ProfileDatabase currentDatabase, History action) {
//        profile profile = currentDatabase.getProfile(action.getHistoryId());
//        String s = action.getHistoryData();
//        String[] values = s.split(",");
//        String diagDate = values[1].substring(8) + "-" + values[1].substring(5, 7) + "-" + values[1]
//                .substring(0, 4);
//        String cureDate = null;
//        System.out.println(diagDate);
//        if (!values[3].equals("null")) {
//            cureDate = values[3].substring(8) + "-" + values[3].substring(5, 7) + "-" + values[3]
//                    .substring(0, 4);
//        }
//        Condition condition = new Condition(values[0], diagDate, cureDate,
//                Boolean.valueOf(values[2]));
//        UndoRedoCLIService.addCondition(condition, profile);
//        LocalDateTime currentTime = LocalDateTime.now();
//        History newAction = new History("Donor", profile.getId(), "removed condition",
//                condition.getName() + "," + condition.getDateOfDiagnosis() + "," + condition
//                        .getChronic() + "," +
//                        condition.getDateCuredString(),
//                UndoRedoCLIService.getCurrentConditions(profile).indexOf(condition), currentTime);
//        currentSessionHistory.set(historyPosition, newAction);
//        if (historyPosition > 0) {
//            historyPosition -= 1;
//        }
//        CurrentHistory.setPosition(historyPosition);
//    }
//
//    /**
//     * Undoes added conditions
//     *
//     * @param currentDatabase
//     * @param action
//     */
//    public void addCondition(ProfileDatabase currentDatabase, History action) {
//        profile profile = currentDatabase.getProfile(action.getHistoryId());
//        int c = action.getHistoryDataIndex();
//        Condition condition = UndoRedoCLIService.getCurrentConditions(profile).get(c);
//        UndoRedoCLIService.removeCondition(condition, profile);
//        if (historyPosition > 0) {
//            historyPosition -= 1;
//        }
//        CurrentHistory.setPosition(historyPosition);
//    }
//
//    /**
//     * Undoes a drug being moved to history
//     *
//     * @param currentDatabase
//     * @param action
//     */
//    public void stopDrug(ProfileDatabase currentDatabase, History action)
//            throws IndexOutOfBoundsException {
//        try {
//            profile profile = currentDatabase.getProfile(action.getHistoryId());
//            int d = action.getHistoryDataIndex();
//            List<Drug> drugs = profile.getHistoryOfMedication();
//            Drug drug = drugs.get(d);
//            UndoRedoCLIService.moveDrugToCurrent(drug, profile);
//            LocalDateTime currentTime = LocalDateTime.now();
//            History newAction = new History("profile", profile.getId(), "stopped",
//                    drug.getDrugName(),
//                    profile.getCurrentMedications().indexOf(drug), currentTime);
//            CurrentHistory.currentSessionHistory.set(historyPosition, newAction);
//            if (historyPosition > 0) {
//                historyPosition -= 1;
//            }
//            CurrentHistory.setPosition(historyPosition);
//        } catch (IndexOutOfBoundsException e) {
//
//        }
//    }
//
//    /**
//     * Undoes a drug being moved to current
//     *
//     * @param currentDatabase
//     * @param action
//     */
//    public void renewDrug(ProfileDatabase currentDatabase, History action) {
//        try {
//            profile profile = currentDatabase.getProfile(action.getHistoryId());
//            int d = action.getHistoryDataIndex();
//            List<Drug> drugs = profile.getCurrentMedications();
//            Drug drug = drugs.get(d);
//            UndoRedoCLIService.moveDrugToHistory(drug, profile);
//            LocalDateTime currentTime = LocalDateTime.now();
//            History newAction = new History("profile", profile.getId(), "started",
//                    drug.getDrugName(),
//                    profile.getHistoryOfMedication().indexOf(drug), currentTime);
//            CurrentHistory.currentSessionHistory.set(historyPosition, newAction);
//            if (historyPosition > 0) {
//                historyPosition -= 1;
//            }
//            CurrentHistory.setPosition(historyPosition);
//        } catch (IndexOutOfBoundsException e) {
//
//        }
//    }
//
//    /**
//     * Undoes a drug being added
//     *
//     * @param currentDatabase
//     * @param action
//     */
//    public void addDrug(ProfileDatabase currentDatabase, History action) {
//        try {
//            profile profile = currentDatabase.getProfile(action.getHistoryId());
//            int d = action.getHistoryDataIndex();
//            List<Drug> drugs = profile.getCurrentMedications();
//            UndoRedoCLIService.deleteDrug(drugs.get(d), profile);
//            if (historyPosition > 0) {
//                historyPosition -= 1;
//            }
//            CurrentHistory.setPosition(historyPosition);
//        } catch (IndexOutOfBoundsException e) {
//
//        }
//    }
//
//    /**
//     * Undoes a drug being deleted
//     *
//     * @param currentDatabase
//     * @param action
//     */
//    public void deleteDrug(ProfileDatabase currentDatabase, History action)
//            throws IndexOutOfBoundsException {
//        try {
//            profile profile = currentDatabase.getProfile(action.getHistoryId());
//            if (action.getHistoryAction().contains("history")) {
//                String drug = action.getHistoryData();
//                Drug d = new Drug(drug);
//                UndoRedoCLIService.addDrug(d, profile);
//                UndoRedoCLIService.moveDrugToHistory(d, profile);
//            } else {
//                String drug = action.getHistoryData();
//                UndoRedoCLIService.addDrug(new Drug(drug), profile);
//            }
//            if (historyPosition > 0) {
//                historyPosition -= 1;
//            }
//            CurrentHistory.setPosition(historyPosition);
//        } catch (IndexOutOfBoundsException e) {
//
//        }
//    }
//
//    /**
//     * Undoes an update to a clinician
//     *
//     * @param action
//     */
//    public void updated(History action) {
//        user user = LoginView.getCurrentUser();
//        String previous = action.getHistoryData()
//                .substring(action.getHistoryData().indexOf("previous ") + 9,
//                        action.getHistoryData().indexOf("new "));
//        String[] previousValues = previous.split(",");
//        user.setName(previousValues[1].replace("name=", ""));
//        user.setStaffID(Integer.valueOf(previousValues[0].replace("staffId=", "")
//                .replace(" ", "")));
//        user.setWorkAddress(previousValues[2].replace("workAddress=", ""));
//        user.setRegion(previousValues[3].replace("region=", ""));
//        if (historyPosition > 0) {
//            historyPosition -= 1;
//        }
//        CurrentHistory.setPosition(historyPosition);
//    }
//
//    /**
//     * Undoes a profile being added
//     *
//     * @param currentDatabase
//     * @param action
//     */
//    public void added(ProfileDatabase currentDatabase, History action) {
//        profile profile = currentDatabase.getProfile(action.getHistoryId());
//        currentDatabase.deleteProfile(action.getHistoryId());
//        unaddedProfiles.add(profile);
//        if (historyPosition > 0) {
//            historyPosition -= 1;
//        }
//        CurrentHistory.setPosition(historyPosition);
//    }
//
//    /**
//     * Undoes a profile being deleted
//     *
//     * @param currentDatabase
//     * @param action
//     */
//    public void deleted(ProfileDatabase currentDatabase, History action) {
//        int oldid = action.getHistoryId();
//        int id = currentDatabase
//                .restoreProfile(oldid, CurrentHistory.deletedProfiles
//                        .get(CurrentHistory.deletedProfiles.size() - 1));
//        CurrentHistory.deletedProfiles.remove(CurrentHistory.deletedProfiles
//                .get(CurrentHistory.deletedProfiles.size() - 1));
//        for (int i = 0; i < currentSessionHistory.size() - 1; i++) {
//            if (currentSessionHistory.get(i).getHistoryId() == oldid) {
//                currentSessionHistory.get(i).setHistoryId(id);
//            }
//        }
//        if (historyPosition > 0) {
//            historyPosition -= 1;
//        }
//        CurrentHistory.setPosition(historyPosition);
//    }
//
//    /**
//     * Undoes an organ being removed from organs donating
//     *
//     * @param currentDatabase
//     * @param action
//     * @throws Exception
//     */
//    public void removed(ProfileDatabase currentDatabase, History action) throws Exception {
//        //todo overhaul organ donating stuff
//        profile profile = currentDatabase.getProfile(action.getHistoryId());
//        UndoRedoCLIService.addOrgansDonating(OrganEnum.stringListToOrganSet(
//                Arrays.asList(action.getHistoryData().replace(' ', '_').split(","))), profile);
//        if (historyPosition > 0) {
//            historyPosition -= 1;
//        }
//        CurrentHistory.setPosition(historyPosition);
//    }
//
//    /**
//     * Undoes organs being set to donating
//     *
//     * @param currentDatabase
//     * @param action
//     */
//    public void set(ProfileDatabase currentDatabase, History action) {
//        profile profile = currentDatabase.getProfile(action.getHistoryId());
//        String[] stringOrgans = action.getHistoryData().replace(' ', '_').split(",");
//        System.out.println(stringOrgans);
//        Set<OrganEnum> organSet = OrganEnum.stringListToOrganSet(Arrays.asList(stringOrgans));
//        UndoRedoCLIService.removeOrgansDonating(organSet, profile);
//        if (historyPosition > 0) {
//            historyPosition -= 1;
//        }
//
//        CurrentHistory.setPosition(historyPosition);
//    }
//
//    /**
//     * Undoes an organ being donated
//     *
//     * @param currentDatabase
//     * @param action
//     */
//    public void donate(ProfileDatabase currentDatabase, History action) {
//        profile profile = currentDatabase.getProfile(action.getHistoryId());
//        String[] stringOrgans = action.getHistoryData().replace(' ', '_').split(",");
//        Set<OrganEnum> organSet = OrganEnum.stringListToOrganSet(Arrays.asList(stringOrgans));
//        UndoRedoCLIService.removeOrgansDonated(organSet, profile);
//        if (historyPosition > 0) {
//            historyPosition -= 1;
//        }
//        CurrentHistory.setPosition(historyPosition);
//    }
//
//    /**
//     * Undoes a profile being updated
//     *
//     * @param currentDatabase
//     * @param action
//     */
//    public void update(ProfileDatabase currentDatabase, History action) {
//        profile profile = currentDatabase.getProfile(action.getHistoryId());
//        System.out.println(action);
//        String old = action.getHistoryData()
//                .substring(action.getHistoryData().indexOf("previous ") + 9,
//                        action.getHistoryData().indexOf(" new "));
//        UndoRedoCLIService
//                .setExtraAttributes(new ArrayList<>(Arrays.asList(old.split(","))), profile);
//        if (historyPosition > 0) {
//            historyPosition -= 1;
//        }
//        CurrentHistory.setPosition(historyPosition);
//    }
//
//    /**
//     * Undoes a procedure being edited
//     *
//     * @param currentDatabase
//     * @param action
//     */
//    public void edited(ProfileDatabase currentDatabase, History action) {
//        profile profile = currentDatabase.getProfile(action.getHistoryId());
//        int procedurePlace = action.getHistoryDataIndex();
//        String previous = action.getHistoryData()
//                .substring(action.getHistoryData().indexOf("PREVIOUS(") + 9,
//                        action.getHistoryData().indexOf("CURRENT("));
//        String[] previousValues = previous.split(",");
//        String organs = action.getHistoryData();
//        List<String> list = new ArrayList<>(Arrays.asList(organs.split(",")));
//        ArrayList<OrganEnum> organList = new ArrayList<>();
//        System.out.println(organs);
//        for (String organ : list) {
//            System.out.println(organ);
//            try {
//                organList.add(OrganEnum.valueOf(organ.replace(" ", "")));
//            } catch (IllegalArgumentException e) {
//                System.out.println(e);
//            }
//        }
//        profile.getAllProcedures().get(procedurePlace).setSummary(previousValues[0]);
//        profile.getAllProcedures().get(procedurePlace)
//                .setDate(LocalDate.parse(previousValues[1]));
//        if (previousValues.length == 3) {
//            profile.getAllProcedures().get(procedurePlace)
//                    .setLongDescription(previousValues[2]);
//        }
//        profile.getAllProcedures().get(procedurePlace).setOrgansAffected(organList);
//        if (historyPosition > 0) {
//            historyPosition -= 1;
//        }
//        CurrentHistory.setPosition(historyPosition);
//    }
//
//}
