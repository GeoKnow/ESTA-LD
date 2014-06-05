/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package rs.pupin.jpo.datacube.sparql_impl;

import org.openrdf.repository.Repository;
import rs.pupin.jpo.datacube.DataSet;
import rs.pupin.jpo.datacube.Structure;

/**
 *
 * @author vukm
 */
public class SparqlDataSet implements DataSet {
    private Repository repository;
    private String uri;
    private String graph;
    private Structure structure;
    
    private static final String QUERY = "SELECT DISTINCT ?dsd \n"
            + "FROM <@graph> \n"
            + "WHERE { \n"
            + "  <@ds> qb:structure ?dsd . \n"
            + "}";
    
    public SparqlDataSet(Repository repository, String uri, String graph){
        this.repository = repository;
        this.uri = uri;
        this.graph = graph;
        this.structure = null;
    }

    public Structure getStructure() {
        if (structure != null) return structure;
        
        //TODO implement
        return structure;
    }
    
    public void setStructure(Structure structure){
        this.structure = structure;
    }

    public String getUri() {
        return uri;
    }

    public String getGraph() {
        return graph;
    }
    
}
