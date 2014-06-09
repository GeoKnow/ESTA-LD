/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package rs.pupin.jpo.esta_ld;

import java.util.Collection;
import java.util.LinkedList;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sparql.SPARQLRepository;
import rs.pupin.jpo.datacube.Attribute;
import rs.pupin.jpo.datacube.DataCubeGraph;
import rs.pupin.jpo.datacube.DataSet;
import rs.pupin.jpo.datacube.Dimension;
import rs.pupin.jpo.datacube.Measure;
import rs.pupin.jpo.datacube.Thing;
import rs.pupin.jpo.datacube.sparql_impl.SparqlDCRepository;

/**
 *
 * @author vukm
 */
public class SimpleTest {
    
    public static void printThing(Thing thing){
        System.out.format("%-80s %s \n", thing.getUri(), thing.getLabel());
    }
    
    public static void main (String [] args) throws RepositoryException{
        SPARQLRepository repo = new SPARQLRepository("http://jpo.imp.bg.ac.rs/sparql");
        repo.initialize();
        SparqlDCRepository dcRepo = new SparqlDCRepository(repo);
        
        Collection<DataCubeGraph> dcGraphs = dcRepo.getDataCubeGraphs();
        LinkedList<DataCubeGraph> detailGraphs = new LinkedList<DataCubeGraph>();
        System.out.println("===== Data Cube Graphs =====");
        for (DataCubeGraph dcGraph: dcGraphs){
            if (dcGraph.getUri().contains("test.validation/noDataSet")) detailGraphs.add(dcGraph);
            System.out.format("%-40s\n", dcGraph.getUri());
        }
        
        System.out.println("===== Data Cube DataSets =====");
        for (DataCubeGraph dcGraph: detailGraphs){
            printDCGraph(dcGraph);
        }
        repo.shutDown();
    }
    
    public static void printDCGraph(DataCubeGraph dcGraph){
        for (DataSet dcDataSet: dcGraph.getDataSets()){
            printDCDataSet(dcDataSet);
        }
    }

    private static void printDCDataSet(DataSet dcDataSet) {
        System.out.println("--- DataSet");
        System.out.print("    ");
        printThing(dcDataSet);
        System.out.println("------ Dimensions");
        for (Dimension dcDimension: dcDataSet.getStructure().getDimensions()){
            printDCDimension(dcDimension);
            if (dcDimension.getCodeList() == null) continue;
            System.out.println("--------- Codes (used)");
            for (String code: dcDataSet.getValuesForDimension(dcDimension))
                System.out.println("          " + code);
        }
        System.out.println("------ Attributes");
        for (Attribute dcAttribute: dcDataSet.getStructure().getAttributes())
            printDCAttribute(dcAttribute);
        System.out.println("------ Measures");
        for (Measure dcMeasure: dcDataSet.getStructure().getMeasures())
            printDCMeasure(dcMeasure);
    }

    private static void printDCDimension(Dimension dcDimension) {
        System.out.print("       ");
        printThing(dcDimension);
        System.out.format("        isTime: %b\n        isGeo: %b\n", dcDimension.isTimeDimension(), dcDimension.isGeoDimension());
        if (dcDimension.getCodeList() == null) return;
        
//        System.out.println("--------- Codes");
//        for (String code: dcDimension.getCodeList().getAllCodes()){
//            System.out.println("          " + code);
//        }
    }

    private static void printDCAttribute(Attribute dcAttribute) {
        System.out.print("       ");
        printThing(dcAttribute);
        if (dcAttribute.getCodeList() == null) return;
        
        System.out.println("--------- Codes");
        for (String code: dcAttribute.getCodeList().getAllCodes()){
            System.out.println("          " + code);
        }
    }

    private static void printDCMeasure(Measure dcMeasure) {
        System.out.print("       ");
        printThing(dcMeasure);
        if (dcMeasure.getCodeList() == null) return;
        
        System.out.println("--------- Codes");
        for (String code: dcMeasure.getCodeList().getAllCodes()){
            System.out.println("          " + code);
        }
    }
    
}
