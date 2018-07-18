package odms.view.profile;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import odms.controller.CommonController;
import odms.controller.GuiMain;
import odms.controller.data.ProfileDataIO;
import odms.controller.history.RedoController;
import odms.controller.history.UndoController;
import odms.controller.profile.ProfileEditController;
import odms.controller.profile.ProfileMedicationsController;
import odms.controller.profile.ProfileOrganOverviewControllerPOSSIBLYREDUNDANT;
import odms.model.profile.Condition;
import odms.model.profile.Profile;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import static odms.controller.AlertController.invalidUsername;

public class ProfileDisplayControllerTODO extends CommonController {

    public Profile currentProfile;
    /**
     * Text for showing recent edits.
     */
    @FXML
    public Text editedText;
    @FXML
    private Label donorFullNameLabel;
    @FXML
    private Label donorStatusLabel;
    @FXML
    private Label userIdLabel;
    @FXML
    private Button logoutButton;
    @FXML
    private Button buttonViewMedicationHistory;
    @FXML
    private Button addNewProcedureButton;
    @FXML
    private Button deleteProcedureButton;
    @FXML
    private Label receiverStatusLabel;

    protected ObjectProperty<Profile> currentProfileBound = new SimpleObjectProperty<>();
    private Boolean isOpenedByClinician = false;
    // Displays in IntelliJ as unused but is a false positive
    // The FXML includes operate this way and allow access to the instantiated controller.
    @FXML
    private AnchorPane profileOrganOverview;
    @FXML
    private ProfileOrgansView profileOrgansView;

    @FXML
    private ProfileGeneralViewTODOReplacesDisplayController profileGeneralViewTODOReplacesDisplayController;
    @FXML
    private ProfileMedicalViewTODO profileMedicalViewTODO;
    @FXML
    private ProfileHistoryViewTODO profileHistoryViewTODO;
    @FXML
    private ProfileMedicationsView profileMedicationsView;


    /**
     * Called when there has been an edit to the current profile.
     */
    public void editedTextArea() {
        editedText.setText("The profile was successfully edited.");
    }


    /**
     * Scene change to log in view.
     *
     * @param event clicking on the logout button.
     */
    @FXML
    private void handleLogoutButtonClicked(ActionEvent event) throws IOException {
        showLoginScene(event);
    }


    /**
     * sets all of the items in the fxml to their respective values
     *
     * @param currentProfile donors profile
     */
    @FXML
    private void setPage(Profile currentProfile) {

        try {
            donorFullNameLabel.setText(currentProfile.getFullName());
            donorStatusLabel.setText(donorStatusLabel.getText() + "Unregistered");
            receiverStatusLabel.setText(receiverStatusLabel.getText() + "Unregistered");

            if (currentProfile.getDonor() != null && currentProfile.getDonor()) {
                if (currentProfile.getOrgansDonated().size() > 0) {
                    donorStatusLabel.setText("Donor Status: Registered");
                }
            }

            if (currentProfile.getOrgansRequired().size() < 1) {
                currentProfile.setReceiver(false);
            } else {
                currentProfile.setReceiver(true);
            }

            if (currentProfile.isReceiver()) {
                receiverStatusLabel.setText("Receiver Status: Registered");
            }



            if (currentProfile.getId() != null) {
                userIdLabel
                        .setText(userIdLabel.getText() + Integer.toString(currentProfile.getId()));
            }

            refreshConditionTable();

        } catch (Exception e) {
            e.printStackTrace();
            invalidUsername();
        }
        refreshConditionTable();
    }


    /**
     * Enables the relevant buttons on medications tab for how many drugs are selected
     */
    @FXML
    private void refreshPageElements() {
        ArrayList<Condition> allConditions = convertConditionObservableToArray(
                curConditionsTable.getSelectionModel().getSelectedItems());
        allConditions.addAll(convertConditionObservableToArray(
                pastConditionsTable.getSelectionModel().getSelectedItems()));

        hideItems();
        disableButtonsIfNoItems(allConditions);
    }


