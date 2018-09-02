package odms.view.user;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import odms.commons.model.enums.BloodTypeEnum;
import odms.commons.model.enums.NewZealandRegionsEnum;
import odms.commons.model.enums.OrganEnum;
import odms.commons.model.profile.Profile;
import odms.commons.model.user.User;
import odms.controller.user.OrganExpiryProgressBar;
import odms.view.CommonView;
import org.controlsfx.control.CheckComboBox;

import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;

import static odms.controller.user.AvailableOrgans.*;

/**
 * Available organs view.
 */
public class AvailableOrgans extends CommonView {

    @FXML
    private CheckBox ageRangeCheckbox;
    @FXML
    private TextField ageField;
    @FXML
    private TextField ageRangeField;

    // To add to fxml ^^^^


    @FXML
    private TextField nameSearchField;
    @FXML
    private CheckComboBox bloodTypeComboboxMatchesTable;
    @FXML
    private CheckComboBox regionsComboboxMatchesTable;
    @FXML
    private CheckComboBox organsCombobox;
    @FXML
    private CheckComboBox regionsCombobox;
    @FXML
    private TableView availableOrgansTable;
    @FXML
    private TableView<Profile> potentialOrganMatchTable;

    private boolean filtered = false;
    private OrganEnum selectedOrgan;
    private Profile selectedProfile;

    private ObservableList<Entry<Profile, OrganEnum>> listOfAvailableOrgans;
    private ObservableList<Map.Entry<Profile, OrganEnum>> listOfFilteredAvailableOrgans;
    private ObservableList<Profile> potentialOrganMatches = FXCollections.observableArrayList();
    private ClinicianProfile parentView;
    private odms.controller.user.AvailableOrgans controller =
            new odms.controller.user.AvailableOrgans();

    private ObservableList<String> organsStrings = FXCollections.observableArrayList();

    private User currentUser;


    /**
     * Called if the age range is toggled.
     */
    @FXML
    private void handleAgeRangeCheckboxChecked() {
        if (ageRangeCheckbox.isSelected()) {
            ageRangeField.setDisable(false);
            ageField.setPromptText("Lower Age");
            ageRangeField.setPromptText("Upper Age");
            ageRangeField.clear();
        } else {
            ageRangeField.setDisable(true);
            ageField.setPromptText("Age");
        }
        // update
    }

    /**
     * Populates the matches table with potential matches.
     */
    private void populateMatchesTable() {

        potentialOrganMatchTable.getColumns().clear();
        potentialOrganMatchTable.getItems().clear();

        TableColumn<Profile, String> waitTimeColumn = new TableColumn<>(
                "Wait time"
        );
        waitTimeColumn.setCellValueFactory(
                cdf -> new SimpleStringProperty(
                        getWaitTime(selectedOrgan, cdf.getValue().getOrgansRequired(), cdf.getValue())));

        TableColumn<Profile, String> ageColumn = new TableColumn<>(
                "Age"
        );
        ageColumn.setCellValueFactory(
                cdf -> new SimpleStringProperty(
                        String.valueOf(cdf.getValue().getAge())));

        TableColumn<Profile, String> nhiColumn = new TableColumn<>(
                "NHI"
        );
        nhiColumn.setCellValueFactory(
                cdf -> new SimpleStringProperty(
                        String.valueOf(cdf.getValue().getNhi())));

        TableColumn<Profile, String> locationColumn = new TableColumn<>(
                "Location"
        );
        locationColumn.setCellValueFactory(
                cdf -> new SimpleStringProperty(
                        cdf.getValue().getCountry() + ", " + cdf.getValue().getRegion()));

        potentialOrganMatchTable.getColumns().add(waitTimeColumn);
        potentialOrganMatchTable.getColumns().add(ageColumn);
        potentialOrganMatchTable.getColumns().add(nhiColumn);
        potentialOrganMatchTable.getColumns().add(locationColumn);

        setPotentialOrganMatchesList();

        // Sorting on wait time, need to add in distance from location of organ as a 'weighting'
        Comparator<Profile> comparator1 = (o1, o2) -> {
            if (getWaitTimeRaw(selectedOrgan, o1.getOrgansRequired(), o1) >
                    getWaitTimeRaw(selectedOrgan, o2.getOrgansRequired(), o2))
                return 1;
            else if (getWaitTimeRaw(selectedOrgan, o1.getOrgansRequired(), o1).equals(
                    getWaitTimeRaw(selectedOrgan, o2.getOrgansRequired(), o2))) {
                return 0;
            } else {
                return -1;
            }
        };
        FXCollections.sort(potentialOrganMatchTable.getItems(), comparator1);

        potentialOrganMatchTable.setOnMousePressed(event -> {
            if (event.isPrimaryButtonDown() && event.getClickCount() == 2 &&
                    potentialOrganMatchTable.getSelectionModel().getSelectedItem() != null) {
                createNewDonorWindow(potentialOrganMatchTable.getSelectionModel().getSelectedItem(), parentView, currentUser);
            }
        });

    }

