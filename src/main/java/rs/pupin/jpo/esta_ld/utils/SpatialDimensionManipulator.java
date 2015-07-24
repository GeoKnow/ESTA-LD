/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.pupin.jpo.esta_ld.utils;

import java.util.Collection;
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
        BY_CODE ("By two-letter code");
        
        private final String title;
        Kind(String title) {
            this.title = title;
        }

        @Override
        public String toString() {
            return title;
        }
    }
    
    private Kind kind;
    private final Dimension dim;
    
    public SpatialDimensionManipulator(Dimension dim, Kind kind){
        this.logger = Logger.getLogger(getClass().getName());
        this.dim = dim;
        this.kind = kind;
    }
    
    public String extractPairs(String prefix) {
        if (!dim.hasCodeList() || dim.getCodeList() == null) {
            return "[]";
        }
        
        StringBuilder sb = new StringBuilder();
        Collection<String> codes = dim.getCodeList().getAllCodes();
        
        if (codes.isEmpty()) { 
            return "[]";
        }
        
        for (String code: codes) {
            String res = code.substring(prefix.length());
            sb.append(", { ");
            sb.append("uri: ");
            sb.append("\"").append(code).append("\"");
            sb.append(", code: ");
            sb.append("\"").append(res).append("\"");
            sb.append(" }");
        } 
        sb.replace(0, 2, "[");
        sb.append("]");
        return sb.toString();
    }
    
}