    /**
     * hides items that shouldn't be visible to either a donor or clinician
     */
    @FXML
    private void hideItems() {
        if (isOpenedByClinician) {
            //user is a clinician looking at donors profile, maximise functionality
            curConditionsTable.setEditable(true);
            pastConditionsTable.setEditable(true);
            toggleChronicButton.setDisable(false);
            toggleChronicButton.setVisible(true);
            toggleCuredButton.setDisable(false);
            toggleCuredButton.setVisible(true);
            addNewConditionButton.setVisible(true);
            deleteConditionButton.setVisible(true);
            addNewProcedureButton.setVisible(true);
            deleteProcedureButton.setVisible(true);
            buttonSaveMedications.setVisible(true);
            buttonDeleteMedication.setVisible(true);
            buttonShowDrugInteractions.setVisible(true);
            buttonViewActiveIngredients.setVisible(true);
            buttonAddMedication.setVisible(true);
            buttonMedicationCurrentToHistoric.setVisible(true);
            buttonMedicationHistoricToCurrent.setVisible(true);
            textFieldMedicationSearch.setVisible(true);
            tableViewActiveIngredients.setVisible(true);
            tableViewDrugInteractionsNames.setVisible(true);
            tableViewDrugInteractions.setVisible(true);
            buttonViewMedicationHistory.setVisible(true);

            logoutButton.setVisible(false);
        } else {
            // user is a standard profile, limit functionality
            curConditionsTable.setEditable(false);
            pastConditionsTable.setEditable(false);
            toggleChronicButton.setDisable(true);
            toggleChronicButton.setVisible(false);
            toggleCuredButton.setDisable(true);
            toggleCuredButton.setVisible(false);
            addNewConditionButton.setVisible(false);
            deleteConditionButton.setVisible(false);
            addNewProcedureButton.setVisible(false);
            deleteProcedureButton.setVisible(false);
            buttonSaveMedications.setVisible(false);
            buttonDeleteMedication.setVisible(false);
            buttonShowDrugInteractions.setVisible(false);
            buttonViewActiveIngredients.setVisible(false);
            buttonAddMedication.setVisible(false);
            buttonMedicationCurrentToHistoric.setVisible(false);
            buttonMedicationHistoricToCurrent.setVisible(false);
            textFieldMedicationSearch.setVisible(false);
            tableViewActiveIngredients.setVisible(false);
            tableViewDrugInteractionsNames.setVisible(false);
            tableViewDrugInteractions.setVisible(false);
            buttonViewMedicationHistory.setVisible(false);
        }
    }

    public Profile getCurrentProfile() {
        return currentProfile;
    }

    @FXML
    private void onTabOrgansSelected() {
        profileOrgansView.currentProfile.bind(currentProfileBound);
        profileOrgansView.populateOrganLists();
    }

    @FXML
    public void onTabGeneralSelected() {
        profileGeneralViewTODOReplacesDisplayController.currentProfile.bind(currentProfileBound);
    }

    @FXML
    public void onTabMedicalSelected() {
        profileMedicalViewTODO.currentProfile.bind(currentProfileBound);
    }

    @FXML
    public void onTabHistorySelected() {
        profileHistoryViewTODO.currentProfile.bind(currentProfileBound);
    }

    public void onTabMedicationsSelected() {
        profileMedicationsView.currentProfile.bind(currentProfileBound);
    }


    /**
     * Sets the current donor attributes to the labels on start up.
     */
    @FXML
    public void initialize() {

        if (currentProfile != null) {
            currentProfileBound.set(currentProfile);
            setPage(currentProfile);
        }

        refreshPageElements();

    }


    /**
     * sets the profile if it is being opened by a clinician If opened by clinician, set appropriate
     * boolean and profile
     *
     * @param profile to be used
     */
    public void setProfileViaClinician(Profile profile) {
        isOpenedByClinician = true;
        currentProfile = profile;
    }

    /**
     * sets the donor if it was logged in by a user If logged in normally, sets profile
     *
     * @param profile to be used
     */
    public void setProfile(Profile profile) {
        currentProfile = profile;
    }


}
