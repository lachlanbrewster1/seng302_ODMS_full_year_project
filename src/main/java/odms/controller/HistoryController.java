package odms.controller;

import odms.History.History;
import odms.profile.Profile;

import java.util.ArrayList;

public class HistoryController {
    public static ArrayList<History> currentSessionHistory = new ArrayList<>();
    public static int historyPosition = 0;
    public static ArrayList<Profile> deletedProfiles = new ArrayList<>();

    public static void updateHistory(History history) {
        if (getHistory().size() != 0) {
            if (getPosition()
                    != getHistory().size() - 1) {
                currentSessionHistory
                        .subList(getPosition(),
                                getHistory().size() - 1).clear();
            }
        }
        currentSessionHistory.add(history);
        historyPosition = currentSessionHistory.size() - 1;
    }
    public static ArrayList<History> getHistory() {
        return currentSessionHistory;
    }


    public static int getPosition() {
        return historyPosition;
    }
    public static void setPosition(int num) {historyPosition = num;}

}
