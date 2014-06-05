/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package rs.pupin.jpo.datacube.sparql_impl;

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
import rs.pupin.jpo.datacube.DataSet;
import rs.pupin.jpo.datacube.Structure;
import rs.pupin.jpo.esta_ld.utils.SparqlUtils;

/**
 *
 * @author vukm
 */
public class SparqlDataSet implements DataSet {
    private Repository repository;
    private String uri;
    private String graph;
    
    private Structure structure;
    
    private static final String QUERY = "SELECT DISTINCT ?dsd \n"
            + "FROM <@graph> \n"
            + "WHERE { \n"
            + "  <@ds> qb:structure ?dsd . \n"
            + "}";
    
    public SparqlDataSet(Repository repository, String uri, String graph){
        this.repository = repository;
        this.uri = uri;
        this.graph = graph;
        this.structure = null;
    }

    public Structure getStructure() {
        if (structure != null) return structure;
        
        try {
            RepositoryConnection conn = repository.getConnection();
            String query = SparqlUtils.PREFIXES + QUERY;
            query = query.replace("@graph", graph).replace("@ds", uri);
            TupleQuery q = conn.prepareTupleQuery(QueryLanguage.SPARQL, query);
            TupleQueryResult results = q.evaluate();
            while (results.hasNext()){
                BindingSet set = results.next();
                String dsd = set.getValue("dsd").stringValue();
                structure = new SparqlStructure(repository, dsd, graph);
            }
        } catch (RepositoryException ex) {
            Logger.getLogger(SparqlDataSet.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedQueryException ex) {
            Logger.getLogger(SparqlDataSet.class.getName()).log(Level.SEVERE, null, ex);
        } catch (QueryEvaluationException ex) {
            Logger.getLogger(SparqlDataSet.class.getName()).log(Level.SEVERE, null, ex);
        }
        return structure;
    }
    
    public void setStructure(Structure structure){
        this.structure = structure;
    }

    public String getUri() {
        return uri;
    }

    public String getGraph() {
        return graph;
    }
    
}
