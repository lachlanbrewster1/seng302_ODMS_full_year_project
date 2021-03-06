package server.controller;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import odms.commons.model.enums.OrganEnum;
import odms.commons.model.profile.ExpiredOrgan;
import odms.commons.model.profile.OrganConflictException;
import odms.commons.model.profile.Profile;
import org.sonar.api.internal.google.gson.Gson;
import org.sonar.api.internal.google.gson.JsonObject;
import org.sonar.api.internal.google.gson.JsonParser;
import server.model.database.DAOFactory;
import server.model.database.organ.OrganDAO;
import server.model.enums.DataTypeEnum;
import server.model.enums.KeyEnum;
import server.model.enums.ResponseMsgEnum;
import spark.Request;
import spark.Response;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
public class OrganController {
    private static final String DONATED = "donated";
    private static final String DONATING = "donating";
    private static final String RECEIVED = "received";
    private static final String REQUIRED = "required";

    /**
     * Prevent instantiation of static class.
     */
    private OrganController() {
        throw new UnsupportedOperationException();
    }

    /**
     * Gets a list of all organs for a profile.
     * @param req sent to the endpoint.
     * @param res sent back.
     * @return the response body.
     */
    public static String getAll(Request req, Response res) {
        int profileId;

        try {
            profileId = Integer.valueOf(req.params(KeyEnum.ID.toString()));
        } catch (IllegalArgumentException e) {
            res.status(400);
            return ResponseMsgEnum.BAD_REQUEST.toString();
        }
        try {
            res.type(DataTypeEnum.JSON.toString());
            res.status(200);
            return getOrgans(new Profile(profileId), req, res);
        } catch (Exception e) {
            res.status(500);
            return ResponseMsgEnum.INTERNAL_SERVER_ERROR.toString();
        }
    }

    /**
     * Adds an organ to a profile in persistent storage.
     * @param req sent to the endpoint.
     * @param res sent back.
     * @return the response body.
     */
    public static String add(Request req, Response res) {
        int profileId;
        JsonObject body;
        JsonParser parser = new JsonParser();

        try {
            profileId = Integer.valueOf(req.params(KeyEnum.ID.toString()));
            body = parser.parse(req.body()).getAsJsonObject();
        } catch (Exception e) {
            res.status(400);
            return ResponseMsgEnum.BAD_REQUEST.toString();
        }

        try {
            addOrgan(new Profile(profileId), body);
        } catch (Exception e) {
            res.status(500);
            return e.getMessage();
        }

        res.status(201);
        return "Organ added";
    }

    /**
     * Removes an organ from a profile in persistent storage.
     * @param req sent to the endpoint.
     * @param res sent back.
     * @return the response body.
     */
    public static String delete(Request req, Response res) {
        int profileId;
        String organ;
        try {
            profileId = Integer.valueOf(req.params(KeyEnum.ID.toString()));
            organ = String.valueOf(req.queryParams(KeyEnum.NAME.toString()));
        } catch (Exception e) {
            res.status(500);
            return ResponseMsgEnum.BAD_REQUEST.toString();
        }

        try {
            removeOrgan(new Profile(profileId), organ, req);
        } catch (Exception e) {
            res.status(500);
            return e.getMessage();
        }

        res.status(200);
        return "Organ removed";
    }

    /**
     * Gets the organs from the database bases on organ usage.
     * @param profile to get the organs for.
     * @param req that was received.
     * @return the set of organs based on the criteria.
     */
    private static String getOrgans(Profile profile, Request req, Response res) {
        OrganDAO database = DAOFactory.getOrganDao();
        Gson gson = new Gson();

        if (req.queryParams(DONATED) != null && Boolean.valueOf(req.queryParams(DONATED))) {
            return gson.toJson(database.getDonations(profile));
        }
        if (req.queryParams(DONATING) != null && Boolean.valueOf(req.queryParams(DONATING))) {
            return gson.toJson(database.getDonating(profile));
        }
        if (req.queryParams(RECEIVED) != null && Boolean.valueOf(req.queryParams(RECEIVED))) {
            return gson.toJson(database.getReceived(profile));
        }
        if (req.queryParams(REQUIRED) != null && Boolean.valueOf(req.queryParams(REQUIRED))) {
            return gson.toJson(database.getRequired(profile));
        }
        res.status(400);
        return ResponseMsgEnum.BAD_REQUEST.toString();
    }

