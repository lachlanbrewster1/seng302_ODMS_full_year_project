package server.controller;

import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;
import odms.commons.model.enums.OrganEnum;
import odms.commons.model.enums.UserType;
import odms.commons.model.profile.Profile;
import odms.commons.model.user.UserNotFoundException;
import org.sonar.api.internal.google.gson.Gson;
import server.model.database.DAOFactory;
import server.model.database.profile.ProfileDAO;
import server.model.enums.DataTypeEnum;
import server.model.enums.KeyEnum;
import server.model.enums.ResponseMsgEnum;
import spark.Request;
import spark.Response;

/**
 * The profile server controller.
 */
@Slf4j
public class ProfileController {

    private static final String KEY_ORGANS = "organs";
    private static final String KEY_SEARCH = "searchString";

    /**
     * Prevent instantiation of static class.
     */
    private ProfileController() {
        throw new UnsupportedOperationException();
    }

    /**
     * Gets all profiles stored.
     * @param req sent to the endpoint.
     * @param res sent back.
     * @return the response body, a list of all profiles.
     */
    public static String getAll(Request req, Response res) {
        return getAllProfiles(req, res);
    }

    /**
     * Gets all receiving profiles (possibly with search criteria).
     * @param req received.
     * @return json string of profiles.
     */
    public static String getReceiving(Request req, Response res) {
        ProfileDAO database = DAOFactory.getProfileDao();
        Gson gson = new Gson();
        String profiles;
        try {
            if (req.queryParams(KEY_SEARCH) != null) {
                String searchString = req.queryParams(KEY_SEARCH);
                List<Entry<Profile, OrganEnum>> result = database.searchReceiving(searchString);
                profiles = gson.toJson(result);
            } else if (req.queryParams(KEY_ORGANS) != null) {
                String organs = req.queryParams(KEY_ORGANS);
                String bloodTypes = req.queryParams("bloodTypes");
                Integer lowerAgeRange = Integer.valueOf(req.queryParams("lowerAgeRange"));
                Integer upperAgeRange = Integer.valueOf(req.queryParams("upperAgeRange"));
                List<Profile> result = database.getOrganReceivers(organs, bloodTypes,
                        lowerAgeRange, upperAgeRange);
                profiles = gson.toJson(result);
            } else {
                profiles = gson.toJson(database.getAllReceiving());
            }
        } catch (NumberFormatException e) {
            res.status(400);
            return ResponseMsgEnum.BAD_REQUEST.toString();
        } catch (Exception e) {
            res.status(500);
            return ResponseMsgEnum.INTERNAL_SERVER_ERROR.toString();
        }
        res.type(DataTypeEnum.JSON.toString());
        res.status(200);

        return profiles;
    }

    /**
     * Gets all dead profiles stored, possibly with search criteria.
     * @param req sent to the endpoint.
     * @param res sent back.
     * @return the response body, a list of all profiles.
     */
    public static String getDead(Request req, Response res) {
        ProfileDAO database = DAOFactory.getProfileDao();
        Gson gson = new Gson();
        String profiles;
        try {
            if (req.queryParams(KEY_SEARCH) != null) {
                String searchString = req.queryParams(KEY_SEARCH);
                List<Profile> result = database.getDeadFiltered(searchString);
                profiles = gson.toJson(result);
            } else {
                profiles = gson.toJson(database.getDead());
            }

        } catch (SQLException e) {
            res.status(500);
            return e.getMessage();
        }
        res.type(DataTypeEnum.JSON.toString());
        res.status(200);

        return profiles;
    }

    /**
     * Gets all profiles (possibly with search criteria).
     * @param req received.
     * @return json string of profiles.
     */
    private static String getAllProfiles(Request req, Response res) {
        ProfileDAO database = DAOFactory.getProfileDao();
        Gson gson = new Gson();
        String profiles;

        if (req.queryParams(KEY_SEARCH) != null) {
            String searchString = null;
            int ageSearchInt = 0;
            int ageRangeSearchInt = 0;
            String region = null;
            String gender = null;
            String type = null;
            try {
                searchString = req.queryParams(KEY_SEARCH);
                ageSearchInt = Integer.parseInt(req.queryParams("ageSearchInt"));
                ageRangeSearchInt = Integer.parseInt(req.queryParams("ageRangeSearchInt"));
                region = req.queryParams("region");
                gender = req.queryParams("gender") != null ? req.queryParams("gender") : "any";
                type = req.queryParams("type");
            } catch (NumberFormatException e) {
                ageSearchInt = 0;
                ageRangeSearchInt = -999;
            }
            Set<OrganEnum> organs = new HashSet<>();
            List<String> organArray = gson.fromJson(req.queryParams(KEY_ORGANS), List.class) != null ?
                    gson.fromJson(req.queryParams("organs"), List.class) : new ArrayList<>();
            for (String organ : organArray) {
                organs.add(OrganEnum.valueOf(organ));
            }
            List<Profile> result = database.search(searchString, ageSearchInt,
                    ageRangeSearchInt, region, gender, type, organs);
            profiles = gson.toJson(result);
        } else {
            profiles = gson.toJson(database.getAll());
        }

        res.type(DataTypeEnum.JSON.toString());
        res.status(200);
        return profiles;
    }

