package odms.view.profile;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import odms.commons.model.profile.Profile;
import odms.controller.profile.ProfileCreate;
import odms.view.CommonView;

import java.io.IOException;
import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static odms.controller.AlertController.invalidEntry;

public class CreateAccount extends CommonView {
    @FXML
    private TextField givenNamesField;

    @FXML
    private TextField surnamesField;

    @FXML
    private DatePicker dobDatePicker;

    @FXML
    private TextField nhiField;

    private ProfileCreate controller = new ProfileCreate();

    /**
     * Scene change to profile view if all required fields are filled in.
     * @param event clicking on the create new account button.
     * @throws IOException throws IOException
     */
    @FXML
    private void handleCreateAccountButtonClicked(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ProfileDisplay.fxml"));
        Scene scene = new Scene(loader.load());
        Display v = loader.getController();
        Profile profile;
        if(checkDetailsEntered()) {
            profile = controller.createAccount(getGivenNamesFieldValue(),getsurnamesFieldValue(),getdobDatePickerValue(),getNhiField(),getNhiField().length());
        } else {
            profile = null;
        }
        if (profile != null) {
            v.initialize(profile, false, null, null);
            Stage appStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            appStage.setScene(scene);
            appStage.show();
        }
    }

    /**
     * Scene change to login view.
     *
     * @param event clicking on the log in link.
     * @throws IOException throws IOException
     */
    @FXML
    private void handleLoginLinkClicked(ActionEvent event) throws IOException {
        //controller.login();
        changeScene(event, "/view/Login.fxml", "Login");
    }

    public void initialize() {
        nhiField.textProperty().addListener((observable, oldValue, newValue) -> {
            String pattern = "^[A-HJ-NP-Z]{3}\\d{4}$";
            Pattern r = Pattern.compile(pattern);
            Matcher m = r.matcher(newValue);

            if (!m.matches() && !m.hitEnd()) {
                nhiField.setText(oldValue);
            }
        });

    }

    public String getGivenNamesFieldValue() {
        return givenNamesField.getText();
    }

    public String getsurnamesFieldValue() {
        return surnamesField.getText();
    }

    public LocalDate getdobDatePickerValue() {
        return dobDatePicker.getValue();
    }

    public String getNhiField() {
        return nhiField.getText();
    }

    public void setGivenNamesFieldValue(String s) {
        givenNamesField.setText(s);
    }

    public void setsurnamesFieldValue(String s) {
        surnamesField.setText(s);
    }

    public void setNhiField(String s) {
        nhiField.setText(s);
    }

    public void setdobDatePickerValue(LocalDate l) {
        dobDatePicker.setValue(l);
    }

    public ProfileCreate getController() {
        return controller;
    }

    private boolean checkDetailsEntered() {
        if (getGivenNamesFieldValue().isEmpty()) {
            invalidEntry("Please enter Given Name(s)");
            return false;
        }

        if (getsurnamesFieldValue().isEmpty()) {
            invalidEntry("Please enter Surname(s)");
            return false;
        }

        if (getdobDatePickerValue() == null) {
            invalidEntry("Please enter a Date of Birth");
            return false;
        }
        if (getNhiField().isEmpty()) {
            invalidEntry("Please enter an IRD number");
            return false;
        } else {
            return true;
        }
    }
}