    /**
     * Populates the organs table.
     */
    private void populateOrgansTable() {

        availableOrgansTable.getColumns().clear();
        TableColumn<Map.Entry<Profile, OrganEnum>, String> organCol = new TableColumn<>(
                "Organ");
        organCol.setCellValueFactory(
                cdf -> new SimpleStringProperty(cdf.getValue().getValue().getName()));

        TableColumn<Map.Entry<Profile, OrganEnum>, String> dateOfDeathNameCol = new TableColumn<>(
                "Death");
        dateOfDeathNameCol.setCellValueFactory(
                cdf -> new SimpleStringProperty(
                        cdf.getValue().getKey().getDateOfDeath().toString()));
        dateOfDeathNameCol.setMinWidth(35);

        TableColumn<Map.Entry<Profile, OrganEnum>, String> countdownCol = new TableColumn<>(
                "Countdown"
        );
        countdownCol.setCellValueFactory(
                cdf -> new SimpleStringProperty(getTimeToExpiryHoursSeconds(
                        cdf.getValue().getValue(), cdf.getValue().getKey())
                )
        );
        Comparator<String> comparatorCountdownHrSec =
                Comparator.comparingInt(odms.controller.user.AvailableOrgans::hoursAndSecondsToMs);

        countdownCol.setComparator(comparatorCountdownHrSec);

        TableColumn<Map.Entry<Profile, OrganEnum>, String> nhiCol = new TableColumn<>(
                "NHI");
        nhiCol.setCellValueFactory(
                cdf -> new SimpleStringProperty((cdf.getValue().getKey().getNhi())));

        TableColumn<Map.Entry<Profile, OrganEnum>, Double> expiryProgressBarCol = new TableColumn(
                "Expiry");
        expiryProgressBarCol.setCellValueFactory(
                cdf -> new SimpleDoubleProperty(
                        getTimeRemaining(cdf.getValue().getValue(), cdf.getValue().getKey())
                                / getExpiryLength(cdf.getValue().getValue())).asObject()
        );

        expiryProgressBarCol.setCellFactory(OrganExpiryProgressBar.forTableColumn(
                this.getClass().getResource("/styles/Common.css").toExternalForm())
        );


        availableOrgansTable.getColumns().add(organCol);
        availableOrgansTable.getColumns().add(dateOfDeathNameCol);
        availableOrgansTable.getColumns().add(countdownCol);
        availableOrgansTable.getColumns().add(nhiCol);
        availableOrgansTable.getColumns().add(expiryProgressBarCol);
        availableOrgansTable.getItems().clear();

        try {
            setAvailableOrgansList();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // Sorting on wait time, need to add in distance from location of organ as a 'weighting'
        Comparator<Map.Entry<Profile, OrganEnum>> comparator = (o1, o2) -> {
            if (getTimeRemaining(o1.getValue(), o1.getKey()) < getTimeRemaining(o2.getValue(), o2.getKey())) {
                return -1;
            } else if (getTimeRemaining(o2.getValue(), o2.getKey()) < getTimeRemaining(o1.getValue(), o1.getKey())) {
                return 1;
            } else {
                return 0;
            }
        };
        FXCollections.sort(availableOrgansTable.getItems(), comparator);
        availableOrgansTable.setOnMousePressed(event -> {
            if (event.isPrimaryButtonDown() && event.getClickCount() == 2 &&
                    availableOrgansTable.getSelectionModel().getSelectedItem() != null) {
                createNewDonorWindow(((Map.Entry<Profile, OrganEnum>) availableOrgansTable.getSelectionModel()
                        .getSelectedItem()).getKey(), parentView, currentUser);
            } else if (event.isPrimaryButtonDown() && event.getClickCount() == 1 &&
                    availableOrgansTable.getSelectionModel().getSelectedItem() != null) {
                selectedOrgan = ((Map.Entry<Profile, OrganEnum>) availableOrgansTable.getSelectionModel().getSelectedItem()).getValue();
                selectedProfile = ((Map.Entry<Profile, OrganEnum>) availableOrgansTable.getSelectionModel().getSelectedItem()).getKey();

                setPotentialOrganMatchesList();
                updateMatchesTable();
            }
        });
    }

    /**
     * Populates available organs table with ALL available organs in database
     *
     * @throws SQLException exception thrown when accessing DB to get all available organs
     */
    private void setAvailableOrgansList() throws SQLException {
        listOfAvailableOrgans = FXCollections.observableArrayList(controller.getAllOrgansAvailable());
        availableOrgansTable.setItems(listOfAvailableOrgans);
        listOfFilteredAvailableOrgans = listOfAvailableOrgans;
    }

    /**
     * Populates available organs table with ALL available organs in database
     */
    private void setPotentialOrganMatchesList() {

        try {
            OrganEnum organToMatch = selectedOrgan;
            Profile donorProfile = ((Map.Entry<Profile, OrganEnum>) availableOrgansTable.getSelectionModel().getSelectedItem()).getKey();

            potentialOrganMatches = controller.getSuitableRecipientsSorted(
                    organToMatch, donorProfile, selectedOrgan, nameSearchField.getText(), bloodTypeComboboxMatchesTable.getCheckModel().getCheckedItems(),
                    regionsComboboxMatchesTable.getCheckModel().getCheckedItems(), ageField.getText(), ageRangeField.getText(),
                    ageRangeCheckbox.isSelected());

        } catch (NullPointerException e) {
            // No organ selected in table
        }
    }

    /**
     * Updates the available organs list according to the active filters
     */
    private void performOrganSearchFromFilters() {
        listOfFilteredAvailableOrgans = FXCollections.observableArrayList();
        listOfFilteredAvailableOrgans.clear();
        for (Map.Entry<Profile, OrganEnum> m : listOfAvailableOrgans) {
            if (organsCombobox.getCheckModel().getCheckedItems().contains(m.getValue()) && regionsCombobox.getCheckModel().getCheckedItems().contains(m.getKey().getRegionOfDeath())) {
                listOfFilteredAvailableOrgans.add(m);
            } else if (organsCombobox.getCheckModel().getCheckedItems().contains(m.getValue()) && regionsCombobox.getCheckModel().getCheckedItems().size() == 0) {
                listOfFilteredAvailableOrgans.add(m);
            } else if (organsCombobox.getCheckModel().getCheckedItems().size() == 0 && regionsCombobox.getCheckModel().getCheckedItems().size() == 0) {
                listOfFilteredAvailableOrgans.add(m);
            } else if (organsCombobox.getCheckModel().getCheckedItems().size() == 0 && regionsCombobox.getCheckModel().getCheckedItems().contains(m.getKey().getRegionOfDeath())) {
                listOfFilteredAvailableOrgans.add(m);
            }
        }
        if (listOfFilteredAvailableOrgans.size() != 0 || organsCombobox.getCheckModel().getCheckedItems().size() != 0 || regionsCombobox.getCheckModel().getCheckedItems().size() != 0) {
            availableOrgansTable.setItems(listOfFilteredAvailableOrgans);
        } else {
            availableOrgansTable.setItems(listOfAvailableOrgans);
        }
    }

    /**
     * Clears the potential organ match table and updates with the updated profiles
     */
    private void updateMatchesTable() {
        potentialOrganMatchTable.setItems(potentialOrganMatches);
    }

    /**
     * Initializes the Available organs view. Sets the current user and
     * the clinician profile parent view.
     *
     * @param currentUser current user logged in.
     * @param p           parent view.
     */
    public void initialize(User currentUser, ClinicianProfile p) {
        this.currentUser = currentUser;
        controller.setView(this);
        populateOrgansTable();
        populateMatchesTable();
        parentView = p;

        regionsCombobox.getItems().setAll(NewZealandRegionsEnum.toArrayList());
        regionsCombobox.getCheckModel().getCheckedItems().addListener((ListChangeListener) c -> performOrganSearchFromFilters());

        organsStrings.clear();
        organsStrings.addAll(OrganEnum.toArrayList());
        organsCombobox.getItems().setAll(OrganEnum.values());
        organsCombobox.getCheckModel().getCheckedItems().addListener((ListChangeListener) c -> performOrganSearchFromFilters());


        regionsComboboxMatchesTable.getItems().setAll(NewZealandRegionsEnum.toArrayList());
        regionsComboboxMatchesTable.getCheckModel().getCheckedItems().addListener((ListChangeListener) c -> performOrganSearchFromFilters());

        bloodTypeComboboxMatchesTable.getItems().setAll(BloodTypeEnum.toArrayList());
        bloodTypeComboboxMatchesTable.getCheckModel().getCheckedItems().addListener((ListChangeListener) c -> performOrganSearchFromFilters());

        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                availableOrgansTable.refresh();
                potentialOrganMatchTable.refresh();
                List<Entry<Profile, OrganEnum>> toRemove = new ArrayList<>(listOfAvailableOrgans);
                for (Map.Entry<Profile, OrganEnum> m : toRemove) {
                    controller.checkOrganExpiredListRemoval(m.getValue(), m.getKey(), m);
                }
            }
        }, 0, 1);

    }

    public ObservableList<Map.Entry<Profile, OrganEnum>> getListOfAvailableOrgans() {
        return listOfAvailableOrgans;
    }

    /**
     * Removes an item from the list of available organs.
     *
     * @param m item to remove.
     */
    public void removeItem(Map.Entry<Profile, OrganEnum> m) {
        listOfAvailableOrgans.remove(m);
    }
}
