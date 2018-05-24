package odms.dao;

public interface ReadOnlyDAO {

    /**
     * Interface for the querying of databases across
     * platforms.
     * @param query
     */
    public void queryDatabase(String query);

}
