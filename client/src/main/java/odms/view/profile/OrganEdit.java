package odms.view.profile;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import odms.commons.model.enums.OrganEnum;
import odms.commons.model.enums.OrganSelectEnum;
import odms.commons.model.profile.Procedure;
import odms.commons.model.profile.Profile;
import odms.controller.AlertController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Control Organ view tab pane.
 */
@Slf4j
public class OrganEdit extends OrganCommon {
    protected ObservableList<String> observableListOrgansSelected = FXCollections
            .observableArrayList();
    private Profile currentProfile;
    private odms.controller.profile.OrganEdit controller = new odms.controller.profile.OrganEdit(
            );

    @FXML
    private ListView<String> viewOrgansAvailable;
    @FXML
    private ListView<String> viewOrgansSelected;
    @FXML
    private Button btnOrganSwitch;
    @FXML
    private Button btnSave;
    @FXML
    private Label lblBanner;
    @FXML
    private Label lblSelected;

    private OrganSelectEnum windowType;

    private Procedure procedure;

    /**
     * Convert an HashSet of Organ Strings to a HashSet of OrganEnum.
     *
     * @param organStrings strings to convert
     * @return set of OrganEnum
     */
    private static Set<OrganEnum> observableListStringsToOrgans(Set<String> organStrings) {
        List<String> correctedOrganStrings = new ArrayList<>();

        for (String organ : organStrings) {
            correctedOrganStrings.add(organ.trim().toUpperCase().replace(" ", "_"));
        }

        return OrganEnum.stringListToOrganSet(correctedOrganStrings);
    }

    public void setWindowType(OrganSelectEnum windowType) {
        this.windowType = windowType;
    }

    private OrganSelectEnum getWindowType() {
        return windowType;
    }

    /**
     * Initialize the current view instance and populate organ lists.
     *
     * @param profile the profile to set on view instance
     */
    public void initialize(Profile profile) {
        currentProfile = profile;
        lblBanner.setText(getWindowType().toString());

        if (currentProfile != null) {
            // Order of execution for building these is required due to removing items from the
            // Available list that are present in the Required list.
            buildOrgansSelected();
            buildOrgansAvailable(observableListOrgansSelected);
            viewOrgansAvailable.setItems(observableListOrgansAvailable);
            viewOrgansSelected.setItems(observableListOrgansSelected);
        }

        btnOrganSwitch.setOnAction(this::handleBtnOrganSwitchClicked);
        viewOrgansAvailable.setOnMouseClicked(this::handleListOrgansAvailableClick);
        viewOrgansSelected.setOnMouseClicked(this::handleListOrgansRequiredClick);
    }

    /**
     * Populate the ListView with the organs that are available and that are not in the required
     * list.
     *
     * @param removeStrings organ strings to remove
     */
    protected void buildOrgansAvailable(ObservableList<String> removeStrings) {
        observableListOrgansAvailable = FXCollections.observableArrayList();
        if (getWindowType() == OrganSelectEnum.PROCEDURE) {
            for (OrganEnum organ : currentProfile.getOrgansDonating()) {
                if (!observableListOrgansSelected.contains(organ.getNamePlain())) {
                    observableListOrgansAvailable.add(organ.getNamePlain());
                }
            }
        } else {
            observableListOrgansAvailable.addAll(OrganEnum.toArrayList());
            observableListOrgansAvailable.removeIf(removeStrings::contains);
        }
    }

    /**
     * Support function to populate an observable list with organs from an organ set.
     *
     * @param destinationList list to populate
     * @param organs source list of organs to populate from
     */
    protected void populateOrganList(ObservableList<String> destinationList,
            Set<OrganEnum> organs) {
        destinationList.clear();

        if (getWindowType() == OrganSelectEnum.PROCEDURE) {
            for (OrganEnum organ : procedure.getOrgansAffected()) {
                destinationList.add(organ.getNamePlain());
            }
        } else {
            if (organs != null) {
                for (OrganEnum organ : organs) {
                    destinationList.add(organ.getNamePlain());
                }
            }
        }
        Collections.sort(destinationList);
    }

    /**
     * Populate the ListView with the organs the profile currently requires.
     */
    private void buildOrgansSelected() {
        Set<OrganEnum> organs = new HashSet<>();

        switch (getWindowType()) {
            case DONATED:
                lblSelected.setText("Donated");
                organs = currentProfile.getOrgansDonated();
                break;
            case DONATING:
                lblSelected.setText("Donating");
                organs = currentProfile.getOrgansDonating();
                break;
            case REQUIRED:
                lblSelected.setText("Required");
                organs = currentProfile.getOrgansRequired();
                break;
            case PROCEDURE:
                lblSelected.setText("Affected Organs");
                break;
            default:
                // noop
        }
        populateOrganList(observableListOrgansSelected, organs);
    }

