/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package rs.pupin.jpo.datacube.sparql_impl;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import rs.pupin.jpo.datacube.CodeList;
import rs.pupin.jpo.datacube.Dimension;
import rs.pupin.jpo.datacube.Structure;
import rs.pupin.jpo.esta_ld.utils.SparqlUtils;

/**
 *
 * @author vukm
 */
public class SparqlDimension extends SparqlComponentProperty implements Dimension {
    
    private Structure structure;
    private CodeList codeList;
    private Boolean indCodeList;
    private Boolean indTimeDimension;
    private Boolean indGeoDimension;
    private Boolean indHasGeometries;
    
    private static final String TIME_CONCEPT = "http://purl.org/linked-data/sdmx/2009/concept#refPeriod";
    private static final String GEO_CONCEPT = "http://purl.org/linked-data/sdmx/2009/concept#refArea";
    
    private static final String[] TIME_RANGES = new String [] {
        "http://www.w3.org/2001/XMLSchema#date",
        "http://www.w3.org/2001/XMLSchema#gYear", 
        "http://www.w3.org/2001/XMLSchema#gYearMonth", 
        "http://www.w3.org/2002/07/owl#time"
    };
    private static final String[] TIME_URIS = new String[] {
        "http://purl.org/dc/terms/date", 
        "http://elpo.stat.gov.rs/lod2/RS-DIC/rs/time",
        "http://purl.org/linked-data/sdmx/2009/dimension#refTime"
    };
    private static final String[] GEO_URIS = new String [] {
        "http://elpo.stat.gov.rs/lod2/RS-DIC/rs/geo", 
        "http://ontologycentral.com/2009/01/eurostat/ns#geo",
        "http://purl.org/linked-data/sdmx/2009/dimension#refArea"
    } ;
    private static final String QUERY_CODELIST = "SELECT ?cl \n"
            + "FROM <@graph> \n"
            + "WHERE { \n"
            + "  <@dim> qb:codeList ?cl . \n"
            + "}";
    private static final String QUERY_STRUCTURE = "SELECT ?dsd \n"
            + "FROM <@graph> \n"
            + "WHERE { \n"
            + "  ?dsd qb:component ?cs . \n"
            + "  { { ?cs qb:componentProperty <@dim> } UNION \n"
            + "    { ?cs qb:dimension <@dim> } \n"
            + "  } \n"
            + "}";
    private static final String QUERY_HAS_GEOMETRIES = "ASK \n"
            + "FROM <@graph> \n"
            + "WHERE { \n"
            + "  ?obs qb:dataSet <@ds> . \n"
            + "  ?obs <@dim> ?val . \n"
            + "  ?val ogc:hasDefaultGeometry ?geo . \n"
            + "  ?geo ogc:asWKT ?geoWKT . \n"
            + "}";
    
    public SparqlDimension(Repository repository, String uri, String graph){
        super(repository, uri, graph);
        
        this.structure = null;
        this.codeList = null;
        this.indCodeList = null;
        this.indTimeDimension = null;
        this.indGeoDimension = null;
        this.indHasGeometries = null;
    }

    public boolean isTimeDimension() {
        if (indTimeDimension != null) return indTimeDimension;
        
//        boolean noTimeConceptPresent = true;
//        for (String c: getConcepts()) {
//            if (c.equalsIgnoreCase(TIME_CONCEPT)){
//                noTimeConceptPresent = false;
//                break;
//            }
//        }
//        if (noTimeConceptPresent) return indTimeDimension = Boolean.FALSE;
        
        for (String r: TIME_RANGES){
            for (String r2: getRanges()){
                if (r.equalsIgnoreCase(r2))
                    return indTimeDimension = Boolean.TRUE;
            }
        }
        for (String r: TIME_URIS){
            if (r.equals(uri)) 
                return indTimeDimension = Boolean.TRUE;
        }
        
        return indTimeDimension = Boolean.FALSE;
    }

