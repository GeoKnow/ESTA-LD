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
import rs.pupin.jpo.datacube.DataCubeGraph;
import rs.pupin.jpo.datacube.DataSet;
import rs.pupin.jpo.datacube.Structure;
import rs.pupin.jpo.esta_ld.utils.SparqlUtils;

/**
 *
 * @author vukm
 */
public class SparqlDCGraph implements DataCubeGraph {
    
    private final Repository repository;
    private final String uri;
    private Collection<DataSet> datasets;
    private Collection<Structure> structures;
    
    private static final String QUERY_DATASETS = "SELECT DISTINCT ?ds \n"
            + "FROM <@graph> \n"
            + "WHERE { \n"
            + "  { ?ds qb:structure ?dsd . } \n"
            + "  UNION \n"
            + "  { ?obs qb:dataSet ?ds . } \n"
            + "  UNION \n"
            + "  { ?ds a qb:DataSet . } \n"
            + "}";
    private static final String QUERY_STRUCTURES = "SELECT DISTINCT ?dsd \n"
            + "FROM <@graph> \n"
            + "WHERE { \n"
            + "  { ?ds qb:structure ?dsd . } \n"
            + "  UNION \n"
            + "  { ?dsd a qb:DataStructureDefinition . } \n"
            + "}";
    
    public SparqlDCGraph(Repository repository, String uri){
        this.repository = repository;
        this.uri = uri;
        
        this.datasets = null;
        this.structures = null;
    }

    public Collection<DataSet> getDataSets() {
        if (datasets != null) return datasets;
        
        try {
            RepositoryConnection conn = repository.getConnection();
            String query = SparqlUtils.PREFIXES + QUERY_DATASETS;
            query = query.replace("@graph", uri);
            TupleQuery q = conn.prepareTupleQuery(QueryLanguage.SPARQL, query);
            TupleQueryResult results = q.evaluate();
            datasets = new LinkedList<DataSet>();
            while (results.hasNext()){
                BindingSet set = results.next();
                String ds = set.getValue("ds").stringValue();
                SparqlDataSet sparql_ds = new SparqlDataSet(repository, ds, uri);
                datasets.add(sparql_ds);
            }
        } catch (RepositoryException ex) {
            Logger.getLogger(SparqlDCGraph.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedQueryException ex) {
            Logger.getLogger(SparqlDCGraph.class.getName()).log(Level.SEVERE, null, ex);
        } catch (QueryEvaluationException ex) {
            Logger.getLogger(SparqlDCGraph.class.getName()).log(Level.SEVERE, null, ex);
        }
        return datasets;
    }

    public Collection<Structure> getStructures() {
        if (structures != null) return structures;
        
        try {
            RepositoryConnection conn = repository.getConnection();
            String query = SparqlUtils.PREFIXES + QUERY_STRUCTURES;
            query = query.replace("@graph", uri);
            TupleQuery q = conn.prepareTupleQuery(QueryLanguage.SPARQL, query);
            TupleQueryResult results = q.evaluate();
            structures = new LinkedList<Structure>();
            while (results.hasNext()){
                BindingSet set = results.next();
                String dsd = set.getValue("dsd").stringValue();
                SparqlStructure sparql_dsd = new SparqlStructure(repository, dsd, uri);
                structures.add(sparql_dsd);
            }
        } catch (RepositoryException ex) {
            Logger.getLogger(SparqlDCGraph.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedQueryException ex) {
            Logger.getLogger(SparqlDCGraph.class.getName()).log(Level.SEVERE, null, ex);
        } catch (QueryEvaluationException ex) {
            Logger.getLogger(SparqlDCGraph.class.getName()).log(Level.SEVERE, null, ex);
        }
        return structures;
    }

    public void setDatasets(Collection<DataSet> datasets) {
        this.datasets = datasets;
    }

    public void setStructures(Collection<Structure> structures) {
        this.structures = structures;
    }

    public String getUri() {
        return uri;
    }

    @Override
    public String toString() {
        return uri;
    }
    
}
