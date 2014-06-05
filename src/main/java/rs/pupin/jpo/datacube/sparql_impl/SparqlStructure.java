/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package rs.pupin.jpo.datacube.sparql_impl;

import java.util.Collection;
import org.openrdf.repository.Repository;
import rs.pupin.jpo.datacube.Attribute;
import rs.pupin.jpo.datacube.DataSet;
import rs.pupin.jpo.datacube.Dimension;
import rs.pupin.jpo.datacube.Measure;
import rs.pupin.jpo.datacube.Structure;

/**
 *
 * @author vukm
 */
public class SparqlStructure implements Structure {
    private Repository repository;
    private String uri;
    private String graph;
    
    private Collection<DataSet> datasets;
    private Collection<Dimension> dimensions;
    private Collection<Attribute> attributes;
    private Collection<Measure> measures;
    
    public SparqlStructure(Repository repository, String uri, String graph){
        this.repository = repository;
        this.uri = uri;
        this.graph = graph;
        this.datasets = null;
        this.dimensions = null;
        this.attributes = null;
        this.measures = null;
    }

    public Collection<DataSet> getAllDataSets() {
        if (datasets != null) return datasets;
        
        //TODO: implement
        return datasets;
    }

    public Collection<Dimension> getDimensions() {
        if (dimensions != null) return dimensions;
        
        //TODO: implement
        return dimensions;
    }

    public Collection<Attribute> getAttributes() {
        if (attributes != null) return attributes;
        
        //TODO: implement
        return attributes;
    }

    public Collection<Measure> getMeasures() {
        if (measures != null) return measures;
        
        //TODO: implement
        return measures;
    }

    public String getUri() {
        return uri;
    }

    public String getGraph() {
        return graph;
    }
    
}
