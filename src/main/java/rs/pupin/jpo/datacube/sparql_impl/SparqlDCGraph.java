/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package rs.pupin.jpo.datacube.sparql_impl;

import java.util.Collection;
import org.openrdf.repository.Repository;
import rs.pupin.jpo.datacube.DataCubeGraph;
import rs.pupin.jpo.datacube.DataSet;
import rs.pupin.jpo.datacube.Structure;

/**
 *
 * @author vukm
 */
public class SparqlDCGraph implements DataCubeGraph {
    
    private final Repository repository;
    private final String uri;
    private Collection<DataSet> datasets;
    private Collection<Structure> structures;
    
    public SparqlDCGraph(Repository repository, String uri){
        this.repository = repository;
        this.uri = uri;
        
        this.datasets = null;
        this.structures = null;
    }

    public Collection<DataSet> getDataSets() {
        if (datasets != null) return datasets;
        
        // TODO implement
        return datasets;
    }

    public Collection<Structure> getStructures() {
        if (structures != null) return structures;
        
        // TODO implement
        return structures;
    }

    public void setDatasets(Collection<DataSet> datasets) {
        this.datasets = datasets;
    }

    public void setStructures(Collection<Structure> structures) {
        this.structures = structures;
    }

    public String getUri() {
        return uri;
    }
    
}
