/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package rs.pupin.jpo.datacube.sparql_impl;

import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import rs.pupin.jpo.datacube.Attribute;
import rs.pupin.jpo.datacube.DataSet;
import rs.pupin.jpo.datacube.Dimension;
import rs.pupin.jpo.datacube.Measure;
import rs.pupin.jpo.datacube.Structure;
import rs.pupin.jpo.esta_ld.utils.SparqlUtils;

/**
 *
 * @author vukm
 */
public class SparqlStructure extends SparqlThing implements Structure {
    
    private Collection<DataSet> datasets;
    private Collection<Dimension> dimensions;
    private Collection<Attribute> attributes;
    private Collection<Measure> measures;
    
    private static final String QUERY_DATASETS = "SELECT DISTINCT ?ds \n"
            + "FROM <@graph> \n"
            + "WHERE { \n"
            + "  ?ds qb:structure <@dsd> . \n"
            + "}";
    private static final String QUERY_DIMENSIONS = "SELECT DISTINCT ?dim \n"
            + "FROM <@graph> \n"
            + "WHERE { \n"
            + "  <@dsd> qb:component ?cs . \n"
            + "  { { ?cs qb:dimension ?dim } UNION { \n"
            + "    ?cs qb:componentProperty ?dim . \n"
            + "    ?dim a qb:DimensionProperty . } \n"
            + "  } \n"
            + "}";
    private static final String QUERY_ATTRIBUTES = "SELECT DISTINCT ?attr \n"
            + "FROM <@graph> \n"
            + "WHERE { \n"
            + "  <@dsd> qb:component ?cs . \n"
            + "  { { ?cs qb:attribute ?attr } UNION { \n"
            + "    ?cs qb:componentProperty ?attr . \n"
            + "    ?attr a qb:AttributeProperty . } \n"
            + "  } \n"
            + "}";
    private static final String QUERY_MEASURES = "SELECT DISTINCT ?meas \n"
            + "FROM <@graph> \n"
            + "WHERE { \n"
            + "  <@dsd> qb:component ?cs . \n"
            + "  { { ?cs qb:measure ?meas } UNION { \n"
            + "    ?cs qb:componentProperty ?meas . \n"
            + "    ?meas a qb:MeasureProperty . } \n"
            + "  } \n"
            + "}";
    
    public SparqlStructure(Repository repository, String uri, String graph){
        super(repository, uri, graph);
        
        this.datasets = null;
        this.dimensions = null;
        this.attributes = null;
        this.measures = null;
    }

    public Collection<DataSet> getAllDataSets() {
        if (datasets != null) return datasets;
        
        try {
            RepositoryConnection conn = repository.getConnection();
            String query = SparqlUtils.PREFIXES + QUERY_DATASETS;
            query = query.replace("@graph", graph).replace("@dsd", uri);
            TupleQuery q = conn.prepareTupleQuery(QueryLanguage.SPARQL, query);
            TupleQueryResult results = q.evaluate();
            datasets = new LinkedList<DataSet>();
            while (results.hasNext()){
                BindingSet set = results.next();
                String ds = set.getValue("ds").stringValue();
                SparqlDataSet sparql_ds = new SparqlDataSet(repository, ds, graph);
                sparql_ds.setStructure(this);
                datasets.add(sparql_ds);
            }
        } catch (RepositoryException ex) {
            Logger.getLogger(SparqlStructure.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedQueryException ex) {
            Logger.getLogger(SparqlStructure.class.getName()).log(Level.SEVERE, null, ex);
        } catch (QueryEvaluationException ex) {
            Logger.getLogger(SparqlStructure.class.getName()).log(Level.SEVERE, null, ex);
        }
        return datasets;
    }

    public Collection<Dimension> getDimensions() {
        if (dimensions != null) return dimensions;
        
        try {
            RepositoryConnection conn = repository.getConnection();
            String query = SparqlUtils.PREFIXES + QUERY_DIMENSIONS;
            query = query.replace("@graph", graph).replace("@dsd", uri);
            TupleQuery q = conn.prepareTupleQuery(QueryLanguage.SPARQL, query);
            TupleQueryResult results = q.evaluate();
            dimensions = new LinkedList<Dimension>();
            while (results.hasNext()){
                BindingSet set = results.next();
                String dim = set.getValue("dim").stringValue();
                SparqlDimension sparql_dim = new SparqlDimension(repository, dim, graph);
                sparql_dim.setStructure(this);
                dimensions.add(sparql_dim);
            }
        } catch (RepositoryException ex) {
            Logger.getLogger(SparqlStructure.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedQueryException ex) {
            Logger.getLogger(SparqlStructure.class.getName()).log(Level.SEVERE, null, ex);
        } catch (QueryEvaluationException ex) {
            Logger.getLogger(SparqlStructure.class.getName()).log(Level.SEVERE, null, ex);
        }
        return dimensions;
    }

    public Collection<Attribute> getAttributes() {
        if (attributes != null) return attributes;
        
        try {
            RepositoryConnection conn = repository.getConnection();
            String query = SparqlUtils.PREFIXES + QUERY_ATTRIBUTES;
            query = query.replace("@graph", graph).replace("@dsd", uri);
            TupleQuery q = conn.prepareTupleQuery(QueryLanguage.SPARQL, query);
            TupleQueryResult results = q.evaluate();
            attributes = new LinkedList<Attribute>();
            while (results.hasNext()){
                BindingSet set = results.next();
                String attr = set.getValue("attr").stringValue();
                SparqlAttribute sparql_attr = new SparqlAttribute(repository, attr, graph);
                sparql_attr.setStructure(this);
                attributes.add(sparql_attr);
            }
        } catch (RepositoryException ex) {
            Logger.getLogger(SparqlStructure.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedQueryException ex) {
            Logger.getLogger(SparqlStructure.class.getName()).log(Level.SEVERE, null, ex);
        } catch (QueryEvaluationException ex) {
            Logger.getLogger(SparqlStructure.class.getName()).log(Level.SEVERE, null, ex);
        }
        return attributes;
    }

    public Collection<Measure> getMeasures() {
        if (measures != null) return measures;
        
        try {
            RepositoryConnection conn = repository.getConnection();
            String query = SparqlUtils.PREFIXES + QUERY_MEASURES;
            query = query.replace("@graph", graph).replace("@dsd", uri);
            TupleQuery q = conn.prepareTupleQuery(QueryLanguage.SPARQL, query);
            TupleQueryResult results = q.evaluate();
            measures = new LinkedList<Measure>();
            while (results.hasNext()){
                BindingSet set = results.next();
                String meas = set.getValue("meas").stringValue();
                SparqlMeasure sparql_meas = new SparqlMeasure(repository, meas, graph);
                sparql_meas.setStructure(this);
                measures.add(sparql_meas);
            }
        } catch (RepositoryException ex) {
            Logger.getLogger(SparqlStructure.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedQueryException ex) {
            Logger.getLogger(SparqlStructure.class.getName()).log(Level.SEVERE, null, ex);
        } catch (QueryEvaluationException ex) {
            Logger.getLogger(SparqlStructure.class.getName()).log(Level.SEVERE, null, ex);
        }
        return measures;
    }

    public void setDatasets(Collection<DataSet> datasets) {
        this.datasets = datasets;
    }
    
}
