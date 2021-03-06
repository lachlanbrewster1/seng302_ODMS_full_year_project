package odms.view.profile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import lombok.extern.slf4j.Slf4j;
import odms.Session;
import odms.commons.model.profile.Profile;
import odms.commons.model.user.User;
import odms.controller.AlertController;
import odms.controller.data.ImageDataIO;
import odms.controller.database.DAOFactory;
import odms.controller.database.common.CommonDAO;
import odms.view.CommonView;
import odms.view.SocialFeedTab;
import odms.view.user.TransplantWaitingList;

/**
 * The profile display view.
 */
@Slf4j
public class Display extends CommonView {

    private Profile currentProfile;

    /**
     * Text for showing recent edits.
     */
    @FXML
    private Label donorFullNameLabel;
    @FXML
    private Label donorStatusLabel;
    @FXML
    private Label nhiLabel;
    @FXML
    private Label receiverStatusLabel;
    @FXML
    private Tab tabGeneral;
    @FXML
    private Tab tabMedical;
    @FXML
    private Tab tabMedicalHistory;
    @FXML
    private Tab tabMedications;
    @FXML
    private Tab tabOrgans;
    @FXML
    private Tab tabProcedures;
    @FXML
    private Tab tabSocialFeed;
    @FXML
    private Button logoutButton;
    @FXML
    private ImageView profileImage;

    private Boolean isOpenedByClinician = false;
    private User currentUser;
    private TransplantWaitingList transplantWaitingListView;
    private SocialFeedTab socialFeed;
    private boolean socialFeedInitialised = false;

    // Displays in IntelliJ as unused but is a false positive
    // The FXML includes operate this way and allow access to the instantiated controller.

    /**
     * calls the controller method when the edit button is clicked.
     * @param event edit button clicked event.
     * @throws IOException thrown when edit window cannot be opened.
     */
    @FXML
    private void handleEditButtonClicked(ActionEvent event) throws IOException {
        handleProfileEditButtonClicked(event, currentProfile, isOpenedByClinician, currentUser);
    }

    /**
     * Scene change to log in view.
     *
     * @param event clicking on the logout button.
     * @throws IOException error displaying login window.
     */
    @FXML
    private void handleLogoutButtonClicked(ActionEvent event) throws IOException {
        CommonDAO server = DAOFactory.getCommonDao();
        server.logout();
        currentProfile = null;
        Session.setCurrentUser(null, null);
        if(socialFeedInitialised) {
            socialFeed.pauseTimer();
            socialFeedInitialised = false;
        }
        changeScene(event, "/view/Login.fxml", "Login");
    }


    /**
     * Sets all of the items in the fxml to their respective values.
     *
     * @param currentProfile donors profile
     */
    @FXML
    private void setPage(Profile currentProfile) {
        try {
            if (currentProfile.getPreferredName() != null &&
                    !currentProfile.getPreferredName().isEmpty()) {
                donorFullNameLabel.setText(currentProfile.getPreferredName());
            } else {
                donorFullNameLabel.setText(currentProfile.getFullName());
            }

            donorStatusLabel.setText(donorStatusLabel.getText() + "Unregistered");
            receiverStatusLabel.setText(receiverStatusLabel.getText() + "Unregistered");

            if (!currentProfile.getOrgansDonated().isEmpty() || !currentProfile.getOrgansDonating()
                    .isEmpty()) {
                donorStatusLabel.setText("Donor Status: Registered");
            }

            if (currentProfile.getOrgansRequired().isEmpty()) {
                currentProfile.setReceiver(false);
            } else {
                currentProfile.setReceiver(true);
            }

            if (!currentProfile.getOrgansReceived().isEmpty() || !currentProfile.getOrgansRequired().isEmpty()) {
                receiverStatusLabel.setText("Receiver Status: Registered");
            }

            if (currentProfile.getNhi() != null) {
                nhiLabel.setText("NHI : " + currentProfile.getNhi());
            }

            setProfileImage();

        } catch (MalformedURLException e) {
            AlertController.guiPopup("Server error. Please try again.");
        }
    }