    public boolean isGeoDimension() {
        if (indGeoDimension != null) return indGeoDimension;
        
//        boolean noGeoConceptPresent = true;
//        for (String c: getConcepts()) {
//            if (c.equalsIgnoreCase(GEO_CONCEPT)){
//                noGeoConceptPresent = false;
//                break;
//            }
//        }
//        if (noGeoConceptPresent) return indGeoDimension = Boolean.FALSE;
        
        if (hasGeometries()) return indGeoDimension = Boolean.TRUE;
        
        for (String r: GEO_URIS){
            if (r.equals(uri)) 
                return indGeoDimension = Boolean.TRUE;
        }
        
        return indGeoDimension = Boolean.FALSE;
    }

    public boolean hasCodeList() {
        if (indCodeList != null) return indCodeList;
        
        if (getCodeList() != null) return indCodeList = Boolean.TRUE; 
        return indCodeList = Boolean.FALSE;
    }

    public CodeList getCodeList() {
        if (codeList != null) return codeList;
        if (indCodeList != null) return null;
        
        try {
            RepositoryConnection conn = repository.getConnection();
            String query = SparqlUtils.PREFIXES + QUERY_CODELIST;
            query = query.replace("@graph", graph).replace("@dim", uri);
            TupleQuery q = conn.prepareTupleQuery(QueryLanguage.SPARQL, query);
            TupleQueryResult results = q.evaluate();
            if (results.hasNext()){
                indCodeList = Boolean.TRUE;
                String cl = results.next().getValue("cl").stringValue();
                return codeList = new SparqlCodeList(repository, cl, graph);
            } else indCodeList = Boolean.FALSE;
        } catch (RepositoryException ex) {
            Logger.getLogger(SparqlDimension.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedQueryException ex) {
            Logger.getLogger(SparqlDimension.class.getName()).log(Level.SEVERE, null, ex);
        } catch (QueryEvaluationException ex) {
            Logger.getLogger(SparqlDimension.class.getName()).log(Level.SEVERE, null, ex);
        }
        return codeList;
    }

    public Structure getStructure() {
        if (structure != null) return structure;
        
        try {
            RepositoryConnection conn = repository.getConnection();
            String query = SparqlUtils.PREFIXES + QUERY_STRUCTURE;
            query = query.replace("@graph", graph).replace("@dim", uri);
            TupleQuery q = conn.prepareTupleQuery(QueryLanguage.SPARQL, query);
            TupleQueryResult results = q.evaluate();
            if (results.hasNext()){
                String dsd = results.next().getValue("dsd").stringValue();
                return structure = new SparqlStructure(repository, dsd, graph);
            }
        } catch (RepositoryException ex) {
            Logger.getLogger(SparqlDimension.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedQueryException ex) {
            Logger.getLogger(SparqlDimension.class.getName()).log(Level.SEVERE, null, ex);
        } catch (QueryEvaluationException ex) {
            Logger.getLogger(SparqlDimension.class.getName()).log(Level.SEVERE, null, ex);
        }
        return structure;
    }
    
    public Boolean hasGeometries(){
        if (indHasGeometries != null) return indHasGeometries;
        
        try {
            RepositoryConnection conn = repository.getConnection();
            String query = SparqlUtils.PREFIXES + QUERY_HAS_GEOMETRIES;
            query = query.replace("@graph", graph).replace("@dim", uri);
            BooleanQuery q = conn.prepareBooleanQuery(QueryLanguage.SPARQL, query);
            return indHasGeometries = q.evaluate();
        } catch (RepositoryException ex) {
            Logger.getLogger(SparqlDimension.class.getName()).log(Level.SEVERE, null, ex);
        } catch (QueryEvaluationException ex) {
            Logger.getLogger(SparqlDimension.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedQueryException ex) {
            Logger.getLogger(SparqlDimension.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return indHasGeometries;
    }

    public void setStructure(Structure structure) {
        this.structure = structure;
    }

    public void setCodeList(CodeList codeList) {
        this.codeList = codeList;
    }

    public void setIndTimeDimension(Boolean indTimeDimension) {
        this.indTimeDimension = indTimeDimension;
    }

    public void setIndGeoDimension(Boolean indGeoDimension) {
        this.indGeoDimension = indGeoDimension;
    }
    
}
