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
import rs.pupin.jpo.datacube.ComponentProperty;
import rs.pupin.jpo.esta_ld.utils.SparqlUtils;

/**
 *
 * @author vukm
 */
public abstract class SparqlComponentProperty extends SparqlThing implements ComponentProperty {
    
    private static final String QUERY_VALUES = "SELECT DISTINCT ?val \n"
            + "FROM <@graph> \n"
            + "WHERE { \n"
            + "  ?obs qb:dataSet ?ds . \n"
            + "  ?obs <@dim> ?val . \n"
            + "}";
    private static final String QUERY_CONCEPTS = "SELECT ?c \n"
            + "FROM <@graph> \n"
            + "WHERE { \n"
            + "  <@dim> qb:concept ?c . \n"
            + "}";
    private static final String QUERY_RANGE = "SELECT ?t \n"
            + "FROM <@graph> \n"
            + "WHERE { \n"
            + "  <@dim> rdfs:range ?t . \n"
            + "}";
    
    private Collection<String> values;
    private Collection<String> concepts;
    private Collection<String> ranges;

    public SparqlComponentProperty(Repository repository, String uri, String graph) {
        super(repository, uri, graph);
        
        this.values = null;
        this.concepts = null;
        this.ranges = null;
    }

    public Collection<String> getValues() {
        if (values != null) return values;
        
        try {
            RepositoryConnection conn = repository.getConnection();
            String query = SparqlUtils.PREFIXES + QUERY_VALUES;
            query = query.replace("@graph", graph).replace("@dim", uri);
            TupleQuery q = conn.prepareTupleQuery(QueryLanguage.SPARQL, query);
            TupleQueryResult results = q.evaluate();
            values = new LinkedList<String>();
            while (results.hasNext()) {
                BindingSet set = results.next();
                values.add(set.getValue("val").stringValue());
            }
            results.close();
        } catch (RepositoryException ex) {
            Logger.getLogger(SparqlDimension.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedQueryException ex) {
            Logger.getLogger(SparqlDimension.class.getName()).log(Level.SEVERE, null, ex);
        } catch (QueryEvaluationException ex) {
            Logger.getLogger(SparqlDimension.class.getName()).log(Level.SEVERE, null, ex);
        }
        return values;
    }

    public Collection<String> getConcepts() {
        if (concepts != null) return concepts;
        
        try {
            RepositoryConnection conn = repository.getConnection();
            String query = SparqlUtils.PREFIXES + QUERY_CONCEPTS;
            query = query.replace("@graph", graph).replace("@dim", uri);
            TupleQuery q = conn.prepareTupleQuery(QueryLanguage.SPARQL, query);
            TupleQueryResult results = q.evaluate();
            concepts = new LinkedList<String>();
            while (results.hasNext()) {
                BindingSet set = results.next();
                concepts.add(set.getValue("c").stringValue());
            }
            results.close();
        } catch (RepositoryException ex) {
            Logger.getLogger(SparqlComponentProperty.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedQueryException ex) {
            Logger.getLogger(SparqlComponentProperty.class.getName()).log(Level.SEVERE, null, ex);
        } catch (QueryEvaluationException ex) {
            Logger.getLogger(SparqlComponentProperty.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return concepts;
    }

    public Collection<String> getRanges() {
        if (ranges != null) return ranges;
        
        try {
            RepositoryConnection conn = repository.getConnection();
            String query = SparqlUtils.PREFIXES + QUERY_RANGE;
            query = query.replace("@graph", graph).replace("@dim", uri);
            TupleQuery q = conn.prepareTupleQuery(QueryLanguage.SPARQL, query);
            TupleQueryResult results = q.evaluate();
            ranges = new LinkedList<String>();
            if (results.hasNext()){
                ranges.add(results.next().getValue("t").stringValue());
            }
        } catch (RepositoryException ex) {
            Logger.getLogger(SparqlDimension.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedQueryException ex) {
            Logger.getLogger(SparqlDimension.class.getName()).log(Level.SEVERE, null, ex);
        } catch (QueryEvaluationException ex) {
            Logger.getLogger(SparqlDimension.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ranges;
    }

    @Override
    public String toString() {
        if (!hasLabel()) {
            int hashPosition = getUri().lastIndexOf('#');
            int slashPosition = getUri().lastIndexOf('/');
            if (hashPosition > slashPosition) return getUri().substring(hashPosition+1);
            else if (slashPosition > -1) return getUri().substring(slashPosition+1);
        } else
            return getLabel();
        
        return super.toString();
    }
    
}
