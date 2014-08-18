/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package rs.pupin.jpo.dsdrepo;

import java.util.Collection;
import java.util.LinkedList;

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
    
    public static String qCodesTypes(String compUri, String ds, String graph){
        StringBuilder builder = createBuilderWithPrefixes();
        builder.append("SELECT DISTINCT ?val isiri(?val) as ?iri datatype(?val) as ?datatype \n");
        builder.append("FROM <@g> \n");
        builder.append("WHERE { \n");
        builder.append("  ?obs qb:dataSet <@ds> . \n");
        builder.append("  ?obs <@comp> ?val . \n");
        builder.append("}");
        return builder.toString().replace("@g", graph).replace("@comp", compUri).replace("@ds", ds);
    }
    
    public static String qResourcePorperties(String resource, String graph){
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT ?p ?o \n");
        builder.append("FROM <@g> \n");
        builder.append("WHERE { \n");
        builder.append("  <@r> ?p ?o . \n");
        builder.append("} ");
        return builder.toString().replace("@g", graph).replace("@r", resource);
    }
    
    public static Collection<String> qCopyDSD(String dsd, String sourceGraph, String targetGraph){
        LinkedList<String> list = new LinkedList<String>();
        // insert dsd info
        StringBuilder builder = createBuilderWithPrefixes();
        builder.append("INSERT INTO GRAPH <@target> { \n");
        builder.append("  <@dsd> ?p ?o . \n");
        builder.append("} \n");
        builder.append("WHERE { \n");
        builder.append("  GRAPH <@source> { <@dsd> ?p ?o . } \n");
        builder.append("}");
        list.add(builder.toString().replace("@dsd", dsd).replace("@target", targetGraph).replace("@source", sourceGraph));
        // insert component specification info
        builder = createBuilderWithPrefixes();
        builder.append("INSERT INTO GRAPH <@target> { \n");
        builder.append("  ?cs ?p ?o . \n");
        builder.append("} \n");
        builder.append("WHERE { \n");
        builder.append("  GRAPH <@source> { \n");
        builder.append("    <@dsd> qb:component ?cs . \n");
        builder.append("    ?cs ?p ?o . \n");
        builder.append("  } \n");
        builder.append("}");
        list.add(builder.toString().replace("@dsd", dsd).replace("@target", targetGraph).replace("@source", sourceGraph));
        // insert component property and code/code list info
        builder = createBuilderWithPrefixes();
        builder.append("INSERT INTO GRAPH <@target> { \n");
        builder.append("  ?cp ?p ?o . \n");
        builder.append("  ?list ?list_p ?list_o . \n");
        builder.append("  ?list_ls ?list_lp ?list . \n");
        builder.append("  ?code ?code_p ?code_o . \n");
        builder.append("  ?code_t ?code_t_p ?code_t_o . \n");
        builder.append("} \n");
        builder.append("WHERE { \n");
        builder.append("  GRAPH <@source> { \n");
        builder.append("    <@dsd> qb:component ?cs . \n");
        builder.append("    { \n");
        builder.append("      { ?cs qb:componentProperty ?cp . } \n");
        builder.append("      UNION { ?cs qb:dimension ?cp . } \n");
        builder.append("      UNION { ?cs qb:measure ?cp . } \n");
        builder.append("      UNION { ?cs qb:attribute ?cp . } \n");
        builder.append("    } \n");
        builder.append("    ?cp ?p ?o . \n");
        builder.append("    OPTIONAL { \n");
        builder.append("      ?cp qb:codeList ?list . \n");
        builder.append("      ?list ?list_p ?list_o . \n");
        builder.append("      ?list_ls ?list_lp ?list . \n");
        builder.append("      ?code skos:inScheme ?list . \n");
        builder.append("      ?code ?code_p ?code_o . \n");
        builder.append("      OPTIONAL { \n");
        builder.append("        ?code a ?code_t . \n");
        builder.append("        ?code_t ?code_t_p ?code_t_o . \n");
        builder.append("      } \n");
        builder.append("    } \n");
        builder.append("  } \n");
        builder.append("} ");
        list.add(builder.toString().replace("@dsd", dsd).replace("@target", targetGraph).replace("@source", sourceGraph));
        return list;
    }
    
}
