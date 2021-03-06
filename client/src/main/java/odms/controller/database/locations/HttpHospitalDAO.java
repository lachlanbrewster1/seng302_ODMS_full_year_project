package odms.controller.database.locations;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import odms.commons.model.locations.Hospital;
import odms.controller.http.Request;
import odms.controller.http.Response;
import org.sonar.api.internal.google.gson.Gson;
import org.sonar.api.internal.google.gson.JsonArray;
import org.sonar.api.internal.google.gson.JsonElement;
import org.sonar.api.internal.google.gson.JsonParser;

/**
 * Hospital DAO that communicates with the server.
 */
@Slf4j
public class HttpHospitalDAO implements HospitalDAO {

    private static final Integer PORTNUMBER = 8080;
    private static final String URL_ALL = "http://csse-s302g2:%s/api/v1/hospitals/all";
    private static final String URL = "http://csse-s302g2:%s/api/v1/hospitals";

    /**
     * Get all hospitals in database.
     *
     * @return list of hospitals
     */
    @Override
    public List<Hospital> getAll() throws SQLException {
        String url = String.format(URL_ALL, PORTNUMBER);
        Map<String, Object> queryParams = new HashMap<>();
        JsonParser parser = new JsonParser();
        Gson gson = new Gson();
        Response response = null;
        Request request = new Request(url, queryParams);
        try {
            response = request.get();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        List<Hospital> hospitals = new ArrayList<>();
        if (response != null && response.getStatus() == 200) {
            JsonArray results = parser.parse(response.getBody()).getAsJsonArray();
            for (JsonElement result : results) {
                hospitals.add(gson.fromJson(result, Hospital.class));
            }
        } else if (response != null && response.getStatus() == 500) {
            throw new SQLException(response.getBody());
        }
        return hospitals;
    }

    /**
     * Get a hospital from database.
     *
     * @param name the name of the hospital to retrieve
     * @return hospital object
     */
    @Override
    public Hospital get(String name) throws SQLException {
        String url = String.format(URL, PORTNUMBER);
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("name", name);
        Gson parser = new Gson();
        Response response = null;
        Request request = new Request(url, queryParams);
        try {
            response = request.get();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        if (response != null && response.getStatus() == 200) {
            return parser.fromJson(response.getBody(), Hospital.class);
        } else if (response != null && response.getStatus() == 500) {
            throw new SQLException(response.getBody());
        }
        return null;
    }

    /**
     * Get a hospital from the database by it's id.
     * @param id the id of the hospital to retrieve
     * @return the hospital object
     * @throws SQLException thrown when there is a server error.
     */
    @Override
    public Hospital get(int id) throws SQLException {
        String url = String.format(URL, PORTNUMBER);
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("id", id);
        Gson parser = new Gson();
        Response response = null;
        Request request = new Request(url, queryParams);
        try {
            response = request.get();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        if (response != null && response.getStatus() == 200) {
            return parser.fromJson(response.getBody(), Hospital.class);
        } else if (response != null && response.getStatus() == 500) {
            throw new SQLException(response.getBody());
        }
        return null;
    }

    /**
     * Add a hospital to the database.
     *
     * @param hospital hospital object to add
     */
    @Override
    public void add(Hospital hospital) throws SQLException {
        Gson gson = new Gson();
        String url = String.format(URL, PORTNUMBER);
        Map<String, Object> queryParams = new HashMap<>();
        Response response = null;

        String body = gson.toJson(hospital);
        Request request = new Request(url, queryParams, body);
        try {
            response = request.post();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        if (response != null && response.getStatus() == 500) {
            throw new SQLException(response.getBody());
        }
    }

    /**
     * Edit a hospitals details.
     *
     * @param hospital edited hospital object
     */
    @Override
    public void edit(Hospital hospital) throws SQLException {
        Gson gson = new Gson();
        String url = String.format(URL, PORTNUMBER);
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("id", hospital.getId());
        String body = gson.toJson(hospital);
        Request request = new Request(url, queryParams, body);
        Response response = null;

        try {
            response = request.patch();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }

        if (response != null && response.getStatus() == 500) {
            throw new SQLException(response.getBody());
        }
    }

    /**
     * Remove a hospital from the database.
     *
     * @param name the name of the hospital to remove
     */
    @Override
    public void remove(String name) throws SQLException {
        String url = String.format(URL, PORTNUMBER);
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("name", name);
        Request request = new Request(url, queryParams);
        Response response = null;

        try {
            response = request.delete();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }

        if (response != null && response.getStatus() == 500) {
            throw new SQLException(response.getBody());
        }
    }
}
