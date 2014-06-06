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
import rs.pupin.jpo.datacube.Thing;
import rs.pupin.jpo.esta_ld.utils.SparqlUtils;

/**
 *
 * @author vukm
 */
public abstract class SparqlThing implements Thing {
    protected final Repository repository;
    protected final String uri;
    protected final String graph;
    
    protected String label = null;
    protected Boolean indLabel = null;
    
    private static final String QUERY = "SELECT ?label \n"
            + "FROM <@graph> \n"
            + "WHERE { \n"
            + "  <@uri> rdfs:label ?label . \n"
            + "}";
    
    public SparqlThing(Repository repository, String uri, String graph){
        this.repository = repository;
        this.uri = uri;
        this.graph = graph;
    }

    public boolean hasLabel() {
        if (indLabel != null) return indLabel;
        
        String query = SparqlUtils.PREFIXES + QUERY;
        query = query.replace("@graph", graph).replace("@uri", uri);
        try {
            RepositoryConnection conn = repository.getConnection();
            TupleQuery q = conn.prepareTupleQuery(QueryLanguage.SPARQL, query);
            TupleQueryResult results = q.evaluate();
            if (results.hasNext()){
                label = results.next().getValue("label").stringValue();
                indLabel = Boolean.TRUE;
            } else indLabel = Boolean.FALSE;
        } catch (RepositoryException ex) {
            Logger.getLogger(SparqlThing.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedQueryException ex) {
            Logger.getLogger(SparqlThing.class.getName()).log(Level.SEVERE, null, ex);
        } catch (QueryEvaluationException ex) {
            Logger.getLogger(SparqlThing.class.getName()).log(Level.SEVERE, null, ex);
        }
        return indLabel;
    }

    public String getLabel() {
        if (hasLabel()) return label;
        else return null;
    }

    public String getUri() {
        return uri;
    }

    public String getGraph() {
        return graph;
    }
    
}
