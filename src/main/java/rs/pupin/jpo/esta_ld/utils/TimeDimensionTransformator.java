/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.pupin.jpo.esta_ld.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 *
 * @author vukm
 */
public class TimeDimensionTransformator {
    
    private static final String PREFIXES = "PREFIX qb: <http://purl.org/linked-data/cube#> \n"
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"
            + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> \n"
            + "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> \n\n";
    
    private final Logger logger;
    
    public static enum Type {
        XSD_YEAR ("Year", "xsd:gYear", "http://www.w3.org/2001/XMLSchema#gYear"), 
        XSD_YEAR_MONTH ("Year-Month", "xsd:gYearMonth", "http://www.w3.org/2001/XMLSchema#gYearMonth"), 
        XSD_DATE ("Year-Month-Date", "xsd:date", "http://www.w3.org/2001/XMLSchema#date");
        
        private final String title;
        private final String typeTag;
        private final String longType;
        Type(String title, String typeTag, String longType) {
            this.title = title;
            this.typeTag = typeTag;
            this.longType = longType;
        }

        @Override
        public String toString() {
            return title;
        }
        
        public String getLongType() {
            return longType;
        }
    }
    
    private final Repository repository;
    private final String graph;
    private final String timeDimension;
    private Type type;
    
    private static class ObsValPair {
        public String observation;
        public String value;
        public String literal = null;
        public String type = null;
    }
    private List<ObsValPair> pairs;
    private List<ObsValPair> parsedPairs;

