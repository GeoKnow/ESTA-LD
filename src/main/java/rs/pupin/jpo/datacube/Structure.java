/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package rs.pupin.jpo.datacube;

import java.util.Collection;

/**
 *
 * @author vukm
 */
public interface Structure extends Thing{
    
    public Collection<DataSet> getAllDataSets();
    public Collection<Dimension> getDimensions();
    public Collection<Attribute> getAttributes();
    public Collection<Measure> getMeasures();
    
}
