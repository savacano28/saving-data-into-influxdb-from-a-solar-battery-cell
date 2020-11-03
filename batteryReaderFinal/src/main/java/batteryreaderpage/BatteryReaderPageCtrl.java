package batteryreaderpage;

import influxdb.LoaderInfluxDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class BatteryReaderPageCtrl {

    private final static Logger logger = LoggerFactory.getLogger(BatteryReaderPageCtrl.class);
    private ReaderValuesPage readerValuesPage;
    private LoaderInfluxDB loaderInfluxDB;
    private Map<String, Map<String,String>> deltas = new LinkedHashMap<>();
    private Map<String, List<String>> lastValues = new LinkedHashMap<>();
    private Map<String, Map<String,String>> pagesToInfluxdb = new LinkedHashMap<>();
    private Map<String, String> data = new LinkedHashMap<>();
    private String [] pages;
    private String [] samplingtimes;
    private String [] influxDb;
    private String pathServeur;
    final private String INNER_FILE= "_inner/";
    final private String T_FILE= ".config.properties";
    private static long START_TIME;
    private ScheduledExecutorService scheduledExecutorService;

    public BatteryReaderPageCtrl() throws IOException {
        getGlobalProperties();
        getPageProperties();
        readerValuesPage = new ReaderValuesPage();
        loaderInfluxDB = new LoaderInfluxDB(influxDb[0],influxDb[1],influxDb[2]);

    }

    public void getGlobalProperties() throws IOException {
        Properties prop = new Properties();
        String propFilePath = "global.config.properties";
        FileInputStream file;
        file=new FileInputStream(propFilePath);
        prop.load(file);

        /*inputStream = getClass().getClassLoader().getResourceAsStream(propFilePath);
        if (inputStream != null) {
            prop.load(inputStream);
        } else {
            throw new FileNotFoundException("property file '" + propFilePath + "' not found in the classpath");
        }*/

        pages = prop.getProperty("pages").split(",");
        samplingtimes=prop.getProperty("samplingtimes").split(",");
        influxDb=prop.getProperty("influxdb").split(",");
        pathServeur=prop.getProperty("serveur");
        file.close();
    }

    public void getPageProperties() throws IOException {
        for(String s : pages) {
            deltas.put(s,getDeltaPages(s));
            lastValues.put(s,new ArrayList<>(Collections.nCopies(60, "0")));
        }

    }

    public Map<String, String> getDeltaPages(String s) throws IOException {
        OrderedProperties prop = new OrderedProperties();
        Map<String, String> delta= new LinkedHashMap<String, String >();
        FileInputStream file;
        file=new FileInputStream(s.toLowerCase().replace("_","").concat(T_FILE));
        prop.load(file);

       /* inputStream = getClass().getClassLoader().getResourceAsStream(s.toLowerCase().replace("_","").concat(T_FILE));
        if (inputStream != null) {
            prop.load(inputStream);
        } else {
            throw new FileNotFoundException("property file of page '" + s + "' not found in the classpath");
        }*/

        Enumeration <?> e = prop.propertyNames();
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            delta.put(key, prop.getProperty(key));
        }
         file.close();

        return delta;
    }


    public void reader() throws IOException, InterruptedException {

        scheduledExecutorService = Executors.newScheduledThreadPool(pages.length + 1);
        START_TIME = System.currentTimeMillis();

        int i=0;
        for(String s : pages) {
            scheduledExecutorService.scheduleAtFixedRate(getData(s), i, Long.parseLong(samplingtimes[i]), TimeUnit.SECONDS);
            i++;
        }

        scheduledExecutorService.scheduleAtFixedRate(loadData(), 0, Long.parseLong(samplingtimes[samplingtimes.length-1]), TimeUnit.SECONDS);
    }

    private Runnable getData(String s)  {
        return () -> {
            try {
                data= readerValuesPage.getValuesToInfluxBD(pathServeur+s.concat(INNER_FILE), lastValues.get(s),deltas.get(s));
                if (!data.isEmpty()) {
                    lastValues.put(s,new ArrayList<>(data.values()));
                    pagesToInfluxdb.put((s.concat("-").concat(String.valueOf(System.currentTimeMillis() - START_TIME))), data);
                }

                logger.info(s + ": " + ((System.currentTimeMillis() - START_TIME))/1000+ " size : "+pagesToInfluxdb.size());

            } catch (IOException e) {
                logger.error("Caught exception in ScheduledExecutorService. StackTrace:\n" +e.getStackTrace());
            }
        };
    }

    private Runnable loadData() {
        return () ->{
            try {
                loaderInfluxDB.loadValuesInfluxDB(influxDb[3],pagesToInfluxdb);
                logger.info("-----------------------Total Data : "+pagesToInfluxdb.size());
            } catch (Exception e) {
                logger.error("Caught exception in ScheduledExecutorService. StackTrace:\n" +e.getStackTrace());
            }
        };

    }

}
