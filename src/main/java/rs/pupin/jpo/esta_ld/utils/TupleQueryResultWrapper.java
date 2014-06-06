/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package rs.pupin.jpo.esta_ld.utils;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;

/**
 *
 * @author vukm
 */
public class TupleQueryResultWrapper implements TupleQueryResult {
    
    private TupleQueryResult result;
    
    public TupleQueryResultWrapper (TupleQueryResult result){
        this.result = result;
    }

    public List<String> getBindingNames() throws QueryEvaluationException {
        return result.getBindingNames();
    }

    public void close() throws QueryEvaluationException {
        result.close();
    }

    public boolean hasNext() throws QueryEvaluationException {
        try {
            return result.hasNext();
        } catch (Exception e){
            Logger.getLogger(TupleQueryResultWrapper.class.getName()).log(Level.SEVERE, null, e);
        }
        return true;
    }

    public BindingSet next() throws QueryEvaluationException {
        try {
            return result.next();
        } catch (Exception e){
            Logger.getLogger(TupleQueryResultWrapper.class.getName()).log(Level.SEVERE, null, e);
        }
        return null;
    }

    public void remove() throws QueryEvaluationException {
        result.remove();
    }
    
}
