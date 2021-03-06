package odms.cli.commands;

import java.util.Arrays;

public final class Help {

    private Help() {
        throw new UnsupportedOperationException();
    }

    /**
     * Output a list of all commands and explanations/examples of use
     */
    public static void help() {
        System.out.println("\n-Create a new clinician:");
        System.out.println("create-clinician {attributes (name, username and password is required)}");
        System.out.println("e.g. create-clinician name=\"Bob Ross\"");

        System.out.println("\n-View a clinician:");
        System.out.println("clinician {attributes to search clinicians by} > view");

        System.out.println("\n-Delete a clinician:");
        System.out.println("clinician {attributes to search clinicians by} > delete");

        System.out.println("\n-View the date a clinician was created:");
        System.out.println("clinician {attributes to search clinicians by} > date-created");

        System.out.println("\n-Update a clinicians attributes:");
        System.out.println("clinician {attributes to search clinicians by} > {attributes to update}");
        System.out.println("e.g. clinician name=\"Bob Ross\" > region=\"Waikato\"");

        System.out.println("\n-Create a new profile:");
        System.out.println("create-profile {attributes (given-names, last-names, dob and ird is "
                + "required)}");
        System.out.println("e.g. create-profile given-names=\"Abby Rose\" last-names=\"Walker\" "
                + "dob=\"03-03-1998\" ird=\"123456789\"");

        System.out.println("\n-View a profile:");
        System.out.println("profile {attributes to search profiles by} > view");

        System.out.println("\n-Delete a profile:");
        System.out.println("profile {attributes to search profiles by} > delete");

        System.out.println("\n-View the date a profile was created:");
        System.out.println("profile {attributes to search profiles by} > date-created");

        System.out.println("\n-Update a profiles attributes:");
        System.out.println("profile {attributes to search profiles by} > {attributes to update}");
        System.out
                .println("e.g. profile given-names=\"Abby Rose\" last-names=\"Walker\" dob=\"03-03"
                        + "-1998\" > height=\"169\"");

        System.out.println("\n-View a profiles past donations, required organs and currently "
                + "donating organs:");
        System.out.println("profile {attributes to search profiles by} > organs");

        System.out.println("\n-Add an organ to donate:");
        System.out
                .println(
                        "profile {attributes to search profiles by} > add-organ=\" {list of organs "
                                + "to donate} \"");
        System.out
                .println("e.g. profile given-names=\"Abby Rose\" last-names=\"Walker\" dob=\"03-03"
                        + "-1998\" > add-organ=\"liver, kidney\"");

        System.out.println("\n-Remove an organ to donate:");
        System.out.println("profile {attributes to search profiles by} > remove-organ=\" {list of "
                + "organs to remove} \"");
        System.out.println("e.g. profile given-names=\"Abby Rose\" last-names=\"Walker\" dob=\"03-"
                + "03-1998\" > remove-organ=\"liver, kidney\"");

        System.out.println("\n-Add an organ to profile(s) donated organs:");
        System.out
                .println("profile {attributes to search profiles by} > donate-organ=\" {list of "
                        + "donated organs} \"");
        System.out
                .println("e.g. profile given-names=\"Abby Rose\" last-names=\"Walker\" dob=\"03-03"
                        + "-1998\" > donate-organ=\"liver, kidney\"");

        System.out.println("\n-Add an organ to receive:");
        System.out
                .println("profile {attributes to search profiles by} > receive-organ=\" {list of organs "
                        + "to donate} \"");
        System.out
                .println("e.g. profile given-names=\"Abby Rose\" last-names=\"Walker\" dob=\"03-03"
                        + "-1998\" > receive-organ=\"liver, kidney\"");

        System.out.println("\n-Remove an organ to receive:");
        System.out.println("profile {attributes to search profiles by} > remove-receive-organ=\" {list of "
                + "organs to remove} \"");
        System.out.println("e.g. profile given-names=\"Abby Rose\" last-names=\"Walker\" dob=\"03-"
                + "03-1998\" > remove-receive-organ=\"liver, kidney\"");

        System.out.println("\n-Print all profiles: ");
        System.out.println("print all profiles");

        System.out.println("\n-Print all donors: ");
        System.out.println("print all donors");

        System.out.println("\n-Print all clinicians: ");
        System.out.println("print all clinicians");

        System.out.println("\n-Print all users: ");
        System.out.println("(Users are all admins and clinicians)");
        System.out.println("print all users");

        System.out.println("\n-Import/export data: ");
        System.out.println("export");
        System.out.println("import {filepath}");

        System.out.println("\n-Make select call to the external database: ");
        System.out.println("db-read {SELECT statement}");

        System.out.println("\n-Close the app: ");
        System.out.println("quit");

        System.out.println("\n-------------------------------------------------------------------");
        System.out.println("-profile attributes:");
        System.out.println("given-names, last-names, dob, dod, gender, height, weight, blood-type,"
            + " address, region, country, nhi");

        System.out.println("\n-user attributes:");
        System.out.println("name, password, username, workAddress, staffID, region");

        System.out.println("\n-Organs:");
        System.out.println(
            "Liver, Kidney, Pancreas, Heart, Lung, Intestine, Cornea, Middle Ear, Skin, Bone, "
                + "Bone Marrow, Connective Tissue");

        System.out.println("\n-------------------------------------------------------------------");
    }