    /**
     * Gets a single profile from storage.
     * @param req sent to the endpoint.
     * @param res sent back.
     * @return the response body.
     */
    public static String get(Request req, Response res) {
        ProfileDAO database = DAOFactory.getProfileDao();
        Profile profile;

        try {
            if (req.queryParams(KeyEnum.ID.toString()) != null) {
                profile = database.get(Integer.valueOf(req.queryParams(KeyEnum.ID.toString())));
            } else {
                profile = database.get(req.queryParams("username"));
            }
            if (profile == null) {
                throw new IllegalArgumentException("Required fields are missing.");
            }

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            res.status(500);
            return ResponseMsgEnum.INTERNAL_SERVER_ERROR.toString();
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage(), e);
            res.status(400);
            return ResponseMsgEnum.BAD_REQUEST.toString();
        }
        String responseBody = new Gson().toJson(profile);

        res.type(DataTypeEnum.JSON.toString());
        res.status(200);

        return responseBody;
    }

    /**
     * Creates and stores a new profile.
     * @param req sent to the endpoint.
     * @param res sent back.
     * @return the response body.
     */
    public static String create(Request req, Response res) {
        Gson gson = new Gson();
        ProfileDAO database = DAOFactory.getProfileDao();
        Profile newProfile;

        try {
            newProfile = gson.fromJson(req.body(), Profile.class);
            if (newProfile == null) {
                throw new IllegalArgumentException("Profile body missing.");
            }
        } catch (IllegalArgumentException e) {
            res.status(400);
            return ResponseMsgEnum.BAD_REQUEST.toString();
        } catch (Exception e) {
            res.status(400);
            return ResponseMsgEnum.BAD_REQUEST.toString();
        }

        if (newProfile != null) {
            try {
                if (database.isUniqueNHI(newProfile.getNhi()) == 0
                        && !database.isUniqueUsername(newProfile.getNhi())) {
                    database.add(newProfile);
                    Profile profile = database.get(newProfile.getNhi());
                    Map<String, Integer> body = Middleware.authenticate(profile.getId(), UserType.PROFILE);
                    res.type(DataTypeEnum.JSON.toString());
                    res.status(200);
                    return gson.toJson(body);
                } else {
                    res.status(400);
                    return ResponseMsgEnum.BAD_REQUEST.toString();
                }
            } catch (SQLException e) {
                res.status(500);
                return ResponseMsgEnum.INTERNAL_SERVER_ERROR.toString();
            }
        }

        res.status(201);
        return "Profile Created";
    }

    /**
     * Edits a stored profile.
     * @param req sent to the endpoint.
     * @param res sent back.
     * @return the response body.
     */
    public static String edit(Request req, Response res) {
        ProfileDAO database = DAOFactory.getProfileDao();
        Profile profile;

        try {
            profile = new Gson().fromJson(req.body(), Profile.class);
            profile.setId(Integer.valueOf(req.params(KeyEnum.ID.toString())));
        } catch (Exception e) {
            res.status(400);
            return ResponseMsgEnum.BAD_REQUEST.toString();
        }

        try {
            database.update(profile);
        } catch (SQLException e) {
            res.status(500);
            return ResponseMsgEnum.INTERNAL_SERVER_ERROR.toString();
        }

        res.status(200);
        return "Profile Updated";
    }

    /**
     * Deletes a profile from storage.
     * @param req sent to the endpoint.
     * @param res sent back.
     * @return the response body.
     */
    public static String delete(Request req, Response res) {
        ProfileDAO database = DAOFactory.getProfileDao();
        String query = req.params(KeyEnum.ID.toString());
        Profile profile;

        try {
            if (isValidNHI(query)) {
                profile = new Profile(query);
            } else {
                profile = new Profile(Integer.parseInt(query));
            }
        } catch (NumberFormatException e) {
            res.status(400);
            return ResponseMsgEnum.BAD_REQUEST.toString();
        }

        try {
            database.remove(profile);
        } catch (SQLException e) {
            res.status(500);
            return ResponseMsgEnum.INTERNAL_SERVER_ERROR.toString();
        }

        res.status(200);
        return "Profile Deleted";
    }

    /**
     * Gets a count of all stored profiles.
     * @param req sent to the endpoint.
     * @param res sent back.
     * @return the response body.
     */
    public static String count(Request req, Response res) {
        ProfileDAO database = DAOFactory.getProfileDao();
        int count;

        try {
            count = database.size();
        } catch (SQLException e) {
            res.status(500);
            return ResponseMsgEnum.INTERNAL_SERVER_ERROR.toString();
        }

        Gson gson = new Gson();
        String responseBody = gson.toJson(count);

        res.type(DataTypeEnum.JSON.toString());
        res.status(200);

        return responseBody;
    }

    /**
     * Checks that a profile has a password.
     * @param req the request fields.
     * @param res the response from the server.
     * @return The response body.
     */
    public static String hasPassword(Request req, Response res) {
        ProfileDAO database = DAOFactory.getProfileDao();
        Boolean hasPassword;
        try {
            if (req.queryParams("nhi") != null) {
                hasPassword = database.hasPassword(req.queryParams("nhi"));
            } else {
                throw new IllegalArgumentException("Required fields missing.");
            }
        } catch (SQLException e) {
            res.status(500);
            return ResponseMsgEnum.INTERNAL_SERVER_ERROR.toString();
        } catch (IllegalArgumentException e) {
            res.status(400);
            return ResponseMsgEnum.BAD_REQUEST.toString();
        }

        res.status(200);
        return hasPassword.toString();
    }

    /**
     * Checks the credentials of a profile logging in,
     * @param request request containg password and username.
     * @param response response from the server.
     * @return String displaying success of validation.
     */
    public static String checkCredentials(Request request, Response response) {
        ProfileDAO database = DAOFactory.getProfileDao();
        Gson gson = new Gson();
        Boolean valid;

        String username = request.queryParams("username");
        String password = request.queryParams("password");
        try {
            if (username == null || password == null) {
                throw new IllegalArgumentException("Missing required fields.");
            }
            valid = database.checkCredentials(username, password);
        } catch (UserNotFoundException e) {
            response.status(404);
            return "Profile not found";
        } catch (IllegalArgumentException e) {
            response.status(400);
            return ResponseMsgEnum.BAD_REQUEST.toString();
        }

        if (valid) {
            try {
                Profile profile = database.get(username);
                Map<String, Integer> body = Middleware.authenticate(profile.getId(), UserType.PROFILE);
                response.type(DataTypeEnum.JSON.toString());
                response.status(200);
                return gson.toJson(body);
            } catch (SQLException e) {
                response.status(500);
                return e.getMessage();
            }
        } else {
            response.status(401);
            return "Unauthorized";
        }
    }

    /**
     * Saves the profiles password.
     * @param request request being sent with url and password.
     * @param response the server response.
     * @return String confirming success.
     */
    public static String savePassword(Request request, Response response) {
        ProfileDAO profileDAO = DAOFactory.getProfileDao();
        Boolean valid;

        String username = request.queryParams("username");
        String password = request.queryParams("password");
        try {
            if (username == null || password == null) {
                throw new IllegalArgumentException("Missing required fields.");
            }
            valid = profileDAO.savePassword(username, password);
        } catch (UserNotFoundException e) {
            log.error(e.getMessage(), e);
            response.status(404);
            return "Profile not found";
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage(), e);
            response.status(400);
            return ResponseMsgEnum.BAD_REQUEST.toString();
        }
        if (valid) {
            response.status(200);
            return ResponseMsgEnum.INTERNAL_SERVER_ERROR.toString();
        } else {
            response.status(404);
            return "Profile not found";
        }
    }

    /**
     * Checks if the nhi is valid (3 characters (no O or I) followed by 4 numbers).
     *
     * @param nhi the nhi to check.
     * @return true if valid and false if not valid.
     */
    private static boolean isValidNHI(String nhi) {
        String pattern = "^[A-HJ-NP-Z]{3}\\d{4}$";
        Pattern r = Pattern.compile(pattern);

        Matcher m = r.matcher(nhi);
        return m.find();
    }

    /**
     * Updates the blood donation points and last donation datetime for a profile.
     * @param request from the client.
     * @param response to the client.
     * @return the response from the server.
     */
    public static String updateBloodDonation(Request request, Response response) {
        ProfileDAO profileDAO = DAOFactory.getProfileDao();

        int points = Integer.valueOf(request.queryParams("points"));
        int id = Integer.valueOf(request.params(KeyEnum.ID.toString()));

        try {
            profileDAO.updateBloodDonation(id, points);
        } catch (SQLException e) {
            response.status(500);
            return ResponseMsgEnum.INTERNAL_SERVER_ERROR.toString();
        }

        response.status(200);
        return "Points Updated";
    }
}
