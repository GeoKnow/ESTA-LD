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
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import rs.pupin.jpo.datacube.DataCubeGraph;
import rs.pupin.jpo.datacube.DataCubeRepository;
import rs.pupin.jpo.datacube.DataSet;
import rs.pupin.jpo.datacube.Structure;
import rs.pupin.jpo.esta_ld.utils.SparqlUtils;

/**
 *
 * @author vukm
 */
public class SparqlDCRepository implements DataCubeRepository{
    
    private final Repository repository;
    private Collection<DataCubeGraph> graphs;
    private Collection<DataSet> datasets;
    
    private static final String QUERY = "SELECT DISTINCT ?g ?dsd ?ds \n"
            + "WHERE { \n"
            + "  graph ?g { \n"
            + "    ?ds qb:structure ?dsd . \n"
            + "  } \n"
            + "  FILTER (regex(?g,'^http://')) \n"
            + "} order by ?g ?dsd";
    
    public SparqlDCRepository(Repository repository){
        this.repository = repository;
        this.graphs = null;
        this.datasets = null;
    }
    
    private void calculateGraphsAndDataSets(){
        try {
            RepositoryConnection conn = repository.getConnection();
            TupleQuery q = conn.prepareTupleQuery(QueryLanguage.SPARQL, SparqlUtils.PREFIXES + QUERY);
            TupleQueryResult results = q.evaluate();
            graphs = new LinkedList<DataCubeGraph>();
            datasets = new LinkedList<DataSet>();
            String lastG = null;
            String lastDSD = null;
            SparqlDCGraph dcGraph;
            SparqlStructure dcStructure = null;
            Collection<DataSet> graphDataSets = null;
            Collection<Structure> graphStructures = null;
            Collection<DataSet> structDataSets = null;
            while (results.hasNext()){
                BindingSet set = results.next();
                String g = set.getValue("g").stringValue();
                String ds = set.getValue("ds").stringValue();
                String dsd = set.getValue("dsd").stringValue();
                
                if (!g.equals(lastG)) {
                    // new Graph
                    dcGraph = new SparqlDCGraph(repository, g);
                    graphDataSets = new LinkedList<DataSet>();
                    graphStructures = new LinkedList<Structure>();
                    dcGraph.setDatasets(graphDataSets);
                    dcGraph.setStructures(graphStructures);
                    graphs.add(dcGraph);
                    // new structure
                    dcStructure = new SparqlStructure(repository, dsd, g);
                    structDataSets = new LinkedList<DataSet>();
                    dcStructure.setDatasets(structDataSets);
                    graphStructures.add(dcStructure);
                } else if (!dsd.equals(lastDSD)){
                    // new structure
                    dcStructure = new SparqlStructure(repository, dsd, g);
                    structDataSets = new LinkedList<DataSet>();
                    dcStructure.setDatasets(structDataSets);
                    graphStructures.add(dcStructure);
                }
                
                SparqlDataSet dcDataSet = new SparqlDataSet(repository, ds, g);
                dcDataSet.setStructure(dcStructure);
                graphDataSets.add(dcDataSet);
                structDataSets.add(dcDataSet);
                datasets.add(dcDataSet);
                
                lastG = g;
                lastDSD = dsd;
            }
        } catch (RepositoryException ex) {
            Logger.getLogger(SparqlDCRepository.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedQueryException ex) {
            Logger.getLogger(SparqlDCRepository.class.getName()).log(Level.SEVERE, null, ex);
        } catch (QueryEvaluationException ex) {
            Logger.getLogger(SparqlDCRepository.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Collection<DataCubeGraph> getDataCubeGraphs() {
        if (graphs != null) return graphs;
        
        calculateGraphsAndDataSets();
        return graphs;
    }

    public Collection<DataSet> getDataSets() {
        if (datasets != null) return datasets;
        
        calculateGraphsAndDataSets();
        return datasets;
    }
    
}
