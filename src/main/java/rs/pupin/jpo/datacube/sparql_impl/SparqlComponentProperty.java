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
    
    private Collection<String> values;

    public SparqlComponentProperty(Repository repository, String uri, String graph) {
        super(repository, uri, graph);
        
        this.values = null;
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
