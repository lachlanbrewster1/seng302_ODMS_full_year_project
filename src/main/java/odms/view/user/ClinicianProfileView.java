package odms.view.user;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import odms.App;
import odms.cli.CommandGUI;
import odms.cli.CommandLine;
import odms.controller.GuiMain;
import odms.controller.data.DataManagementControllerPOTENTIALTODO;
import odms.controller.history.RedoController;
import odms.controller.history.UndoController;
import odms.controller.user.ClinicianProfileEditController;
import odms.model.enums.OrganEnum;
import odms.model.profile.Profile;
import odms.model.user.User;
import odms.model.user.UserType;
import odms.view.CommonView;
import odms.view.profile.ProfileDisplayControllerTODO;
import org.controlsfx.control.CheckComboBox;
import org.controlsfx.control.table.TableFilter;

import java.io.IOException;
import java.util.*;

public class ClinicianProfileView extends CommonView {
    private static Collection<Stage> openProfileStages = new ArrayList<>();
    private User currentUser;
    @FXML
    private Label clinicianFullName;

    @FXML
    private Label donorStatusLabel;
    @FXML
    private Tab viewUsersTab;
    @FXML
    private Tab consoleTab;
    @FXML
    private Tab dataManagementTab;
    @FXML
    private Tab generalTab;
    @FXML
    private DataManagementControllerPOTENTIALTODO dataManagementControllerPOTENTIALTODO;
    private UserGeneralTabView userGeneralTabView = new UserGeneralTabView();
    protected ObjectProperty<User> currentUserBound = new SimpleObjectProperty<>();
    private UserConsoleTabView userConsoleTabView = new UserConsoleTabView();
    private ViewUsersView viewUsersView = new ViewUsersView();


    /**
     * Checks if there are unsaved changes in any open window.
     *
     * @return true if there are unsaved changes.
     */
    public static boolean checkUnsavedChanges(Stage currentStage) {
        for (Stage stage : openProfileStages) {
            //todo maybe need to move isEdited from common controller to common view?
            if (isEdited(stage) && stage.isShowing()) {
                return true;
            }
        }

        return isEdited(currentStage);
    }

    /**
     * closes all open profile windows that the user has opened.
     */
    public static void closeAllOpenProfiles() {
        for (Stage stage : openProfileStages) {
            if (stage.isShowing()) {
                stage.close();
            }
        }
    }

    /**
     * Scene change to log in view.
     *
     * @param event clicking on the logout button.
     */
    @FXML
    private void handleLogoutButtonClicked(ActionEvent event) throws IOException {
        currentUser = null;
        changeScene(event, "/view/Login.fxml");
    }


    /**
     * Sets all the clinicians details in the GUI.
     */
    @FXML
    private void setClinicianDetails() {
        donorStatusLabel.setText(currentUser.getUserType().getName());
        clinicianFullName.setText(currentUser.getName());
    }


    public void handleConsoleTabClicked(Event event) {
        userConsoleTabView.currentProfile.bind(currentUserBound);
    }

    /**
     * Initializes the controller for the view users Tab
     */
    public void handleViewUsersTabClicked() {
        viewUsersView.setCurrentUser(currentUser);
    }

    public void handleGeneralTabClicked(Event event) {
        if (currentUser != null) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/UserGeneralTab.fxml"));
            loader.setController(userGeneralTabView);
            try {
                generalTab.setContent(loader.load());
            } catch (IOException e){
                System.out.println(e.getMessage());
            }
            userGeneralTabView.initialize(currentUser);
        }
    }

    public void handleTabDataManagementClicked() {
        dataManagementControllerPOTENTIALTODO = new DataManagementControllerPOTENTIALTODO();
        dataManagementControllerPOTENTIALTODO.setCurrentUser(currentUser);
    }

    /**
     * Hides/Shows certain nodes if the clinician does / does not have permission to view them
     */
    private void setupAdmin() {
        if (currentUser.getUserType() == UserType.CLINICIAN) {
            dataManagementTab.setDisable(true);
            viewUsersTab.setDisable(true);
            consoleTab.setDisable(true);
        } else {
            dataManagementTab.setDisable(false);
            viewUsersTab.setDisable(false);
            consoleTab.setDisable(false);
        }
    }

    @FXML
    public void initialize() {
        if (currentUser != null) {

            currentUserBound.setValue(currentUser);
            setClinicianDetails();
            setupAdmin();
        }
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    private void closeStage(Stage stage) {
        openProfileStages.remove(stage);
    }

}
