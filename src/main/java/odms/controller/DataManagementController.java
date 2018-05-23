package odms.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import odms.data.ProfileDataIO;
import odms.user.User;

import java.io.File;
import java.io.IOException;

public class DataManagementController {

    public User currentUser;

    @FXML
    private AnchorPane dataManagementAp;

    /**
     * Opens a file chooser and imports the selected files.
     * @param actionEvent
     */
    public void handleImportSavedDataClicked(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("JSON file(*.json)",
                "*.json");
        fileChooser.getExtensionFilters().add(extFilter);
        Stage stage = (Stage) dataManagementAp.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) { // Check that the user actually selected a file
            if (ClinicianProfileController.checkUnsavedChanges((Stage) dataManagementAp.getScene().getWindow())) {
                if (AlertController.unsavedChangesImport()) {
                    importAndCloseWindows(stage, file);
                }
            } else {
                importAndCloseWindows(stage, file);
            }
        }
    }

    /**
     * Imports new json file.
     * Closes all open windows and re-initializes the admin view.
     * @param stage Stage to be close
     * @param file file to be set as database
     */
    private void importAndCloseWindows(Stage stage, File file) {
        GuiMain.setCurrentDatabase(ProfileDataIO.loadData(file.getPath()));

        ClinicianProfileController.closeAllOpenProfiles();
        stage.close();

        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/view/ClinicianProfile.fxml"));

        try {
            Scene scene = new Scene(fxmlLoader.load());

            ClinicianProfileController controller = fxmlLoader.getController();
            controller.setCurrentUser(currentUser);
            controller.initialize();

            stage = new Stage();
            stage.setTitle("Admin");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }
}
