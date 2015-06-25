/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package rs.pupin.jpo.datacube;

import java.util.Collection;
import org.openrdf.model.Value;

/**
 *
 * @author vukm
 */
public interface DataSet extends Thing{
    
    public Structure getStructure();
    public Collection<Value> getValuesForDimension(Dimension dimension);
    
}
