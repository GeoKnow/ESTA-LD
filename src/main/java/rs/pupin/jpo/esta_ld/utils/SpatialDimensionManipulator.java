/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.pupin.jpo.esta_ld.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import rs.pupin.jpo.datacube.Dimension;

/**
 *
 * @author vukm
 */
public class SpatialDimensionManipulator {
    
    private final Logger logger;
    
    public static enum Kind {
        BY_LABEL ("By Label"), 
        BY_CODE ("By two-letter code"),
        BY_CODE3 ("By three-letter code");
        
        private final String title;
        Kind(String title) {
            this.title = title;
        }

        @Override
        public String toString() {
            return title;
        }
    }
    
    private static final Map<String, String> twoLetterMapping = new HashMap<String, String>();
    private static final Map<String, String> threeLetterMapping = new HashMap<String, String>();
    
    static {
        InputStream in = SpatialDimensionManipulator.class.getResourceAsStream("/countrycode.csv");
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String line = "";
        try {
            while ((line = br.readLine()) != null){
                String[] country = line.split(",");
                String name = country[0].substring(1, country[0].length()-1);
                String iso2 = country[1].substring(1, country[1].length()-1);
                String iso3 = country[2].substring(1, country[2].length()-1);
                twoLetterMapping.put(iso2, name);
                threeLetterMapping.put(iso3, name);
            }
            twoLetterMapping.put("UK", "United Kingdom");
            twoLetterMapping.put("EL", "Greece");
            br.close();
        } catch (IOException ex) {
            Logger.getLogger(SpatialDimensionManipulator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private final Kind kind;
    private final Dimension dim;
    
    public SpatialDimensionManipulator(Dimension dim, Kind kind){
        this.logger = Logger.getLogger(getClass().getName());
        this.dim = dim;
        this.kind = kind;
    }
    
    public String extractPairs(String prefix) {
        Collection<String> countries = new LinkedList<String>();
        Collection<String> list = null;
        if (dim.hasCodeList() && dim.getCodeList() != null) {
            list = dim.getCodeList().getAllCodes();
        } else {
            list = dim.getValues();
        }
        if ( list == null || list.isEmpty()){
            return "[]";
        }
        StringBuilder sb = new StringBuilder();
        for (String code: list){
            String parsed = code.substring(prefix.length());
            String c;
            if (code.startsWith("http://elpo.stat.gov.rs/lod2/RS-DIC/geo/RS"))
                c = parsed;
            else
                switch(kind){
                    case BY_CODE: c = twoLetterMapping.get(parsed); break;
                    case BY_CODE3: c = threeLetterMapping.get(parsed); break;
                    case BY_LABEL: c = parsed; break;
                    default: c = null;
                }
            if (c != null) { 
                countries.add(c);
                sb.append(", { uri: \"").append(code).append("\", code: \"").append(c).append("\" }");
            }
            else logger.log(Level.WARNING, "Didn''t recognize country: {0}", code);
        }
        
        sb.replace(0, 2, "[");
        sb.append("]");
        return sb.toString();
    }
    
}
