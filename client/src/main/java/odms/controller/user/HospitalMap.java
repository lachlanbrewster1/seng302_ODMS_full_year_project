package odms.controller.user;

import com.lynden.gmapsfx.javascript.object.Marker;
import com.lynden.gmapsfx.javascript.object.MarkerOptions;
import com.lynden.gmapsfx.javascript.object.InfoWindow;
import com.lynden.gmapsfx.javascript.object.InfoWindowOptions;
import com.lynden.gmapsfx.javascript.object.LatLong;
import com.lynden.gmapsfx.javascript.object.MVCArray;

import com.lynden.gmapsfx.shapes.Polyline;
import com.lynden.gmapsfx.shapes.PolylineOptions;
import odms.commons.model.enums.OrganEnum;
import odms.commons.model.locations.Hospital;
import odms.controller.database.DAOFactory;
import odms.controller.database.locations.HospitalDAO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller class for the hospital map view.
 */
public class HospitalMap {

    private static final int EARTH_RADIUS = 6371; // Radius of the earth
    private static final int ONE_KM = 1000;
    private final odms.view.user.HospitalMap view;

    /**
     * Contructor for HospitalMap controller, takes view instance as a parameter.
     * @param view view instance of HospitalMap view
     */
    public HospitalMap(odms.view.user.HospitalMap view) {
        this.view = view;
    }

    /**
     * Creates location marker and adds the relevant details to it.
     *
     * @param location the location to create a marker for
     * @return a marker object for the given location
     */
    public Marker createLocationMarker(Hospital location) {

        ArrayList<Double> latLong = new ArrayList<>();
        latLong.add(location.getLatitude());
        latLong.add(location.getLongitude());
        LatLong hospitalLocation = new LatLong(latLong.get(0), latLong.get(1));

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(hospitalLocation);
        markerOptions.title(location.getName());
        markerOptions.label(String.valueOf(location.getId()));

        if (location.getId() < 0) {
            markerOptions.label("X");
        } else {
            markerOptions.label(location.getId().toString());
        }

        return new Marker(markerOptions);
    }

    /**
     * Creates Location info window containing a locations details.
     *
     * @param location The location to create a info window for
     * @return A info window object for the given hospital containing locations details
     */
    public InfoWindow createLocationInfoWindow(Hospital location) {

        // Hospital tooltip generated and added
        InfoWindowOptions infoWindowOptions = new InfoWindowOptions();
        String locationInfo = location.getName() + ". \n";

        if (location.getId() < 0) {
            locationInfo += "Location: (" +
                    Double.valueOf(view.getDecimalFormat().format(location.getLatitude())) + ", " +
                    Double.valueOf(view.getDecimalFormat().format(location.getLongitude())) + ")";
        } else {

            if (location.getAddress() != null) {
                locationInfo += "Address: " + location.getAddress() + ". \n";
            }

            List<String> organPrograms = new ArrayList<>();
            int i = 0;
            for (OrganEnum organ : OrganEnum.values()) {
                if (location.getPrograms().get(i)) {
                    organPrograms.add(organ.getNamePlain());
                }
                i++;
            }

            if (location.getPrograms() != null) {
                locationInfo += "Services offered: " + organPrograms + ".";
            }

        }

        infoWindowOptions.content(locationInfo);

        return new InfoWindow(infoWindowOptions);
    }

    /**
     * Calculates the distance between two lat long coordinates, using the Haversine method.
     *
     * @param lat1 Latitude of first coordinate
     * @param lon1 Longitude of first coordinate
     * @param lat2 Latitude of second coordinate
     * @param lon2 Longitude of second coordinate
     * @return Distance between the two coordinates in km
     */
    public double calcDistanceHaversine(double lat1, double lon1, double lat2, double lon2) {

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distanceInMeters = EARTH_RADIUS * c * ONE_KM; // convert to meters

        distanceInMeters = Math.pow(distanceInMeters, 2);

        return Math.sqrt(distanceInMeters) / ONE_KM;
    }

    /**
     * Creates a line object that can be added to the map, the line goes from the given location 1
     * to the given location 2 represents a route between the locations.
     *
     * @param originLat latitude the route starts from
     * @param originLong latitude the route starts from
     * @param destinationLat latitude the route ends at
     * @param destinationLong longitude the route ends at
     * @return A Polyline object that can be added to a map to represent a line
     */
    public Polyline createHelicopterRoute(Double originLat, Double originLong,
            Double destinationLat, Double destinationLong) {

        LatLong originLatLong = new LatLong(originLat, originLong);
        LatLong destinationLatLong = new LatLong(destinationLat, destinationLong);
        LatLong[] coordinatesList = new LatLong[]{originLatLong, destinationLatLong};

        MVCArray pointsOnMap = new MVCArray(coordinatesList);
        PolylineOptions polyOpts = new PolylineOptions().path(pointsOnMap)
                .strokeColor("blue").strokeWeight(2);

        return new Polyline(polyOpts);
    }

    /**
     * Gets all hospitals from the database.
     * @return list of hospitals
     */
    public List<Hospital> getHospitals() {
        HospitalDAO hospitalDAO = DAOFactory.getHospitalDAO();
        try {
            return hospitalDAO.getAll();
        } catch (SQLException e) {
            return new ArrayList<>();
        }
    }

    /**
     * Deletes a hospital from the database.
     * @param hospital hospital to delete
     * @return bool, true if hospital was deleted
     */
    public Boolean deleteHospital(Hospital hospital) {
        HospitalDAO hospitalDAO = DAOFactory.getHospitalDAO();
        try {
            hospitalDAO.remove(hospital.getName());
        } catch (SQLException e) {
            return false;
        }
        return true;
    }
}
