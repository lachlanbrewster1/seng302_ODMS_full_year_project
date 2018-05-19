package odms.controller;

import odms.cli.CommandUtils;
import odms.data.ProfileDatabase;
import odms.data.UserDatabase;
import odms.enums.OrganEnum;
import odms.medications.Drug;
import odms.profile.Condition;
import odms.profile.Profile;
import odms.user.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class UndoController {

    private static ArrayList<Profile> unaddedProfiles = new ArrayList<>();
    private static int historyPosition;
    public static ArrayList<String> currentSessionHistory;

    public static void undo(ProfileDatabase currentDatabase) {
        historyPosition = HistoryController.getPosition();
        currentSessionHistory = HistoryController.getHistory();
        try {
            String action = currentSessionHistory.get(historyPosition);
            int end = 0;
            if(action!= "") {
                end = action.indexOf(" at");
                action = action.substring(0, action.indexOf(" at"));
            }
            if (action.contains("added") && !action.contains("drug") && !action.contains("condition")&& !action.contains("received") && !action.contains("donated")) {
                added(currentDatabase, action);
            } else if (action.contains("deleted")) {
                deleted(currentDatabase, action);
            } else if (action.contains("removed") && !action.contains("drug") && !action.contains("condition")&& !action.contains("received") && !action.contains("donated")) {
                removed(currentDatabase, action);
            } else if (action.contains("set")) {
                set(currentDatabase, action);
            } else if (action.contains("donate")) {
                donate(currentDatabase, action);
            } else if (action.contains("update") && !action.contains("updated")) {
                update(currentDatabase, action);
            } else if (action.contains("EDITED")) {
                edited(currentDatabase, action);
            } else if (action.contains("updated")) {
                updated(action);
            } else if (action.contains("added drug")) {
                addDrug(currentDatabase, action, end);
            } else if (action.contains("removed drug")) {
                deleteDrug(currentDatabase, action, end);
            } else if (action.contains("stopped")) {
                stopDrug(currentDatabase, action);
            } else if (action.contains("started")) {
                renewDrug(currentDatabase, action);
            } else if (action.contains("added condition")) {
                addCondition(currentDatabase,action);
            } else if (action.contains("removed condition")) {
                removedCondition(currentDatabase,action);
            } else if (action.contains("received") &&action.contains("added")) {
                addedReceived(currentDatabase, action);
            } else if (action.contains("donated") &&action.contains("added")) {
                addedDonated(currentDatabase, action);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("No commands have been entered");
        }
    }

    private static void addedDonated(ProfileDatabase currentDatabase, String action) {
        int id = Integer.parseInt(action.substring(0,action.indexOf("added")).replaceAll("[\\D]", ""));
        Profile profile = currentDatabase.getProfile(id);
        String organ = action.substring(action.indexOf("added ")+6, action.indexOf(" to"));
        profile.removeOrganDonated(OrganEnum.valueOf(organ));
    }

    private static void addedReceived(ProfileDatabase currentDatabase, String action) {
        int id = Integer.parseInt(action.substring(0,action.indexOf("added")).replaceAll("[\\D]", ""));
        Profile profile = currentDatabase.getProfile(id);
        String organ = action.substring(action.indexOf("added ")+6, action.indexOf(" to"));
        profile.removeOrganReceived(OrganEnum.valueOf(organ));
    }

    private static void removedCondition(ProfileDatabase currentDatabase, String action) {
        int id = Integer.parseInt(action.substring(0,action.indexOf("removed")).replaceAll("[\\D]", ""));
        Profile profile = currentDatabase.getProfile(id);
        String s = action.substring(action.indexOf("(")+1,action.indexOf(")"));
        String[] values = s.split(",");
        String diagDate = values[1].substring(8)+"-"+values[1].substring(5,7)+"-"+values[1].substring(0,4);
        String cureDate = null;
        System.out.println(diagDate);
        if(!values[3].equals("null")) {
            cureDate = values[3].substring(8) + "-" + values[3].substring(5, 7) + "-" + values[3].substring(0, 4);
        }
        Condition condition = new Condition(values[0], diagDate, cureDate, Boolean.valueOf(values[2]));
        profile.addCondition(condition);
        LocalDateTime currentTime = LocalDateTime.now();
        currentSessionHistory.set(historyPosition,"Donor " + profile.getId()  + " removed condition with values("  +condition.getName()+","+condition.getDateOfDiagnosis()+","+condition.getChronic()+","+condition.getDateCuredString()+ ") index of "+ profile.getCurrentConditions().indexOf(condition) + " at " +currentTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
        if (historyPosition > 0) {
            historyPosition -= 1;
        }

        HistoryController.setPosition(historyPosition);
    }

    private static void addCondition(ProfileDatabase currentDatabase, String action) {
        int id = Integer.parseInt(action.substring(0,action.indexOf("added")).replaceAll("[\\D]", ""));
        Profile profile = currentDatabase.getProfile(id);
        int c = Integer.parseInt(action.substring(action.indexOf("index of")).replaceAll("[\\D]", ""));
        Condition condition = profile.getCurrentConditions().get(c);
        profile.removeCondition(condition);
        if (historyPosition > 0) {
            historyPosition -= 1;
        }

        HistoryController.setPosition(historyPosition);
    }

    private static void stopDrug(ProfileDatabase currentDatabase, String action) {
        int id = Integer.parseInt(action.substring(0,action.indexOf("stopped")).replaceAll("[\\D]", ""));
        Profile profile = currentDatabase.getProfile(id);
        int d = Integer.parseInt(action.substring(action.indexOf("stopped")).replaceAll("[\\D]", ""));
        ArrayList<Drug> drugs = profile.getHistoryOfMedication();
        Drug drug = drugs.get(d);
        profile.moveDrugToCurrent(drug);
        LocalDateTime currentTime = LocalDateTime.now();
        String data = "Donor " + profile.getId()  + " stopped "  + drug.getDrugName() + " index of "+ profile.getCurrentMedications().indexOf(drug) + " at" +currentTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        HistoryController.currentSessionHistory.set(historyPosition, data);
        if (historyPosition > 0) {
            historyPosition -= 1;
        }

        HistoryController.setPosition(historyPosition);
    }

    private static void renewDrug(ProfileDatabase currentDatabase, String action) {
        int id = Integer.parseInt(action.substring(0,action.indexOf("started")).replaceAll("[\\D]", ""));
        Profile profile = currentDatabase.getProfile(id);
        int d = Integer.parseInt(action.substring(action.indexOf("started")).replaceAll("[\\D]", ""));
        ArrayList<Drug> drugs = profile.getCurrentMedications();
        Drug drug = drugs.get(d);
        profile.moveDrugToHistory(drug);
        LocalDateTime currentTime = LocalDateTime.now();
        String data = "Donor " + profile.getId()  + " started using "  + drug.getDrugName() + " index of "+ profile.getHistoryOfMedication().indexOf(drug) + "again at" +currentTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        HistoryController.currentSessionHistory.set(historyPosition, data);
        if (historyPosition > 0) {
            historyPosition -= 1;
        }

        HistoryController.setPosition(historyPosition);
    }

    private static void addDrug(ProfileDatabase currentDatabase, String action, int end) {
        int id = Integer.parseInt(action.substring(0,action.indexOf("drug")).replaceAll("[\\D]", ""));
        Profile profile = currentDatabase.getProfile(id);
        int d = Integer.parseInt(action.substring(action.indexOf(" index of")).replaceAll("[\\D]", ""));
        ArrayList<Drug> drugs = profile.getCurrentMedications();
        profile.deleteDrug(drugs.get(d));
        if (historyPosition > 0) {
            historyPosition -= 1;
        }

        HistoryController.setPosition(historyPosition);
    }

    private static void deleteDrug(ProfileDatabase currentDatabase, String action, int end) {
        int id = Integer.parseInt(action.substring(0,action.indexOf("drug")).replaceAll("[\\D]", ""));
        Profile profile = currentDatabase.getProfile(id);
        if(action.contains("history")) {
            String drug = action.substring(action.indexOf("tory")+5,action.indexOf(" index of"));
            Drug d = new Drug(drug);
            profile.addDrug(d);
            profile.moveDrugToHistory(d);
        } else {
            String drug = action.substring(action.indexOf("drug") + 5,action.indexOf(" index of"));
            profile.addDrug(new Drug(drug));
        }
        if (historyPosition > 0) {
            historyPosition -= 1;
        }

        HistoryController.setPosition(historyPosition);
    }

    private static void updated(String action) {
        int id = Integer.parseInt(action.substring(0,action.indexOf("update")).replaceAll("[\\D]", ""));
        User user = LoginController.getCurrentUser();
        String previous = action.substring(action.indexOf("(")+1,action.indexOf(")"));
        String[] previousValues = previous.split(",");
        user.setName(previousValues[1].replace("name=",""));
        user.setStaffId(Integer.valueOf(previousValues[0].replace("staffId=","").replace(" ","")));
        user.setWorkAddress(previousValues[2].replace("workAddress=",""));
        user.setRegion(previousValues[3].replace("region=",""));
        if (historyPosition > 0) {
            historyPosition -= 1;
        }

        HistoryController.setPosition(historyPosition);
    }

    public static void added(ProfileDatabase currentDatabase, String action) {
        int id = Integer.parseInt(action.replaceAll("[\\D]", ""));
        Profile profile = currentDatabase.getProfile(id);
        currentDatabase.deleteProfile(id);
        unaddedProfiles.add(profile);
        if (historyPosition > 0) {
            historyPosition -= 1;
        }

        HistoryController.setPosition(historyPosition);
    }
    public static void deleted(ProfileDatabase currentDatabase, String action) {
        int oldid = Integer.parseInt(action.replaceAll("[\\D]", ""));
        int id = currentDatabase
                .restoreProfile(oldid, HistoryController.deletedProfiles.get(HistoryController.deletedProfiles.size() - 1));
        HistoryController.deletedProfiles.remove(HistoryController.deletedProfiles.get(HistoryController.deletedProfiles.size() - 1));
        for (int i = 0; i < currentSessionHistory.size() - 1; i++) {
            if (currentSessionHistory.get(i).contains("Profile " + oldid)) {
                currentSessionHistory.set(i,
                        ("Profile " + id + " " + currentSessionHistory.get(i).substring(
                                action.indexOf("Profile " + oldid) + 6 + Integer
                                        .toString(id)
                                        .length())));
            }
        }
        currentSessionHistory
                .set(historyPosition,
                        ("Profile " + id + " deleted at " + LocalDateTime.now()));
        if (historyPosition > 0) {
            historyPosition -= 1;
        }

        HistoryController.setPosition(historyPosition);
}

    public static void removed(ProfileDatabase currentDatabase, String action) throws Exception{
        int id = Integer.parseInt(action.replaceAll("[\\D]", ""));
        Profile profile = currentDatabase.getProfile(id);
        profile.addOrgansDonating(OrganEnum.stringListToOrganSet(Arrays.asList(
                action.substring(
                        action.indexOf("[") + 1,
                        action.indexOf("]")).split(",")
        )));
        if (historyPosition > 0) {
            historyPosition -= 1;
        }

        HistoryController.setPosition(historyPosition);
    }
    public static void set(ProfileDatabase currentDatabase, String action) {
        int id = Integer.parseInt(action.replaceAll("[\\D]", ""));
        Profile profile = currentDatabase.getProfile(id);
        String[] stringOrgans = action.substring(action.indexOf("[") + 1, action.indexOf("]")).split(",");
        Set<OrganEnum> organSet = OrganEnum.stringListToOrganSet(Arrays.asList(stringOrgans));
        profile.removeOrgansDonating(organSet);
        if (historyPosition > 0) {
            historyPosition -= 1;
        }

        HistoryController.setPosition(historyPosition);
    }
    public static void donate(ProfileDatabase currentDatabase, String action) {
        int id = Integer.parseInt(action.replaceAll("[\\D]", ""));
        Profile profile = currentDatabase.getProfile(id);
        String[] stringOrgans = action.substring(action.indexOf("[") + 1, action.indexOf("]")).split(",");
        Set<OrganEnum> organSet = OrganEnum.stringListToOrganSet(Arrays.asList(stringOrgans));
        profile.removeOrgansDonated(organSet);
        if (historyPosition > 0) {
            historyPosition -= 1;
        }

        HistoryController.setPosition(historyPosition);
    }
    public static void update(ProfileDatabase currentDatabase, String action){
        int id = Integer.parseInt(
                action.substring(0, action.indexOf("previous")).replaceAll("[\\D]", ""));
        Profile profile = currentDatabase.getProfile(id);
        System.out.println(action);
        String old = action.substring(action.indexOf("ird"), action.indexOf("new"));
        profile.setExtraAttributes(new ArrayList<>(Arrays.asList(old.split(","))));
        if (historyPosition > 0) {
            historyPosition -= 1;
        }

        HistoryController.setPosition(historyPosition);
    }
    public static void edited(ProfileDatabase currentDatabase, String action) {
        int id = Integer.parseInt(
                action.substring(0, action.indexOf("PROCEDURE")).replaceAll("[\\D]", ""));
        Profile profile = currentDatabase.getProfile(id);
        int procedurePlace = Integer.parseInt(
                action.substring(action.indexOf("PROCEDURE"), action.indexOf("EDITED"))
                        .replaceAll("[\\D]", ""));
        String previous = action
                .substring(action.indexOf("PREVIOUS(") + 9, action.indexOf(") OLD"));
        String[] previousValues = previous.split(",");
        String organs = action
                .substring(action.indexOf("[") + 1, action.indexOf("] CURRENT"));
        List<String> List = new ArrayList<>(Arrays.asList(organs.split(",")));
        ArrayList<OrganEnum> organList = new ArrayList<>();
        System.out.println(organs);
        for (String organ : List) {
            System.out.println(organ);
            try {
                organList.add(OrganEnum.valueOf(organ.replace(" ", "")));
            } catch (IllegalArgumentException e) {
                System.out.println(e);
            }
        }
        profile.getAllProcedures().get(procedurePlace).setSummary(previousValues[0]);
        profile.getAllProcedures().get(procedurePlace)
                .setDate(LocalDate.parse(previousValues[1]));
        if (previousValues.length == 3) {
            profile.getAllProcedures().get(procedurePlace)
                    .setLongDescription(previousValues[2]);
        }
        profile.getAllProcedures().get(procedurePlace).setOrgansAffected(organList);
        if (historyPosition > 0) {
            historyPosition -= 1;
        }

        HistoryController.setPosition(historyPosition);
    }

}
