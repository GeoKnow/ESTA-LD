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
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import rs.pupin.jpo.datacube.CodeList;
import rs.pupin.jpo.esta_ld.utils.SparqlUtils;

/**
 *
 * @author vukm
 */
public class SparqlCodeList extends SparqlThing implements CodeList {
    
    private Collection<String> codes;
    
    private static final String QUERY_CODES = "SELECT DISTINCT ?code \n"
            + "FROM <@graph> \n"
            + "WHERE { \n"
            + "  { ?code skos:inScheme <@cl> } \n"
            + "  UNION \n"
            + "  { <@cl> skos:hasTopConcept ?code } \n"
            + "}";
    
    public SparqlCodeList(Repository repository, String uri, String graph){
        super(repository, uri, graph);
        
        this.codes = null;
    }

    public Collection<String> getAllCodes() {
        if (codes != null) return codes;
        
        try {
            RepositoryConnection conn = repository.getConnection();
            String query = SparqlUtils.PREFIXES + QUERY_CODES;
            query = query.replace("@graph", graph).replace("@cl", uri);
            TupleQuery q = conn.prepareTupleQuery(QueryLanguage.SPARQL, query);
            TupleQueryResult results = q.evaluate();
            codes = new LinkedList<String>();
            while (results.hasNext()){
                String code = results.next().getValue("code").stringValue();
                codes.add(code);
            }
        } catch (RepositoryException ex) {
            Logger.getLogger(SparqlDimension.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedQueryException ex) {
            Logger.getLogger(SparqlDimension.class.getName()).log(Level.SEVERE, null, ex);
        } catch (QueryEvaluationException ex) {
            Logger.getLogger(SparqlDimension.class.getName()).log(Level.SEVERE, null, ex);
        }
        return codes;
    }

    public Collection<String> codesOnLevel(int level) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public int numCodeLevels() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
