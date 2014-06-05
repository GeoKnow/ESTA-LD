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
public interface DataCubeRepository {
    
    public Collection<DataCubeGraph> getDataCubeGraphs();
    public Collection<DataSet> getDataSets();
    
}
