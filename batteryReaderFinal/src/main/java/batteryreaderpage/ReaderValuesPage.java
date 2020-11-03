package batteryreaderpage;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReaderValuesPage {

    private List <String> valuesToInflixDB = new ArrayList<>();
    private List <String> valuesPage = new ArrayList<>();
    private String [] stringvalues;


      public Map<String, String> getValuesToInfluxBD (String file, List <String> previousValues, Map<String, String> delta) throws IOException {

        valuesToInflixDB=(applyFilterstoValues( previousValues, getValuesfromPage(file), delta));
        //  valuesToInflixDB=getValuesfromPage(file);

        Map<String,String> map = new LinkedHashMap<>();  // ordered

        List<String> keys=new ArrayList<>(delta.keySet());

            for (int i=0; i<valuesToInflixDB.size(); i++) {
                map.put(keys.get(i),valuesToInflixDB.get(i) );
            }

        return map;
    }

    public List <String> getValuesfromPage(String file) throws IOException {
        Document doc = null;
        //doc = Jsoup.parse(new File(file), "utf-8");
        doc = Jsoup.connect(file).get();
        valuesPage=getValueInner(doc.body().toString());
        return valuesPage;
    }

    public List <String> applyFilterstoValues( List <String> previousValues , List <String> newValues, Map<String, String> deltas){

        List <String> listS=new ArrayList<>();
        if (deltas.containsKey("stringvalues")){
            stringvalues=deltas.get("stringvalues").split(",");
            listS = Arrays.asList(stringvalues);
            //deltas.remove("stringvalues");
        }

        List<String> deltaV = new ArrayList<>(deltas.values());
        List<String> deltaK = new ArrayList<>(deltas.keySet());
        List<String> previewV = new ArrayList<>(previousValues);

        if(newValues!=null){
        for (int i=0;i<newValues.size();i++) {
            if(!listS.contains(deltaK.get(i))){
                if(Math.abs(Double.parseDouble( previewV.get(i)) -  Double.parseDouble(newValues.get(i))) > Double.parseDouble(deltaV.get(i))) {
                    return newValues;
                }
            }
        }}


        return Collections.emptyList();
    }

    public List<String> getValueInner(String values ) {

        Pattern p = Pattern.compile("(readonly value=\")(.*?)(\">)");
        Matcher m = p.matcher(values);
        List<String> matches = new ArrayList<>();
        while (m.find()) {
            matches.add(m.group(2));
        }

        return matches;
    }

}