    /**
     * Button to perform moving the organ from one ListView to the other ListView.
     *
     * @param event the JavaFX event.
     */
    private void handleBtnOrganSwitchClicked(Event event) {
        switchOrgans(event);
    }

    /**
     * Handle switching organs between views.
     *
     * @param event the JavaFX event.
     */
    private void handleListOrgansAvailableClick(MouseEvent event) {
        handleOrgansClick(event, viewOrgansSelected.getSelectionModel());
    }

    /**
     * Handle switching organs between views.
     *
     * @param event the JavaFX event.
     */
    private void handleListOrgansRequiredClick(MouseEvent event) {
        handleOrgansClick(event, viewOrgansAvailable.getSelectionModel());
    }

    /**
     * Click Handler to handle Click actions on the ListViews. - A single click will clear the
     * selection from the opposing ListView. - A double click will move the organ from the ListView
     * to the opposing ListView.
     *
     * @param event the MouseEvent
     * @param model the SelectionModel to operate against
     */
    private void handleOrgansClick(MouseEvent event, MultipleSelectionModel<String> model) {
        if (event.getButton() == MouseButton.PRIMARY) {
            model.clearSelection();

            if (event.getClickCount() == 2) {
                model.clearSelection();
                switchOrgans(event);
            }
        }
    }

    /**
     * Cancel the current changes in the view and close the window.
     */
    public void onBtnCancelClicked() {
        Stage stage = (Stage) btnSave.getScene().getWindow();
        stage.close();
    }

    /**
     * Save the changes made in the current view and close the window.
     */
    public void onBtnSaveClicked() {
        switch (getWindowType()) {
            case DONATED:
                controller.caseDonated(getCurrentProfile(), getOrgansAdded());
                break;
            case DONATING:
                controller.caseDonating(getCurrentProfile(), getOrgansAdded());
                break;
            case REQUIRED:
                controller.caseRequired(getCurrentProfile(), getOrgansAdded());
                break;
            case PROCEDURE:
                List<OrganEnum> organs = new ArrayList<>(getOrgansAdded());
                procedure.setOrgansAffected(organs);
                break;
            default:
                // noop
        }
        if (getWindowType() != OrganSelectEnum.PROCEDURE) {
            controller.saveOrgans(getCurrentProfile());
        }
        Stage stage = (Stage) btnSave.getScene().getWindow();
        stage.close();
    }

    /**
     * Refresh the listViews.
     */
    private void refreshListViews() {
        Collections.sort(observableListOrgansSelected);
        Collections.sort(observableListOrgansAvailable);

        viewOrgansSelected.refresh();
        viewOrgansAvailable.refresh();
    }

    /**
     * Take the selected organ from the ListView and move it to the other ListView.
     *
     * @param event the JavaFX event.
     */
    private void switchOrgans(Event event) {
        if (currentProfile.getBloodType() != null) {
            if (viewOrgansAvailable.getFocusModel().getFocusedIndex() != -1) {
                String item = viewOrgansAvailable.getSelectionModel().getSelectedItem();
                observableListOrgansAvailable.remove(item);
                observableListOrgansSelected.add(item);
            } else if (viewOrgansSelected.getSelectionModel().getSelectedIndex() != -1) {
                String item = viewOrgansSelected.getSelectionModel().getSelectedItem();
                if (getWindowType() == OrganSelectEnum.PROCEDURE) {
                    observableListOrgansAvailable.add(item);
                    observableListOrgansSelected.remove(item);
                } else {
                    giveReasonForRemoval(event, item);
                }
            }
            refreshListViews();

            viewOrgansAvailable.getSelectionModel().clearSelection();
            viewOrgansSelected.getSelectionModel().clearSelection();
        } else {
            AlertController.invalidEntry("Must set a blood type before allocating an organ.");
        }
    }

    /**
     * Launch pane to add reasoning for organ removal.
     *
     * @param event the JavaFX event
     * @param organ the organ to specify reason for
     */
    private void giveReasonForRemoval(Event event, String organ) {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/view/ProfileOrganRemoval.fxml"));

        try {
            Scene scene = new Scene(fxmlLoader.load());
            OrganRemove removalView = fxmlLoader.getController();
            removalView.initialize(organ, this.currentProfile, this);

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Organ Removal");
            stage.initOwner(((Node) event.getSource()).getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.centerOnScreen();
            stage.setOnHiding(ob -> refreshListViews());
            stage.show();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    public Profile getCurrentProfile() {
        return currentProfile;
    }

    public Set<OrganEnum> getOrgansAdded() {
        return observableListStringsToOrgans(
                new HashSet<>(observableListOrgansSelected)
        );
    }

    public void setProcedure(Procedure procedure) {
        this.procedure = procedure;
    }
}
