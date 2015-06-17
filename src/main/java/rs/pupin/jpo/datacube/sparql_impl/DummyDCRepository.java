/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.pupin.jpo.datacube.sparql_impl;

import java.util.Collection;
import java.util.LinkedList;
import rs.pupin.jpo.datacube.DataCubeGraph;
import rs.pupin.jpo.datacube.DataCubeRepository;
import rs.pupin.jpo.datacube.DataSet;

/**
 *
 * @author vukm
 */
public class DummyDCRepository implements DataCubeRepository{

    public Collection<DataCubeGraph> getDataCubeGraphs() {
        return new LinkedList<DataCubeGraph>();
    }

    public Collection<DataSet> getDataSets() {
        return new LinkedList<DataSet>();
    }
    
}
