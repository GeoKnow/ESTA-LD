/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package rs.pupin.jpo.datacube.sparql_impl;

import java.util.logging.Level;
import java.util.logging.Logger;
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
public class SparqlDimension extends SparqlThing implements Dimension {
    
    private String range;
    private Structure structure;
    private CodeList codeList;
    private Boolean indCodeList;
    private Boolean indTimeDimension;
    private Boolean indGeoDimension;
    
    private static final String QUERY_RANGE = "SELECT ?t \n"
            + "FROM <@graph> \n"
            + "WHERE { \n"
            + "  <@dim> rdfs:range ?t . \n"
            + "}";
    private static final String[] TIME_RANGES = new String [] {
        "http://www.w3.org/2001/XMLSchema#gYear", 
        "http://www.w3.org/2001/XMLSchema#gYearMonth", 
        "http://www.w3.org/2002/07/owl#time"
    };
    private static final String[] GEO_RANGES = new String [] {
        "http://elpo.stat.gov.rs/lod2/RS-DIC/rs/geo"
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
    
    public SparqlDimension(Repository repository, String uri, String graph){
        super(repository, uri, graph);
        
        this.range = null;
        this.structure = null;
        this.codeList = null;
        this.indCodeList = null;
        this.indTimeDimension = null;
        this.indGeoDimension = null;
    }

    public boolean isTimeDimension() {
        if (indTimeDimension != null) return indTimeDimension;
        
        for (String r: TIME_RANGES){
            if (r.equals(getRange())) 
                return indTimeDimension = Boolean.TRUE;
        }
        return indTimeDimension = Boolean.FALSE;
    }

    public boolean isGeoDimension() {
        if (indGeoDimension != null) return indGeoDimension;
        
        for (String r: GEO_RANGES){
            if (r.equals(getRange())) 
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

    public String getRange() {
        if (range != null) return range;
        
        try {
            RepositoryConnection conn = repository.getConnection();
            String query = SparqlUtils.PREFIXES + QUERY_RANGE;
            query = query.replace("@graph", graph).replace("@dim", uri);
            TupleQuery q = conn.prepareTupleQuery(QueryLanguage.SPARQL, query);
            TupleQueryResult results = q.evaluate();
            if (results.hasNext()){
                return range = results.next().getValue("t").stringValue();
            }
        } catch (RepositoryException ex) {
            Logger.getLogger(SparqlDimension.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedQueryException ex) {
            Logger.getLogger(SparqlDimension.class.getName()).log(Level.SEVERE, null, ex);
        } catch (QueryEvaluationException ex) {
            Logger.getLogger(SparqlDimension.class.getName()).log(Level.SEVERE, null, ex);
        }
        return range;
    }

    public void setRange(String range) {
        this.range = range;
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