    public Profile getCurrentProfile() {
        return currentProfile;
    }

    @FXML
    public void onTabGeneralSelected() {
        if (tabGeneral.isSelected() && currentProfile != null) {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/ProfileGeneralTab.fxml")
            );
            try {
                tabGeneral.setContent(loader.load());
                ProfileGeneral profileGeneralView = loader.getController();
                profileGeneralView.initialize(currentProfile);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    @FXML
    private void onTabOrgansSelected() {
        if (tabOrgans.isSelected()) {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/ProfileOrganOverview.fxml")
            );
            try {
                tabOrgans.setContent(loader.load());
                OrganDisplay organsView = loader.getController();
                organsView.initialize(
                        currentProfile, isOpenedByClinician, transplantWaitingListView,
                        currentUser);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    @FXML
    private void onTabSocialFeedSelected() {
        if (tabSocialFeed.isSelected()) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/SocialFeedTab.fxml"));
            try {
                tabSocialFeed.setContent(loader.load());
                socialFeed = loader.getController();
                socialFeed.initialise();
                socialFeedInitialised = true;
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }

        tabSocialFeed.setOnSelectionChanged(event -> {
            if (!tabSocialFeed.isSelected()) {
                socialFeed.pauseTimer();
            } else {
                socialFeed.startTimer();
            }
        });
    }


    @FXML
    public void onTabMedicalSelected() {
        if (tabMedical.isSelected()) {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/ProfileMedicalTab.fxml")
            );
            try {
                tabMedical.setContent(loader.load());
                ProfileMedical profileMedicalViewTODO = loader.getController();
                profileMedicalViewTODO.initialize(currentProfile, isOpenedByClinician);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    @FXML
    public void onTabMedicationsSelected() {
        if (tabMedications.isSelected()) {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/ProfileMedicationsTab.fxml")
            );
            try {
                tabMedications.setContent(loader.load());
                MedicationsGeneral profileMedicationsView = loader.getController();
                profileMedicationsView.initialize(currentProfile, isOpenedByClinician);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    @FXML
    public void onTabMedicalHistorySelected() {
        if (tabMedicalHistory.isSelected()) {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/ProfileMedicalHistoryTab.fxml")
            );
            try {
                tabMedicalHistory.setContent(loader.load());
                ProfileMedicalHistory profileMedicalHistoryView = loader.getController();
                profileMedicalHistoryView.initialize(currentProfile, isOpenedByClinician);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    @FXML
    public void onTabProceduresSelected() {
        if (tabProcedures.isSelected()) {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/ProfileProceduresTab.fxml")
            );
            try {
                tabProcedures.setContent(loader.load());
                ProceduresDisplay profileProceduresView = loader.getController();
                profileProceduresView.initialize(currentProfile, isOpenedByClinician);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    private void setProfileImage() throws MalformedURLException {
        File image = ImageDataIO.getImagePath(currentProfile.getPictureName());

        String defaultImage = ImageDataIO.getPath() + "/default.png";
        String defaultImageFormatted = defaultImage.replace("target/client/", "");

        if (image == null || !image.exists()) {
            image = new File(
                    defaultImageFormatted
            );
        }

        profileImage.setImage(new Image(image.toURI().toURL().toString()));
    }

    /**
     * Sets the current donor attributes to the labels on start up.
     * @param profile to be used
     * @param isOpenedByClinician boolean, if true profile has been opened by a clinician/admin
     * @param transplantWaitingList view for the transplantWaitingList. Will have null value if
     * profile was not opened by a clinician or admin
     * @param currentUser the current logged in clin/admin
     */
    public void initialize(Profile profile, Boolean isOpenedByClinician,
            TransplantWaitingList transplantWaitingList, User currentUser) {
        this.isOpenedByClinician = isOpenedByClinician;
        this.currentUser = currentUser;

        currentProfile = profile;
        setPage(profile);
        onTabGeneralSelected();
        if (transplantWaitingList != null) {
            transplantWaitingListView = transplantWaitingList;
        }
        if (isOpenedByClinician) {
            logoutButton.setVisible(false);
        }
    }
}
