package odms.controller;

import java.time.LocalDate;
import java.util.HashSet;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import odms.enums.OrganEnum;
import odms.profile.Profile;

public class ProfileOrganRemovalController {

    @FXML
    private Label dynamicLabel;

    @FXML
    private Label organLabel;

    @FXML
    private ComboBox<String> reasonSelector;

    @FXML
    private GridPane windowGrid;

    private CheckBox curedCheck = new CheckBox();

    private DatePicker dodPicker = new DatePicker(LocalDate.now());

    private Profile currentProfile;

    private ProfileOrganEditController profileOrganEditController;

    private String currentOrgan;

    /**
     * Confirms the changes made to the organs required and stores the reason given for this
     * change.
     * @param event the ActionEvent
     */
    @FXML
    private void handleConfirmButtonAction(ActionEvent event) {
        Stage appStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        String selection = reasonSelector.getSelectionModel().getSelectedItem();
        switch (selection) {
            case "Error":
                removeOrgan();
                break;

            case "No longer required":
                removeOrgan();
                break;

            case "Patient deceased":
                removeAllOrgans();
                currentProfile.setDateOfDeath(dodPicker.getValue());
                HashSet<OrganEnum> organsRequired = new HashSet<>(
                    currentProfile.getOrgansRequired()
                );
                currentProfile.removeOrgansRequired(organsRequired);
                break;
        }
        appStage.close();
    }

    /**
     * Removes the selected organ from the observable list of required organs displayed.
     */
    private void removeOrgan() {
        profileOrganEditController.observableListOrgansSelected.remove(currentOrgan);
        profileOrganEditController.observableListOrgansAvailable.add(currentOrgan);
    }

    /**
     * Removes all organs from the observable list of required organs displayed.
     */
    private void removeAllOrgans() {
        profileOrganEditController.observableListOrgansAvailable.addAll(
            profileOrganEditController.observableListOrgansSelected
        );
        profileOrganEditController.observableListOrgansSelected.clear();
    }

    /**
     * Closes the window when the cancel button is clicked.
     * @param event the
     */
    @FXML
    private void handleCancelButtonAction(ActionEvent event) {
        Stage appStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        appStage.close();
    }

    /**
     * Dynamically displays a date picker or checkbox based on the reason selected by the user.
     */
    @FXML
    private void handleReasonSelectionAction() {
        String reason = reasonSelector.getSelectionModel().getSelectedItem();

        switch (reason) {

            case "No longer required":
                dynamicLabel.setText("Cured : ");
                // Create cured checkbox.
                dodPicker.setVisible(false);
                curedCheck.setVisible(true);
                break;

            case "Patient deceased":
                dynamicLabel.setText("Date of death : ");
                // Create date picker for dod.
                dodPicker.setVisible(true);
                curedCheck.setVisible(false);
                break;

            default:
                dynamicLabel.setText("");
                dodPicker.setVisible(false);
                curedCheck.setVisible(false);
                break;
        }
    }

    /**
     * Sets the current profile and organ to be processed on start up. Also configures components
     * of the window.
     */
    @FXML
    public void initialize(String organ, Profile profile, ProfileOrganEditController controller) {
        currentProfile = profile;
        profileOrganEditController = controller;
        currentOrgan = organ;
        organLabel.setText(organLabel.getText() + organ);
        reasonSelector.getItems().addAll(
            "Error",
            "No longer required",
            "Patient deceased"
        );
        reasonSelector.setValue(reasonSelector.getItems().get(0));
        GridPane.setMargin(dodPicker, new Insets(0, 40, 0, 0));
        windowGrid.add(dodPicker, 2, 2, 2, 1);
        dodPicker.setVisible(false);
        GridPane.setMargin(curedCheck, new Insets(5, 0, 0, 0));
        windowGrid.add(curedCheck, 2,2, 2, 1);
        curedCheck.setVisible(false);
    }
}