    public TimeDimensionTransformator(Repository repository, String graph, String timeDimension, Type type) {
        this.repository = repository;
        this.graph = graph;
        this.timeDimension = timeDimension;
        this.pairs = null;
        this.parsedPairs = null;
        this.type = type;
        this.logger = Logger.getLogger(getClass().getName());
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
    
    public void initialize() throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        RepositoryConnection conn = repository.getConnection();
        String queryString = PREFIXES + 
                "SELECT DISTINCT ?obs ?val \n"
                + "FROM <" + graph + ">"
                + "WHERE { \n"
                + "  ?obs qb:dataSet ?ds . \n"
                + "  ?obs <" + timeDimension + "> ?val . \n"
                + "} \n";
        TupleQueryResult res = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString)
                .evaluate();
        pairs = new LinkedList<ObsValPair>();
        while (res.hasNext()) {
            ObsValPair pair = new ObsValPair();
            BindingSet set = res.next();
            Value obsVal = set.getValue("obs");
            Value valVal = set.getValue("val");
            pair.observation = obsVal.stringValue();
            pair.value = valVal.stringValue();
            pairs.add(pair);
        }
        res.close();
    }
    
    public void modifyDimension() throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        RepositoryConnection conn = repository.getConnection();
        String query = PREFIXES 
                + "DELETE { graph <" + graph + "> { \n"
                + "  <" + timeDimension + "> qb:codeList ?cl . \n"
                + "  <" + timeDimension + "> rdfs:range ?range . \n"
                + "} } \n"
                + "WHERE { graph <" + graph + "> { \n"
                + "  OPTIONAL { <" + timeDimension + "> qb:codeList ?cl . } \n"
                + "  OPTIONAL { <" + timeDimension + "> rdfs:range ?range . } \n"
                + "} } \n"
                + "; \n"
                + "INSERT { graph <" + graph + "> { \n"
                + "  <" + timeDimension + "> rdfs:range " + type.typeTag + " . \n"
                + "} } \n"
                + "WHERE { graph <" + graph + "> { \n"
                + "  <" + timeDimension + "> ?p ?o . \n"
                + "} } \n";
        GraphQueryResult res = conn.prepareGraphQuery(QueryLanguage.SPARQL, query)
                .evaluate();
    }
    
    private void parse(String pattern, boolean strict) throws ParseException {
        SimpleDateFormat inputFormatter = new SimpleDateFormat(pattern);
        String outputPattern = null;
        switch (type) {
            case XSD_YEAR: outputPattern = "yyyy"; break;
            case XSD_YEAR_MONTH: outputPattern = "yyyy-MM"; break;
            case XSD_DATE: outputPattern = "yyyy-MM-dd"; break;
        }
        SimpleDateFormat outputFormatter = new SimpleDateFormat(outputPattern);
        parsedPairs = new LinkedList<ObsValPair>();
        for (ObsValPair pair: pairs) {
            ObsValPair p = new ObsValPair();
            p.observation = pair.observation;
            String parsed = null;
            try {
                parsed = outputFormatter.format(inputFormatter.parse(pair.value));
            } catch (ParseException e) {
                if (strict) throw e;
                else {
                    Calendar cal = Calendar.getInstance();
                    cal.set(0, 0, 1);
                    parsed = outputFormatter.format(cal.getTime());
                }
            }
            p.value = "\""
                    + parsed
                    + "\"^^" + type.typeTag;
            p.literal = parsed;
            p.type = type.longType;
            parsedPairs.add(p);
            logger.finest(p.value);
        }
    }
    
    public void parse(String pattern) throws ParseException {
        parse(pattern, true);
    }
    
    public void parseLean(String pattern) {
        try {
            parse(pattern, false);
        } catch (ParseException ex) {
            Logger.getLogger(TimeDimensionTransformator.class.getName()).log(Level.SEVERE, null, "This should not have happened: \t" + ex);
        }
    }
    
    public void removeOld() throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        RepositoryConnection conn = repository.getConnection();
        String query = PREFIXES 
                + "DELETE { graph <" + graph + "> { \n"
                + "  ?obs <" + timeDimension + "> ?val . \n"
                + "} } \n"
                + "WHERE { graph <" + graph + "> { \n"
                + "  ?obs qb:dataSet ?ds . \n"
                + "  ?obs <" + timeDimension + "> ?val . \n"
                + "} } \n";
        GraphQueryResult res = conn.prepareGraphQuery(QueryLanguage.SPARQL, query)
                .evaluate();
    }
    
    public void insertNew() throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        RepositoryConnection conn = repository.getConnection();
        StringBuilder builder = new StringBuilder();
        builder.append(PREFIXES);
        builder.append("INSERT DATA { graph <").append(graph).append("> { \n");
        int counter = 0;
        int limit = 1200;
        for (ObsValPair pair: parsedPairs) {
            builder.append("  <").append(pair.observation).append("> <")
                    .append(timeDimension).append("> ")
                    .append(pair.value).append(" . \n");
            if (++counter % limit == 0) {
                logger.fine("Sending query, counter is " + counter);
                // close the query and evaluate
                builder.append("} } \n");
                conn.prepareGraphQuery(QueryLanguage.SPARQL, builder.toString())
                        .evaluate();
                // start a new query
                builder = new StringBuilder();
                builder.append(PREFIXES);
                builder.append("INSERT DATA { graph <").append(graph).append("> { \n");
            }
            
        }
        if (counter % limit != 0){
            logger.fine("Sending query, counter is " + counter);
            builder.append("} } \n");
            conn.prepareGraphQuery(QueryLanguage.SPARQL, builder.toString())
                    .evaluate();
        }
        logger.fine("Finished inserting!!!");
    }
    
    public void insertNewAlt() throws RepositoryException {
        RepositoryConnection conn = repository.getConnection();
        List<Statement> stmts = new LinkedList<Statement>();
        ValueFactory factory = conn.getValueFactory();
        for (ObsValPair pair: parsedPairs) {
            stmts.add(factory.createStatement(
                    factory.createURI(pair.observation),
                    factory.createURI(timeDimension), 
                    factory.createLiteral(pair.literal, factory.createURI(pair.type))
            ));
        }
        if (stmts.isEmpty()) return;
        conn.add(stmts, factory.createURI(graph));
    }
    
}
