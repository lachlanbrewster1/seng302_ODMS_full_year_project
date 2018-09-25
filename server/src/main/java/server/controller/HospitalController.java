package server.controller;

import lombok.extern.slf4j.Slf4j;
import odms.commons.model.locations.Hospital;
import org.sonar.api.internal.google.gson.Gson;
import org.sonar.api.internal.google.gson.JsonObject;
import org.sonar.api.internal.google.gson.JsonParser;
import server.model.database.DAOFactory;
import server.model.database.locations.HospitalDAO;
import spark.Request;
import spark.Response;

import java.sql.SQLException;
import java.util.List;

@Slf4j
public class HospitalController {

    /**
     * Gets a list of all the hospitals in the database.
     * @param req The request sent to the endpoint
     * @param res The response sent back
     * @return The response body as a list of Json object of countries
     */
    public static String getAll(Request req, Response res) {
        List<Hospital> hospitals;

        try {
            HospitalDAO hospitalDAO = DAOFactory.getHospitalDAO();
            hospitals = hospitalDAO.getAll();
        } catch (SQLException e) {
            res.status(500);
            return "Database Error";
        }

        Gson gson = new Gson();
        String responseBody = gson.toJson(hospitals);

        res.type("application/json");
        res.status(200);

        return responseBody;
    }

    /**
     * Gets a single hospital from database.
     *
     * @param req sent to the endpoint.
     * @param res sent back.
     * @return the response body.
     */
    public static String get(Request req, Response res) {
        HospitalDAO database = DAOFactory.getHospitalDAO();
        Hospital hospital;
        try {
            hospital = database.get(req.queryParams("name"));
        } catch (SQLException e) {
            res.status(500);
            return "Database Error";
        }

        Gson gson = new Gson();
        String responseBody = gson.toJson(hospital);

        res.type("application/json");
        res.status(200);

        return responseBody;
    }

    /**
     * Updates a countries valid field to true or false.
     * @param req The request sent to the endpoint
     * @param res The response sent back
     * @return The response body
     */
    public static String edit(Request req, Response res) {
        HospitalDAO hospitalDAO = DAOFactory.getHospitalDAO();
        JsonParser parser = new JsonParser();
        Integer id;
        String name;
        String address;
        Double lat;
        Double lon;
        Hospital hospital;

        try {
            JsonObject body = parser.parse(req.body()).getAsJsonObject();
            id = body.get("id").getAsInt();
            name = body.get("name").getAsString();
            address = body.get("address").getAsString();
            lat = body.get("latitude").getAsDouble();
            lon = body.get("longitude").getAsDouble();

            hospital = new Hospital(name, lat, lon, address, null, id);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            res.status(400);
            return "Bad Request";
        }
        try {
            hospitalDAO.edit(hospital);
            res.status(200);
            return "Hospital Updated";
        } catch (SQLException e) {
            res.status(500);
            return "Database Error";
        }
    }

    /**
     * Removes a hospital from the database.
     *
     * @param req sent to the endpoint.
     * @param res sent back.
     * @return the response body.
     */
    public static String delete(Request req, Response res) {
        HospitalDAO database = DAOFactory.getHospitalDAO();
        String name;

        try {
            name = req.queryParams("name");
        } catch (Exception e) {
            res.status(400);
            return "Bad Request";
        }
        try {

            database.remove(name);
        } catch (SQLException e) {
            res.status(500);
            return "Database Error";
        }

        res.status(200);
        return "Hospital Deleted";
    }

    /**
     * Adds a new hospital to he database.
     *
     * @param req sent to the endpoint.
     * @param res sent back.
     * @return the response body.
     */
    public static String create(Request req, Response res) {
        Gson gson = new Gson();
        HospitalDAO database = DAOFactory.getHospitalDAO();
        Hospital newHospital;

        try {
            newHospital = gson.fromJson(req.body(), Hospital.class);
        } catch (IllegalArgumentException e) {
            res.status(403);
            return "Forbidden";
        }

        if (newHospital != null) {
            try {
                database.add(newHospital);
            } catch (SQLException e) {
                res.status(500);
                return "Database Error";
            }
        }
        res.status(201);
        return "Hospital Created";
    }
}
