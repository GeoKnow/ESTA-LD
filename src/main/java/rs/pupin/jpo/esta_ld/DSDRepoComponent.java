/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package rs.pupin.jpo.esta_ld;

import com.vaadin.data.Property;
import com.vaadin.data.util.PropertyFormatter;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;
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
import org.openrdf.repository.sparql.SPARQLRepository;
import rs.pupin.jpo.datacube.DataCubeGraph;
import rs.pupin.jpo.datacube.DataSet;
import rs.pupin.jpo.datacube.Structure;
import rs.pupin.jpo.datacube.sparql_impl.SparqlDCRepository;
import rs.pupin.jpo.dsdrepo.DSDRepo;
import rs.pupin.jpo.dsdrepo.DSDRepoUtils;

/**
 *
 * @author vukm
 */
public class DSDRepoComponent extends CustomComponent {
    private Repository repository;
    private VerticalLayout mainLayout;
    private HorizontalLayout datasetLayout;
    private String endpoint;
    private SparqlDCRepository dcRepo;
    private HorizontalLayout contentLayout;
    private ComboBox selectDataSet;
    private DataCubeGraph graph;
    private DSDRepo dsdRepo;
    private Tree dataTree;
    private Tree repoTree;
    
    private String dataGraph;
    private String dataset;
    private String repoGraph;
    
    public DSDRepoComponent(Repository repository){
        this.repository = repository;
        
        mainLayout = new VerticalLayout();
        setCompositionRoot(mainLayout);
    }
    
    public DSDRepoComponent(){
        repository = new SPARQLRepository(endpoint);
        try {
            repository.initialize();
        } catch (RepositoryException ex) {
            Logger.getLogger(EstaLdComponent.class.getName()).log(Level.SEVERE, null, ex);
        }
        dcRepo = new SparqlDCRepository(repository);
        
        mainLayout = new VerticalLayout();
        mainLayout.setWidth("100%");
        mainLayout.setSpacing(true);
        datasetLayout = new HorizontalLayout();
        datasetLayout.setSpacing(true);
        datasetLayout.setWidth("100%");
        createDataSetLayout();
        mainLayout.addComponent(datasetLayout);
        
        mainLayout.addComponent(new Label("<hr/>", Label.CONTENT_XHTML));
        
        contentLayout = new HorizontalLayout();
        contentLayout.setSizeFull();
        contentLayout.setWidth("100%");
        contentLayout.setSpacing(true);
        mainLayout.addComponent(contentLayout);
        setCompositionRoot(mainLayout);
    }

    private void createDataSetLayout() {
        Label lbl = new Label("Choose dataset: ");
        lbl.setSizeUndefined();
        datasetLayout.addComponent(lbl);
        datasetLayout.setExpandRatio(lbl, 0.0f);
        selectDataSet = new ComboBox(null, graph.getDataSets());
        selectDataSet.setImmediate(true);
        selectDataSet.setNewItemsAllowed(false);
        selectDataSet.setNullSelectionAllowed(false);
        selectDataSet.setSizeUndefined();
        selectDataSet.setWidth("100%");
        datasetLayout.addComponent(selectDataSet);
        datasetLayout.setExpandRatio(selectDataSet, 2.0f);
        
        selectDataSet.addListener(new Property.ValueChangeListener(){
            public void valueChange(Property.ValueChangeEvent event) {
                DataSet ds = (DataSet) event.getProperty().getValue();
                refreshContent(ds);
            }
        });
    }
    
    private void populateDataTree(){
        try {
            RepositoryConnection con = repository.getConnection();
            TupleQuery q = con.prepareTupleQuery(QueryLanguage.SPARQL, DSDRepoUtils.qPossibleComponents(dataGraph, dataset));
            TupleQueryResult res = q.evaluate();
            while (res.hasNext()){
                BindingSet set = res.next();
                String component = set.getValue("comp").stringValue();
                dataTree.addItem(component);
            }
        } catch (RepositoryException ex) {
            Logger.getLogger(DSDRepoComponent.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedQueryException ex) {
            Logger.getLogger(DSDRepoComponent.class.getName()).log(Level.SEVERE, null, ex);
        } catch (QueryEvaluationException ex) {
            Logger.getLogger(DSDRepoComponent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void populateRepoTree(){
        try {
            RepositoryConnection con = repository.getConnection();
            TupleQuery q = con.prepareTupleQuery(QueryLanguage.SPARQL, DSDRepoUtils.qMatchingStructures(dataGraph, dataset, repoGraph));
            TupleQueryResult res = q.evaluate();
            while (res.hasNext()){
                BindingSet set = res.next();
                String dsd = set.getValue("dsd").stringValue();
                repoTree.addItem(dsd);
            }
        } catch (RepositoryException ex) {
            Logger.getLogger(DSDRepoComponent.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedQueryException ex) {
            Logger.getLogger(DSDRepoComponent.class.getName()).log(Level.SEVERE, null, ex);
        } catch (QueryEvaluationException ex) {
            Logger.getLogger(DSDRepoComponent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void refreshContent(DataSet ds){
        Structure struct = ds.getStructure();
        
        contentLayout.removeAllComponents();;
        dataTree = new Tree("Dataset");
        populateDataTree();
        contentLayout.addComponent(dataTree);
        repoTree = new Tree("Structures");
        populateRepoTree();
        contentLayout.addComponent(repoTree);
        
        if (struct != null) {
            if (dsdRepo.containsDSD(struct.getUri()))
                if (dsdRepo.isIdenticalDSD(graph.getUri(), struct.getUri())) {
                    everythingFine(ds);
                }
                else { 
                    changeUriAndPut(ds);
                }
            else {
                putInRepo(ds);
            }
        }
        else {
            // Find potential components
            // Find DSDs of those components
            // Find common DSDs for above
            // Check if the filtered DSDs have other required attributes
            
            // on the right DSDs from the repo
            // dims, attrs and meas in a tree in different colors
            // on click show their code lists
            
            // on the left current dataset
            // dims, attrs and meas
            // on click show used values
        }
    }
    
    private void everythingFine(DataSet ds){
        // TODO: implement
    }
    
    private void changeUriAndPut(DataSet ds){
        // TODO: implement
    }
    
    private void putInRepo(DataSet ds){
        // TODO: implement
    }
    
}
