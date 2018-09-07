package odms.view;

import java.util.List;
import java.util.Locale;
import javafx.beans.Observable;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Tab;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import odms.commons.model.enums.CountriesEnum;
import odms.commons.model.enums.UserType;
import odms.commons.model.user.User;
import odms.controller.SettingsPopupController;
import odms.controller.database.DAOFactory;
import odms.controller.database.country.CountryDAO;
import odms.data.DefaultLocale;

public class SettingsPopup {

    private SettingsPopupController controller = new SettingsPopupController(this);
    private CountryDAO server = DAOFactory.getCountryDAO();
    private ObservableList<CountriesEnum> countriesEnumObservableList;

    @FXML private TableView countriesTable;
    @FXML private TableColumn<CountriesEnum, String> countriesColumn;
    @FXML private TableColumn<CountriesEnum, Boolean> allowedColumn;
    @FXML private ComboBox languageSelect;
    @FXML private ComboBox timeZoneSelect;
    @FXML private ComboBox datetimeSelect;
    @FXML private ComboBox numberSelect;
    @FXML private Tab countriesTab;


    /**
     * Confirms the changes to the settings made by the user.
     * @param event of the confirm button being clicked.
     */
    @FXML
    private void handleConfirmButtonClicked(ActionEvent event) {
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }

    /**
     * Applies the changes to the settings selected by the user.
     * @param event of the apply button being clicked.
     */
    @FXML
    private void handleApplyButtonClicked(ActionEvent event) {

    }

    /**
     * Closes the settings popup on click.
     * @param event of the cancel button being clicked.
     */
    @FXML
    private void handleCancelButtonClicked(ActionEvent event) {
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }

    /**
     * Populates the countries table with a list of countries. Populates a column with a checkbox
     * that is ticked if the country is valid.
     */
    private void setupCountriesTable() {
        List<String> allCountries = server.getAll();

        List<String> validCountries = server.getAll(true);
        for (String country : allCountries) {
            CountriesEnum countryEnum = CountriesEnum.getEnumByString(country);
            if (countryEnum != null && validCountries.contains(country)) {
                countryEnum.setValid(true);
                countriesEnumObservableList.add(countryEnum);
            } else {
                if (countryEnum != null) {
                    countryEnum.setValid(false);
                    countriesEnumObservableList.add(countryEnum);
                }
            }
        }
        countriesColumn.setSortable(false);
        allowedColumn.setSortable(false);
        countriesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        countriesTable.setItems(countriesEnumObservableList);
        countriesColumn.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        allowedColumn.setCellValueFactory(
                cellData -> new SimpleBooleanProperty(cellData.getValue().getValid()));
    }

    /**
     * Adds listeners to the valid countries checkboxes. One for key pressed and one for mouse
     * pressed.
     */
    private void addAllowedColumnListeners() {
        allowedColumn.setCellFactory(p -> {
            CheckBox checkBox = new CheckBox();
            TableCell<CountriesEnum, Boolean> tableCell = new TableCell<CountriesEnum, Boolean>() {

                @Override
                protected void updateItem(Boolean item, boolean empty) {

                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setGraphic(null);
                    } else {
                        setGraphic(checkBox);
                        checkBox.setSelected(item);
                    }
                }
            };

            checkBox.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
                CountriesEnum countriesEnum = ((CountriesEnum) tableCell.getTableRow().getItem());
                countriesEnum.setValid(!countriesEnum.getValid());
                countriesEnumObservableList.set(tableCell.getTableRow().getIndex(), countriesEnum);

                Integer count = 0;
                for (CountriesEnum country : countriesEnumObservableList) {
                    if (country.getValid()) {
                        count++;
                        if (count > 1) {
                            break;
                        }
                    }
                }
                if (count == 0) {
                    checkBox.setSelected(checkBox.isSelected());
                    countriesEnum.setValid(!countriesEnum.getValid());
                    countriesEnumObservableList.set(tableCell.getTableRow().getIndex(), countriesEnum);
                }

                server.update(countriesEnum,
                        countriesEnum.getValid());
            });

            checkBox.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                if (event.getCode() == KeyCode.SPACE) {
                    CountriesEnum countriesEnum = ((CountriesEnum) tableCell.getTableRow().getItem());
                    checkBox.setSelected(!countriesEnum.getValid());
                }
            });

            tableCell.setAlignment(Pos.CENTER);
            tableCell.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            return tableCell;
        });
    }

    /**
     * Initializes the content displayed by the view.
     * @param currentUser the user currently logged in.
     */
    public void initialize(User currentUser) {
        countriesEnumObservableList = FXCollections.observableArrayList(
                param -> new Observable[]{param.getValidProperty()});

        languageSelect.getItems().addAll(controller.getLanguageOptions());
        languageSelect.setValue(DefaultLocale.getLocale().getDisplayLanguage());

        if (!(currentUser.getUserType().equals(UserType.ADMIN))) {
            countriesTab.setDisable(true);
        } else {
            setupCountriesTable();
            addAllowedColumnListeners();
        }
    }
}
