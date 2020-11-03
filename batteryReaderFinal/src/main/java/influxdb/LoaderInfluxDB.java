package influxdb;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.InfluxDBIOException;
import org.influxdb.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class LoaderInfluxDB {

    private InfluxDB connection;
    private final static Logger logger = LoggerFactory.getLogger(LoaderInfluxDB.class);


    public LoaderInfluxDB(String databaseURL, String userName, String password)  {
        connection (databaseURL,userName,password);
    }

    public void connection(String databaseURL, String userName, String password){
        // Connect to database
        connection = InfluxDBFactory.connect(databaseURL, userName, password);
        // Ping to service
        pingServer(connection);
    }

    private boolean pingServer(InfluxDB influxDB) {
        try {
            // Ping and check for version string
            Pong response = influxDB.ping();
            if (response.getVersion().equalsIgnoreCase("unknown")) {
                logger.error("Error pinging server.");
                return false;
            } else {
                logger.info("Database version: {}", response.getVersion());
                return true;
            }
        } catch (InfluxDBIOException idbo) {
            logger.warn("Exception while pinging database: "+ idbo);
            return false;
        }
    }

    public void loadValuesInfluxDB(String dataBaseName, Map<String, Map<String, String>> data)  {

        List<String> dataK = new ArrayList<String>(data.keySet());

        if(dataK.size()>0){
            // If database doesn t exist
            if (!connection.databaseExists(dataBaseName)){
                connection.createDatabase(dataBaseName);
            }
            logger.info("-------------------------------Data to load : "+ dataK.size()+" Total Data : "+data.size());

            BatchPoints batchPoints = BatchPoints
                    .database(dataBaseName)
                    .tag("async", "true")
                    .build();

            for (int i=0; i<dataK.size();i++) {
                final Map<String,Object> dataO=new HashMap<>();
                dataO.putAll(data.get(dataK.get(i)));
                Point point = Point.measurement(dataK.get(i).substring(0,dataK.get(i).indexOf("-")))
                        .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                        .tag("uniq", String.valueOf(i))
                        .fields(dataO)
                        .build();
                data.remove(dataK.get(i));
                batchPoints.point(point);
            }
            connection.write(batchPoints);
        }
        //Test
        /*Query query = new Query("SELECT * FROM VI", "influxdb");
        QueryResult result = connection.query(query);*/
        logger.info("-------------------------------Loaded Data : "+dataK.size()+" Total Data : "+data.size());
        connection.close();
    }
}
