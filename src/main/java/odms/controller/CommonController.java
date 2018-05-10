package odms.controller;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

class CommonController {

    /**
     * Scene change to log in view.
     *
     * @param event clicking on the logout button.
     */
    @FXML
    public void showLoginScene(ActionEvent event) throws IOException {
        Parent parent = FXMLLoader.load(getClass().getResource("/view/Login.fxml"));
        Scene newScene = new Scene(parent);
        Stage appStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        appStage.setScene(newScene);
        appStage.setResizable(false);
        appStage.setTitle("ODMS");
        appStage.show();
    }

    /**
     * JavaFX Scene loader
     * @param event the ActionEvent
     * @param scene the fxml path
     * @param title the window title
     * @throws IOException if the path is invalid
     */
    protected void showScene(ActionEvent event, String scene, String title) throws IOException {
        showScene(event, scene, title, false);
    }

    /**
     * JavaFX Scene loader
     * @param event the ActionEvent
     * @param scene the fxml path
     * @param title the window title
     * @param resizeable if the window can be resized
     * @throws IOException if the path is invalid
     */
    protected void showScene(ActionEvent event, String scene, String title, Boolean resizeable)
        throws IOException {
        Parent parent = FXMLLoader.load(getClass().getResource(scene));
        Scene newScene = new Scene(parent);
        Stage appStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        appStage.setScene(newScene);
        appStage.setResizable(resizeable);
        appStage.setTitle(title);
        appStage.show();
    }

    /**
     * Changes the Edit Profile title to include an astrix to indicate a value has been edited.
     * @param event Any key event within the text boxes.
     */
    @FXML
    protected void editTrueKey(javafx.scene.input.KeyEvent event) throws IOException {
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        if(!currentStage.getTitle().contains("(*)")){
            currentStage.setTitle(currentStage.getTitle() + " (*)");
        }
    }

    /**
     * Changes the Edit Profile title to include an astrix to indicate a value has been edited.
     * @param event Any action event within the text boxes.
     */
    @FXML
    protected void editTrueAction(ActionEvent event) throws IOException {
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        if(!currentStage.getTitle().contains("(*)")){
            currentStage.setTitle(currentStage.getTitle() + " (*)");
        }
    }

    /**
     * Changes the Edit Profile title to include an astrix to indicate a value has been edited.
     * @param event Any click event within the text boxes.
     */
    @FXML
    protected void editTrueClick(MouseEvent event) throws IOException {
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        if(!currentStage.getTitle().contains("(*)")){
            currentStage.setTitle(currentStage.getTitle() + " (*)");
        }
    }

    //TODO
    @FXML
    protected void showNotification(String editedField, ActionEvent event) throws IOException {
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        Notifications.create()
                .title("Edit Successful")
                .text("The " + editedField + " was edited successfully!")
                .hideAfter(Duration.millis(2500))
                .position(Pos.BOTTOM_LEFT)
                .owner(currentStage)
                .show();

    }

}
