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
public interface DataCubeGraph {
    
    public Collection<DataSet> getDataSets();
    public Collection<Structure> getStructures();
    public String getUri();
    
}
