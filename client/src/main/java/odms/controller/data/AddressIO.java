package odms.controller.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import lombok.extern.slf4j.Slf4j;

/**
 * Validates addresses against the google places api.
 */
@Slf4j
public final class AddressIO {

    private static final String API_URL = "https://maps.googleapis.com/maps/api/";
    private static String key;

    /**
     * Error to show that you can make an instance of AddressIO.
     */
    private AddressIO() {
        throw new UnsupportedOperationException();
    }

    /**
     * Checks that the country is valid.
     *
     * @param address Address to check.
     * @param country Country to check.
     * @return True of the country is valid.
     */
    public static boolean checkValidCountry(String address, String country) {
        try {
            if (address != null) {
                address = address.toLowerCase();
                address = address.replace("road", "rd");
                address = address.replace("street", "st");
                String jsonString = getGeocodeLocation(address, country.replace(" ", "+"))
                        .toString();
                return jsonString.toLowerCase().contains(address.toLowerCase());
            } else {
                return false;
            }
        } catch (IOException e) {
            log.error("Invalid Address");
            log.error(e.getMessage(), e);
            return false;
        }
    }

    /**
     * Checks that a region is within a country.
     *
     * @param address Address to check.
     * @param region Region to check.
     * @param country Country to check against.
     * @return True if the region is within the country.
     */
    public static boolean checkValidRegion(String address, String region, String country) {
        try {
            if (region != null && country != null) {
                JsonObject jsonString = getGeocodeLocation(region, country.replace(" ", "+"));
                return jsonString.getAsJsonArray("results").get(0).getAsJsonObject().getAsJsonArray(
                        "address_components").get(0).getAsJsonObject().toString().contains(region);
            } else {
                return false;
            }
        } catch (IOException | IndexOutOfBoundsException e) {
            return false;
        }
    }

    /**
     * Checks that the address is valid in that region within a country.
     *
     * @param address Address to check.
     * @param city City to check.
     * @param country Country to check against.
     * @return True if the address is in the region.
     */
    public static boolean checkValidCity(String address, String city, String country) {
        try {
            if (address != null) {
                String[] components = address.split(",");
                JsonObject jsonString = getGeocodeLocation(address.replace(",", "+"), country.replace(" ", "+"));
                return (jsonString.getAsJsonArray("results").get(0).getAsJsonObject().getAsJsonArray(
                        "address_components").get(0).getAsJsonObject().toString()
                        .contains("locality") && jsonString.getAsJsonArray("results").get(0).getAsJsonObject().getAsJsonArray(
                        "address_components").get(0).getAsJsonObject().toString()
                        .contains(city) && jsonString.getAsJsonArray("results").get(0).getAsJsonObject().getAsJsonArray(
                        "address_components").get(1).getAsJsonObject().toString()
                        .contains(components[1]));
            } else {
                return false;
            }
        } catch (IOException | IndexOutOfBoundsException e) {
            return false;
        }
    }

    /**
     * Gets the geocode location for the google maps places api.
     *
     * @param address Address of the location.
     * @param country Country of the location.
     * @return JsonObject containing the api response.
     * @throws IOException Thrown when a request cannot be made.
     */
    private static JsonObject getGeocodeLocation(String address, String country)
            throws IOException {
        key = "AIzaSyCfq6coJWIFGQusltLJCA8tZMt9cjouzLw";

        String query = API_URL +
                "geocode/json?address=" +
                address.replace(" ", "+") +
                "&components=country:" + country + "&key=" + key;
        URL url = new URL(query);
        URLConnection request = url.openConnection();
        request.connect();
        JsonParser jp = new JsonParser();
        JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent()));
        JsonObject rootobj = root.getAsJsonObject();
        return rootobj;
    }
}
