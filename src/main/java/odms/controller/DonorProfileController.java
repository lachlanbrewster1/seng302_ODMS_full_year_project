package odms.controller;

import static odms.controller.AlertController.InvalidUsername;
import static odms.controller.LoginController.getCurrentDonor;
import static odms.controller.UndoRedoController.redo;
import static odms.controller.UndoRedoController.undo;

import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import odms.commandlineview.CommandUtils;
import odms.data.DonorDataIO;
import odms.donor.Condition;
import odms.donor.Donor;
import java.io.Console;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class DonorProfileController {

    @FXML
    private Label donorFullNameLabel;

    @FXML
    private Label donorStatusLabel;

    @FXML
    private Label givenNamesLabel;

    @FXML
    private Label lastNamesLabel;

    @FXML
    private Label irdLabel;

    @FXML
    private Label dobLabel;

    @FXML
    private Label dodLabel;

    @FXML
    private Label genderLabel;

    @FXML
    private Label heightLabel;

    @FXML
    private Label weightLabel;

    @FXML
    private Label phoneLabel;

    @FXML
    private Label emailLabel;

    @FXML
    private Label addressLabel;

    @FXML
    private Label regionLabel;

    @FXML
    private Label bloodTypeLabel;

    @FXML
    private Label smokerLabel;

    @FXML
    private Label alcoholConsumptionLabel;

    @FXML
    private Label bloodPressureLabel;

    @FXML
    private Label chronicConditionsLabel;

    @FXML
    private Label organsLabel;

    @FXML
    private Label donationsLabel;

    @FXML
    private TextArea historyView;

    @FXML
    private Label bmiLabel;

    @FXML
    private Label ageLabel;

    @FXML
    private Label userIdLabel;

    @FXML
    private TableView curConditionsTable;

    @FXML
    private TableColumn curDescriptionColumn;

    @FXML
    private TableColumn curChronicColumn;

    @FXML
    private TableColumn curDateOfDiagnosisColumn;

    @FXML
    private TableView pastConditionsTable;

    @FXML
    private TableColumn pastDescriptionColumn;

    @FXML
    private TableColumn pastDateCuredColumn;

    @FXML
    private TableColumn pastDateOfDiagnosisColumn;

    @FXML
    private Button toggleCuredButton;

    @FXML
    private Button toggleChronicButton;

    @FXML
    private Button addNewConditionButton;

    @FXML
    private Button deleteConditionButton;


    private ObservableList<Condition> curConditionsObservableList;
    private ObservableList<Condition> pastConditionsObservableList;



    /**
     * initializes and refreshes the current and past conditions tables
     */
    @FXML
    private void makeTable(ArrayList<Condition> curConditions, ArrayList<Condition> pastConditions){                      //TODO need a function to get all current conditions, rather than just all

        //curDiseasesTable.getSortOrder().add(curChronicColumn);
        Donor currentDonor = getCurrentDonor();
        curChronicColumn.setComparator(curChronicColumn.getComparator().reversed());
        currentDonor.setAllConditions(new ArrayList<>());                                  //remove this eventually, just to keep list small with placeholder data

        if (curConditions != null) {curConditionsObservableList = FXCollections.observableArrayList(curConditions);}
        else {curConditionsObservableList = FXCollections.observableArrayList(); }
        if (pastConditions != null) {pastConditionsObservableList = FXCollections.observableArrayList(pastConditions);}
        else {pastConditionsObservableList = FXCollections.observableArrayList(); }

        Condition placeholdCondition = new Condition( "Space aids", LocalDate.of(2005, 12, 5), true);
        Condition placeholdCondition2 = new Condition("Shortness", LocalDate.of(2005, 12, 7), LocalDate.of(2012, 3, 10), false );
        Condition placeholdCondition3 = new Condition("Ginger", LocalDate.of(2005, 12, 10), false);
        Condition placeholdCondition4 = new Condition("Bla bla", LocalDate.of(2005, 12, 11), true);
        Condition placeholdCondition5 = new Condition("Bla blaaaa", LocalDate.of(2005, 12, 1), true);

        currentDonor.addCondition(placeholdCondition3);
        currentDonor.addCondition(placeholdCondition5);
        currentDonor.addCondition(placeholdCondition);
        currentDonor.addCondition(placeholdCondition4);
        currentDonor.addCondition(placeholdCondition2);

        refreshTable();

    }

    /**
     * refreshes current and past conditions table with its up to date data
     */
    @FXML
    private void refreshTable() {
        Donor currentDonor = getCurrentDonor();

        curConditionsTable.getItems().clear();
        if (currentDonor.getCurrentConditions() != null) {curConditionsObservableList.addAll(currentDonor.getCurrentConditions());}
        pastConditionsTable.getItems().clear();
        if (currentDonor.getCuredConditions() != null) {pastConditionsObservableList.addAll(currentDonor.getCuredConditions());}

        curConditionsTable.setItems(curConditionsObservableList);
        curDescriptionColumn.setCellValueFactory(new PropertyValueFactory("condition"));
        curChronicColumn.setCellValueFactory(new PropertyValueFactory("chronicText"));
        curDateOfDiagnosisColumn.setCellValueFactory(new PropertyValueFactory("dateOfDiagnosis"));
        curConditionsTable.getColumns().setAll(curDescriptionColumn, curChronicColumn, curDateOfDiagnosisColumn);

        pastConditionsTable.setItems(pastConditionsObservableList);
        pastDescriptionColumn.setCellValueFactory(new PropertyValueFactory("condition"));
        pastDateOfDiagnosisColumn.setCellValueFactory(new PropertyValueFactory("dateOfDiagnosis"));
        pastDateCuredColumn.setCellValueFactory(new PropertyValueFactory("dateCured"));
        pastConditionsTable.getColumns().setAll(pastDescriptionColumn, pastDateOfDiagnosisColumn, pastDateCuredColumn);

        forceSortOrder();

    }

    /**
     * forces the sort order of the current conditions table so that Chronic conditions are always at the top
     */
    @FXML
    private void forceSortOrder() {
        curConditionsTable.getSortOrder().clear();
        curConditionsTable.getSortOrder().add(curChronicColumn);
    }

    /**
     * Refreshes the page so that the relevant buttons are available to the correct user, Clinician has max 'access'
     */
    @FXML
    private void refreshPage() {
        //Show or hide relevant buttons if user is clinician or donor

        if (LoginController.getCurrentUser() != null) {
            //User is a donor, limit functionality
            curConditionsTable.setEditable(false);
            pastConditionsTable.setEditable(false);
            toggleChronicButton.setDisable(true);
            toggleChronicButton.setVisible(false);
            toggleCuredButton.setDisable(true);
            toggleCuredButton.setVisible(false);
            addNewConditionButton.setVisible(false);
            deleteConditionButton.setVisible(false);

        } else {
            //User is a clinician looking at donors profile, maximise functionality
            curConditionsTable.setEditable(true);
            pastConditionsTable.setEditable(true);
            toggleChronicButton.setDisable(false);
            toggleChronicButton.setVisible(true);
            toggleCuredButton.setDisable(false);
            toggleCuredButton.setVisible(true);
            addNewConditionButton.setVisible(true);
            deleteConditionButton.setVisible(true);
        }

    }

    /**
     * Button handler to add condition to the current conditions for the current profile.
     * @param event clicking on the add button.
     */
    @FXML
    private void handleAddNewCondition(ActionEvent event) throws IOException {
        Donor currentDonor = getCurrentDonor();

        Condition condition = new Condition("Being cold", LocalDate.now(), true);
        currentDonor.addCondition(condition);

        refreshTable();
    }

    /**
     * Button handler to handle delete button clicked, only available to clinicians
     * @param event clicking on the button.
     */
    @FXML
    private void handleDeleteCondition(ActionEvent event) throws IOException {
        Donor currentDonor = getCurrentDonor();

        Condition condition = (Condition) curConditionsTable.getSelectionModel().getSelectedItem();
        if (condition == null) { condition = (Condition) pastConditionsTable.getSelectionModel().getSelectedItem(); }
        if (condition == null) { return; }

        currentDonor.removeCondition(condition);

        refreshTable();
    }


    /**
     * Button handler to handle toggle chronic button clicked, only available to clinicians
     * @param event clicking on the button.
     */
    @FXML
    private void handleToggleChronicButtonClicked(ActionEvent event) {
        Condition condition = (Condition) curConditionsTable.getSelectionModel().getSelectedItem();
        if (condition == null) { condition = (Condition) pastConditionsTable.getSelectionModel().getSelectedItem(); }
        if (condition == null) { return; }
        condition.setIsChronic(!condition.getChronic());
        if (condition.getChronic()) {
            condition.setChronicText("CHRONIC");
            condition.setIsCured(false);
        }
        else {condition.setChronicText("");}
        refreshTable();

    }


    /**
     * Button handler to handle toggle cured button clicked, only available to clinicians
     * @param event clicking on the button.
     */
    @FXML
    private void handleToggleCuredButtonClicked(ActionEvent event) {
        Condition condition = (Condition) curConditionsTable.getSelectionModel().getSelectedItem();
        if (condition == null) { condition = (Condition) pastConditionsTable.getSelectionModel().getSelectedItem(); }
        if (condition == null) { return; }

        if (!condition.getChronic()) {
            condition.setIsCured(!condition.getCured());
        } else {
            System.out.println("Condition must be unmarked as Chronic before being Cured!");
        }

        if (condition.getCured()) { condition.setDateCured(LocalDate.now()); }
        else {
            condition.setDateCured(null);
        }
        refreshTable();
    }


    /**
     * Scene change to log in view.
     * @param event clicking on the logout button.
     */
    @FXML
    private void handleLogoutButtonClicked(ActionEvent event) throws IOException {
        Parent parent = FXMLLoader.load(getClass().getResource("/view/Login.fxml"));
        Scene newScene = new Scene(parent);
        Stage appStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        appStage.setScene(newScene);
        appStage.show();
    }

    /**
     * Button handler to undo last action.
     * @param event clicking on the undo button.
     */
    @FXML
    private void handleUndoButtonClicked(ActionEvent event) throws IOException {
        undo();
    }

    /**
     * Button handler to redo last undo action.
     * @param event clicking on the redo button.
     */
    @FXML
    private void handleRedoButtonClicked(ActionEvent event) throws IOException {
        redo();
    }

    /**
     * Button handler to make fields editable.
     * @param event clicking on the edit button.
     */
    @FXML
    private void handleEditButtonClicked(ActionEvent event) throws IOException {
        Parent parent = FXMLLoader.load(getClass().getResource("/view/EditDonorProfile.fxml"));
        Scene newScene = new Scene(parent);
        Stage appStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        appStage.setScene(newScene);
        appStage.show();
    }

    /**
     * Sets the current donor attributes to the labels on start up.
     */
    @FXML
    public void initialize() {
        Donor currentDonor = getCurrentDonor();
        makeTable(currentDonor.getCurrentConditions(), currentDonor.getCuredConditions());                       //need get current conditions rather than get all conditions
        refreshPage();
        refreshTable();

        try {
            donorFullNameLabel
                    .setText(currentDonor.getGivenNames() + " " + currentDonor.getLastNames());
            donorStatusLabel.setText(donorStatusLabel.getText() + "Unregistered");

            if (currentDonor.getRegistered() != null && currentDonor.getRegistered() == true) {
                donorStatusLabel.setText("Donor Status: Registered");
            }
            if (currentDonor.getGivenNames() != null) {
                givenNamesLabel.setText(givenNamesLabel.getText() + currentDonor.getGivenNames());
            }
            if (currentDonor.getLastNames() != null) {
                lastNamesLabel.setText(lastNamesLabel.getText() + currentDonor.getLastNames());
            }
            if (currentDonor.getIrdNumber() != null) {
                irdLabel.setText(irdLabel.getText() + currentDonor.getIrdNumber());
            }
            if (currentDonor.getDateOfBirth() != null) {
                dobLabel.setText(dobLabel.getText() + currentDonor.getDateOfBirth());
            }
            if (currentDonor.getDateOfDeath() != null) {
                dodLabel.setText(dodLabel.getText() + currentDonor.getDateOfDeath());
            } else {
                dodLabel.setText(dodLabel.getText() + "NULL");
            }
            if (currentDonor.getGender() != null) {
                genderLabel.setText(genderLabel.getText() + currentDonor.getGender());
            }
            heightLabel.setText(heightLabel.getText() + currentDonor.getHeight());
            weightLabel.setText(weightLabel.getText() + currentDonor.getWeight());
            phoneLabel.setText(phoneLabel.getText());
            emailLabel.setText(emailLabel.getText());

            if (currentDonor.getAddress() != null) {
                addressLabel.setText(addressLabel.getText() + currentDonor.getAddress());
            }
            if (currentDonor.getRegion() != null) {
                regionLabel.setText(regionLabel.getText() + currentDonor.getRegion());
            }
            if (currentDonor.getBloodType() != null) {
                bloodTypeLabel.setText(bloodTypeLabel.getText() + currentDonor.getBloodType());
            }
            if(currentDonor.getHeight() != null && currentDonor.getWeight() != null){
                bmiLabel.setText(bmiLabel.getText() + Math.round(currentDonor.calculateBMI() * 100.00) / 100.00);
            }
            if(currentDonor.getDateOfBirth() != null){
                ageLabel.setText(ageLabel.getText() + Integer.toString(currentDonor.calculateAge()));
            }
            if(currentDonor.getId() != null){
                userIdLabel.setText(userIdLabel.getText() + Integer.toString(currentDonor.getId()));
            }
            organsLabel.setText(organsLabel.getText() + currentDonor.getOrgans().toString());
            donationsLabel.setText(donationsLabel.getText() + currentDonor.getDonatedOrgans().toString());
            /*if (currentDonor.getSmoker() != null) {
                smokerLabel.setText(smokerLabel.getText() + currentDonor.getSmoker());
            }*/
            /*if (currentDonor.getAlcoholConsumption() != null) {
                alcoholConsumptionLabel.setText(alcoholConsumptionLabel.getText() + currentDonor.getAlcoholConsumption());
            }*/
            /*if (currentDonor.getBloodPressure() != null) {
                bloodPressureLabel.setText(bloodPressureLabel.getText() + currentDonor.getBloodPressure());
            }*/
            //chronic diseases.
            //organs to donate.
            //past donations.
            String history = DonorDataIO.getHistory();
            Gson gson = new Gson();

            if(history.equals("")) {
                history = gson.toJson(CommandUtils.getHistory());
            } else {
                history = history.substring(0, history.length()-1);
                history = history+","+gson.toJson(CommandUtils.getHistory()).substring(1);
            }
            history = history.substring(1, history.length()-1);
            String[] actionHistory = history.split(",");

            ArrayList<String> userHistory = new ArrayList<>();

            for(String str : actionHistory){
                if(str.contains("Donor " + getCurrentDonor().getId())){
                    userHistory.add(str);
                }
            }
            historyView.setText(userHistory.toString());
        } catch (Exception e) {
            InvalidUsername();
        }
    }
}
