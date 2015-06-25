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
import org.openrdf.model.Value;
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
import rs.pupin.jpo.datacube.Dimension;
import rs.pupin.jpo.datacube.Structure;
import rs.pupin.jpo.esta_ld.utils.SparqlUtils;

/**
 *
 * @author vukm
 */
public class SparqlDataSet extends SparqlThing implements DataSet {
    
    private Structure structure;
    
    private static final String QUERY = "SELECT DISTINCT ?dsd \n"
            + "FROM <@graph> \n"
            + "WHERE { \n"
            + "  <@ds> qb:structure ?dsd . \n"
            + "}";
    private static final String QUERY_DIM_VALUES = "SELECT DISTINCT ?val \n"
            + "FROM <@graph> \n"
            + "WHERE { \n"
            + "  ?obs qb:dataSet <@ds> . \n"
            + "  ?obs <@dim> ?val . \n"
            + "} order by ?val";
    
    public SparqlDataSet(Repository repository, String uri, String graph){
        super(repository, uri, graph);
        
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

    public Collection<Value> getValuesForDimension(Dimension dimension) {
        for (Dimension dim: getStructure().getDimensions())
            if (dim.equals(dimension)) {
                try {
                    RepositoryConnection conn = repository.getConnection();
                    String query = SparqlUtils.PREFIXES + QUERY_DIM_VALUES;
                    query = query.replace("@graph", graph).replace("@ds", uri).replace("@dim", dimension.getUri());
                    TupleQuery q = conn.prepareTupleQuery(QueryLanguage.SPARQL, query);
                    TupleQueryResult results = q.evaluate();
                    LinkedList<Value> codes = new LinkedList<Value>();
                    while (results.hasNext())
                        codes.add(results.next().getValue("val"));
                    return codes;
                } catch (RepositoryException ex) {
                    Logger.getLogger(SparqlDataSet.class.getName()).log(Level.SEVERE, null, ex);
                } catch (MalformedQueryException ex) {
                    Logger.getLogger(SparqlDataSet.class.getName()).log(Level.SEVERE, null, ex);
                } catch (QueryEvaluationException ex) {
                    Logger.getLogger(SparqlDataSet.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        return null;
    }
    
}