    /**
     * Add an organ to a profile in persistent storage.
     * @param profile to add the organ to.
     * @param body that was received.
     * @throws OrganConflictException error.
     */
    private static void addOrgan(Profile profile, JsonObject body) throws OrganConflictException {
        OrganEnum organEnum = OrganEnum.valueOf(body.get(
                KeyEnum.NAME.toString()
        ).getAsString());
        OrganDAO database = DAOFactory.getOrganDao();

        if (body.keySet().contains(DONATED)) {
            database.addDonation(profile, organEnum);
        }
        if (body.keySet().contains(DONATING)) {
            database.addDonating(profile, organEnum);
        }
        if (body.keySet().contains(REQUIRED)) {
            database.addRequired(profile, organEnum);
        }
        if (body.keySet().contains(RECEIVED)) {
            database.addReceived(profile, organEnum);
        }
    }

    /**
     * Removes an organ from a profile in persistent storage.
     * @param profile to add the organ to.
     * @param organ to add.
     * @param req that was received.
     */
    private static void removeOrgan(Profile profile, String organ, Request req) {
        OrganEnum organEnum = OrganEnum.valueOf(organ);
        OrganDAO database = DAOFactory.getOrganDao();

        if (req.queryParams(DONATED) != null && Boolean.valueOf(req.queryParams(DONATED))) {
            database.removeDonation(profile, organEnum);
        }
        if (req.queryParams(DONATING) != null && Boolean.valueOf(req.queryParams(DONATING))) {
            database.removeDonating(profile, organEnum);
        }
        if (req.queryParams(REQUIRED) != null && Boolean.valueOf(req.queryParams(REQUIRED))) {
            database.removeRequired(profile, organEnum);
        }
        if (req.queryParams(RECEIVED) != null && Boolean.valueOf(req.queryParams(RECEIVED))) {
            database.removeReceived(profile, organEnum);
        }
    }

    /**
     * Handles the endpoint to get expired organs for a profile.
     * @param req from the client.
     * @param res sent to the client.
     * @return the response from the server.
     */
    public static String getExpired(Request req, Response res) {
        OrganDAO database = DAOFactory.getOrganDao();
        int profileId;
        List<ExpiredOrgan> organs;

        try {
            if (req.params(KeyEnum.ID.toString()) == null) {
                throw new IllegalArgumentException("Missing required fields.");
            }
            profileId = Integer.parseInt(req.params(KeyEnum.ID.toString()));
        } catch (IllegalArgumentException e) {
            res.status(400);
            return ResponseMsgEnum.BAD_REQUEST.toString();
        }

        try {
            organs = database.getExpired(new Profile(profileId));
        } catch (SQLException e) {
            res.status(500);
            return ResponseMsgEnum.INTERNAL_SERVER_ERROR.toString();
        }

        Gson gson = new Gson();
        String responseBody = gson.toJson(organs);

        res.type(DataTypeEnum.JSON.toString());
        res.status(200);

        return responseBody;
    }


    /**
     * Handles the endpoint to set organs to expired for a profile.
     * @param req from the client.
     * @param res sent to the client.
     * @return the response sent from the server.
     */
    public static String setExpired(Request req, Response res) {
        OrganDAO database = DAOFactory.getOrganDao();
        int profileId;
        OrganEnum organ;

        try {
            profileId = Integer.parseInt(req.params(KeyEnum.ID.toString()));
            organ = OrganEnum.valueOf(req.queryParams("organ"));

            if (Integer.valueOf(req.queryParams("expired")) == 1) {
                String note = req.queryParams("note");
                int userId = Integer.parseInt(req.queryParams("userId"));
                database.setExpired(new Profile(profileId), organ, 1, note, userId);
            } else {
                database.revertExpired(profileId, organ);
            }

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            res.status(500);
            return ResponseMsgEnum.INTERNAL_SERVER_ERROR.toString();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            res.status(400);
            return ResponseMsgEnum.BAD_REQUEST.toString();
        }

        res.status(200);
        return "Expiry set";
    }
}
