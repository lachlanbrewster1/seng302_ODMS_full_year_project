package odms.user;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class User {

    private UserType userType;
    private String name;
    private Integer staffID;
    private String workAddress;
    private String region;
    private LocalDateTime lastUpdated;
    private ArrayList<String> updateActions = new ArrayList<>();
    private LocalDateTime timeOfCreation;

    /**
     * Logs which property was updated and the time it was updated
     * Also changes the last updated property
     * @param property the property that was updated
     */
    private void generateUpdateInfo(String property) {
        LocalDateTime currentTime = LocalDateTime.now();
        this.lastUpdated = currentTime;
        String output = property + " updated at " + currentTime.format(DateTimeFormatter.ofPattern("hh:mm a dd-MM-yyyy"));
        this.updateActions.add(output);
    }

    /**
     * user constructor
     * @param userType type of user
     * @param attrArray array containing users attributes
     */
    public User(UserType userType, ArrayList<String> attrArray){
        this.userType = userType;
        setExtraAttributes(attrArray);
        timeOfCreation = LocalDateTime.now();
    }

    /**
     * user constructor
     * @param userType type of user
     * @param name user name
     * @param region user region
     */
    public User(UserType userType, String name, String region){
        this.timeOfCreation = LocalDateTime.now();
        this.userType = userType;
        this.name = name;
        this.region = region;
        timeOfCreation = LocalDateTime.now();
        this.updateActions.add("Account for " + name + "created at " + LocalDateTime.now());
    }

    /**
     * Sets the attributes that are passed into the constructor
     * @param attributes the attributes given in the constructor
     * @throws IllegalArgumentException when a required attribute is not included or spelt wrong
     */
    public void setExtraAttributes(ArrayList<String> attributes) throws IllegalArgumentException {
        for (String val : attributes) {
            String[] parts = val.split("=");
            setGivenAttribute(parts);
        }
    }

    /**
     * sets a users specific given attribute
     * @param parts a string containing the users new attribute to be set
     * @throws IllegalArgumentException
     */
    private void setGivenAttribute(String[] parts) throws IllegalArgumentException {
        String attrName = parts[0];
        String value = parts[1].replace("\"", ""); // get rid of the speech marks;

        if (attrName.startsWith(" ")) {attrName = attrName.substring(1);}

        //TODO add in 'attribute' functionality like in profile/Profile eventually
        switch (attrName) {
            case "name":
                setName(value);
                break;
            case "workAddress":
                setWorkAddress(value);
                break;
            case "region":
                setRegion(value);
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    /**
     * gets a attribute summary of the user
     * @return attribute summary of the user
     */
    public String getAttributesSummary() {
        String summary = "";
        summary = summary +("staffID=" + staffID);
        summary = summary +"," +("name=" + name);
        summary = summary +"," +("workAddress=" + workAddress);
        summary = summary +"," +("region=" + region);
        return summary;
    }

    /**
     * Sets the name of the user
     * @param name name to be set
     */
    public void setName(String name){
        this.name = name;
        generateUpdateInfo(name);

    }

    /**
     * Gets the name of the user
     * @return name of the user
     */
    public String getName(){
        return this.name;
    }

    /**
     * Sets the staff id of the user
     * @param staffID staff id to be set
     */
    public void setStaffID(Integer staffID){
        this.staffID = staffID;
        generateUpdateInfo(staffID.toString());
    }

    /**
     * Gets the staff id of the user
     * @return staff id of the user
     */
    public Integer getStaffID(){
        return this.staffID;
    }

    /**
     * Sets the work address of the user
     * @param address address to be set
     */
    public void setWorkAddress(String address){
        this.workAddress = address;
        generateUpdateInfo(workAddress);
    }

    /**
     * Gets the work address of the user
     * @return work address of the user
     */
    public String getWorkAddress(){
        return this.workAddress;
    }

    /**
     * Sets the region of the user
     * @param region The region to be set
     */
    public void setRegion(String region){
        this.region = region;
        generateUpdateInfo(region);
    }

    /**
     * Gets the region of the user
     * @return region of the user
     */
    public String getRegion(){
        return this.region;
    }

    /**
     * Returns the update history of the user
     * @return update history of the user
     */
    public ArrayList<String> getUpdateActions() {
        return updateActions;
    }

    /**
     * Gets the user type of the user
     * @return user type of the user
     */
    public UserType getUserType() {
        return userType;
    }

    /**
     * Sets the user type of the user
     * @param userType user type of the user
     */
    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    /**
     * Gets the date when the user was last updated
     * @return date when the user was last updated
     */
    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    /**
     * Sets the date when the user was last updated
     * @param lastUpdated date when the user was last updated
     */
    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    /**
     * Gets the time of creation of the user
     * @return time of creation of the user
     */
    public LocalDateTime getTimeOfCreation() {
        return timeOfCreation;
    }

    /**
     * Sets the time of creation of the user
     * @param timeOfCreation time of creation of the user
     */
    public void setTimeOfCreation(LocalDateTime timeOfCreation) {
        this.timeOfCreation = timeOfCreation;
    }
}
