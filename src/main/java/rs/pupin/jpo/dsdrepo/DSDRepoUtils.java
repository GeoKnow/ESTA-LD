/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package rs.pupin.jpo.dsdrepo;

/**
 *
 * @author vukm
 */
public class DSDRepoUtils {
    
    private static StringBuilder createBuilderWithPrefixes(){
        StringBuilder builder = new StringBuilder();
        builder.append("PREFIX qb: <http://purl.org/linked-data/cube#> \n");
        builder.append("PREFIX skos: <http://www.w3.org/2004/02/skos/core#> \n");
        builder.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n");
        builder.append("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n");
        builder.append("PREFIX owl: <http://www.w3.org/2002/07/owl#> \n");
        builder.append("PREFIX dct: <http://purl.org/dc/terms/> \n");
        return builder;
    }
    
    public static String qPossibleComponents(String sGraph, String ds){
        StringBuilder builder = createBuilderWithPrefixes();
        builder.append("SELECT DISTINCT ?comp \n");
        builder.append("FROM <@sGraph> \n");
        builder.append("WHERE { \n");
        builder.append("  ?obs qb:dataSet <@ds> . \n");
        builder.append("  ?obs ?comp [] . \n");
        builder.append("  FILTER(NOT(regex(str(?comp),'^http://purl.org/linked-data/cube#')) \n");
        builder.append("    AND NOT(regex(str(?comp),'^http://www.w3.org/'))) . \n");
        builder.append("} \n");
        return builder.toString().replace("@sGraph", sGraph).replace("@ds", ds);
    }
    
    public static String qMatchingStructures(String dataGraph, String ds, String repoGraph){
        StringBuilder builder = createBuilderWithPrefixes();
        builder.append("SELECT DISTINCT ?dsd \n");
        builder.append("WHERE { \n");
        builder.append("  GRAPH <@repoGraph> { ?dsd a qb:DataStructureDefinition . } \n");
        builder.append("  FILTER NOT EXISTS { \n");
        builder.append("    GRAPH <@dataGraph> {  \n");
        builder.append("      ?obs qb:dataSet <@ds> . \n");
        builder.append("      ?obs ?comp [] . \n");
        builder.append("      FILTER(NOT(regex(str(?comp),'^http://purl.org/linked-data/cube#')) \n");
        builder.append("        AND NOT(regex(str(?comp),'^http://www.w3.org/'))) . \n");
        builder.append("    } \n");
        builder.append("    FILTER NOT EXISTS { GRAPH <@repoGraph> { \n");
        builder.append("      ?dsd qb:component ?cs . \n");
        builder.append("      ?cs ?prop ?comp . \n");
        builder.append("    } } \n");
        builder.append("  } \n");
        builder.append("  FILTER NOT EXISTS { \n");
        builder.append("    GRAPH <@repoGraph> { \n");
        builder.append("      ?dsd qb:component ?cs . \n");
        builder.append("      ?cs ?prop ?comp . \n");
        builder.append("      FILTER(?prop IN (qb:dimension,qb:measure,qb:attribute)) . \n");
        builder.append("    } \n");
        builder.append("    FILTER NOT EXISTS { \n");
        builder.append("      GRAPH <@dataGraph> { \n");
        builder.append("        ?obs qb:dataSet <@ds> . \n");
        builder.append("        ?obs ?comp [] . \n");
        builder.append("      } \n");
        builder.append("    } \n");
        builder.append("  } \n");
        builder.append("} \n");
        return builder.toString().replace("@dataGraph", dataGraph)
                .replace("@ds", ds).replace("@repoGraph", repoGraph);
    }
    
    public static String qPossibleValues(String compUri, String ds, String graph) {
        StringBuilder builder = createBuilderWithPrefixes();
        builder.append("SELECT DISTINCT ?val \n");
        builder.append("FROM <@g> \n");
        builder.append("WHERE { \n");
        builder.append("  ?obs qb:dataSet td:dataset1 . \n");
        builder.append("  ?obs ts:refArea ?val . \n");
        builder.append("  FILTER NOT EXISTS { \n");
        builder.append("    ?obs ts:refArea ?v2 . \n");
        builder.append("    FILTER(NOT(isIRI(?v2))) \n");
        builder.append("  } \n");
        builder.append("} \n");
        return builder.toString().replace("@g", graph).replace("@comp", compUri).replace("@ds", ds);
    }
    
}