    /**
     * Output usage information for specified command.
     *
     * @param cmd command requesting usage for
     */
    public static void helpSpecific(String cmd) {
        String[] cmdArray = {"create-profile", "create-clinician", "view", "date-created", "organs",
                "update", "add-organ", "remove-organ", "print all profiles", "print all clinicians",
                "print all donors", "quit", "donate-organ", "delete", "receive-organ",
                "remove-receive-organ", "import", "export", "db-read",
                "print all users", "attribute list", "organ list"};
        String[] definitionArray = {"\nCreate a new profile", "\nCreate a new clinician",
                "\nView a profile or clinician", "\nView the date a profile or clinician was created",
                "\nView a profiles past, present and required donations",
                "\nUpdate a profiles attributes", "\nAdd an organ to donate",
                "\nRemove an organ to donate", "\nPrint all profiles ", "\nPrint all clinicians ",
                "\nPrint all donors ", "\nClose the app ",
                "\nAdd an organ to a profile(s) donated organs list", "\nDelete a profile or clinician",
                "Add an organ to receive", "Remove an organ from the profiles receive list",
                "import profile/clinician data", "export the current data to a JSON file",
                "make a select call to the external database",
                "Prints all users (Users are all admins and clinicians)",
                "The possible attributes are : given-names, last-names, dob, dod, gender, height, "
                + "weight, blood-type, address, region, ird",
                "The possible organs are : Liver, Kidney, Pancreas, Heart, Lung, Intestine, Cornea, "
                + "Middle Ear, Skin, Bone, Bone Marrow, Connective Tissue"};
        String[] exampleArray = {
                "create-profile {attributes (given-names, last-names, dob and ird is required)}",
                "create-clinician {attribute (name, username and password is required)}",
                "profile/clinician {attributes to search profiles/clinicians by} > view",
                "profile/clinician {attributes to search profiles/clinicians by} > date-created",
                "profile {attributes to search profiles by} > donations",
                "profile/clinician {attributes to search profiles/clinicians by} > {attributes to update}",
                "profile {attributes to search profiles by} > add-organ=\" {list of organs to donate} \"",
                "profile {attributes to search profiles by} > remove-organ=\" {list of organs to remove} \"",
                "print all profiles ", "print all clinicians", "print all donors", "quit",
                "profile {attributes to search profiles by} > donate-organ=\" {list of organs to donate} \"",
                "profile/clinician {attributes to search profiles/clinicians by} > delete",
                "profile {attributes to search profiles by} > receive-organ=\" {list of organs to donate} \"",
                "profile {attributes to search profiles by} > remove-receive-organ=\" {list of "
                        + "organs to remove} \"",
                "import {filepath}", "export", "db-read {SELECT statement}",
                "print all users"};
        if (Arrays.asList(cmdArray).contains(cmd)) {
            int position = Arrays.asList(cmdArray).indexOf(cmd);
            System.out.println(definitionArray[position]);
            if (!cmd.equals(cmdArray[21]) && !cmd.equals(cmdArray[22])) {
                System.out.println("The command is entered in this format:");
                System.out.println(exampleArray[position]);
            }
        } else {
            System.out.println("Invalid command");
        }
    }

}
