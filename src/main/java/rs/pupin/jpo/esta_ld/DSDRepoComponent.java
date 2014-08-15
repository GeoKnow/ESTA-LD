/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package rs.pupin.jpo.esta_ld;

import com.vaadin.data.Property;
import com.vaadin.event.Action;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.MouseEvents;
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
import rs.pupin.jpo.datacube.Attribute;
import rs.pupin.jpo.datacube.DataCubeGraph;
import rs.pupin.jpo.datacube.DataSet;
import rs.pupin.jpo.datacube.Dimension;
import rs.pupin.jpo.datacube.Measure;
import rs.pupin.jpo.datacube.Structure;
import rs.pupin.jpo.datacube.sparql_impl.SparqlDCGraph;
import rs.pupin.jpo.datacube.sparql_impl.SparqlDCRepository;
import rs.pupin.jpo.datacube.sparql_impl.SparqlStructure;
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
    
    private static Action ACTION1 = new Action("Action 1");
    private static Action ACTION2 = new Action("Action 2");
    private static Action [] ACTIONS = new Action[] { ACTION1,ACTION2 };
    
    public DSDRepoComponent(Repository repository){
        this.repository = repository;
        
        mainLayout = new VerticalLayout();
        setCompositionRoot(mainLayout);
    }
    
    public DSDRepoComponent(){
        endpoint = "http://localhost:8890/sparql";
        
        repository = new SPARQLRepository(endpoint);
        try {
            repository.initialize();
        } catch (RepositoryException ex) {
            Logger.getLogger(EstaLdComponent.class.getName()).log(Level.SEVERE, null, ex);
        }
        dcRepo = new SparqlDCRepository(repository);
        graph = new SparqlDCGraph(repository, "http://validation-test/regular-all/");
        dataGraph = graph.getUri();
        repoGraph = graph.getUri();
        
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
        dataTree.removeAllItems();
        try {
            RepositoryConnection con = repository.getConnection();
            TupleQuery q = con.prepareTupleQuery(QueryLanguage.SPARQL, DSDRepoUtils.qPossibleComponents(dataGraph, dataset));
            TupleQueryResult res = q.evaluate();
            while (res.hasNext()){
                BindingSet set = res.next();
                String component = set.getValue("comp").stringValue();
                dataTree.addItem(component);
            }
            dataTree.addListener(new ItemClickEvent.ItemClickListener() {
                public void itemClick(ItemClickEvent event) {
                    if (event.getButton() != MouseEvents.ClickEvent.BUTTON_RIGHT) return;
                    // TODO: add code for the context menu here
                }
            });
            dataTree.addActionHandler(new Action.Handler() {

                public Action[] getActions(Object target, Object sender) {
                    return ACTIONS;
                }

                public void handleAction(Action action, Object sender, Object target) {
                    if (action == ACTION1)
                        getWindow().showNotification("Chose Action 1");
                    else if (action == ACTION2)
                        getWindow().showNotification("Clicked Action 2");
                }
            });
        } catch (RepositoryException ex) {
            Logger.getLogger(DSDRepoComponent.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedQueryException ex) {
            Logger.getLogger(DSDRepoComponent.class.getName()).log(Level.SEVERE, null, ex);
        } catch (QueryEvaluationException ex) {
            Logger.getLogger(DSDRepoComponent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void populateRepoTree(){
        repoTree.removeAllItems();
        try {
            RepositoryConnection con = repository.getConnection();
            TupleQuery q = con.prepareTupleQuery(QueryLanguage.SPARQL, DSDRepoUtils.qMatchingStructures(dataGraph, dataset, repoGraph));
            TupleQueryResult res = q.evaluate();
            getWindow().showNotification("Has next: " + res.hasNext());
            while (res.hasNext()){
                BindingSet set = res.next();
                String dsd = set.getValue("dsd").stringValue();
                Structure structure = new SparqlStructure(repository, dsd, graph.getUri());
                
                repoTree.addItem(structure);
                int sizeDimensions = structure.getDimensions().size();
                String dimString = "Dimensions (" + sizeDimensions + ")";
                repoTree.addItem(dimString);
                repoTree.setParent(dimString, structure);
                for (Dimension dim: structure.getDimensions()){
                    repoTree.addItem(dim);
                    repoTree.setParent(dim, dimString);
                }
                int sizeAttributes = structure.getAttributes().size();
                String attrString = "Attributes (" + sizeAttributes + ")";
                repoTree.addItem(attrString);
                repoTree.setParent(attrString, structure);
                for (Attribute attr: structure.getAttributes()){
                    repoTree.addItem(attr);
                    repoTree.setParent(attr, attrString);
                }
                int sizeMeasures = structure.getMeasures().size();
                String measString = "Measures (" + sizeMeasures + ")";
                repoTree.addItem(measString);
                repoTree.setParent(measString, structure);
                for (Measure meas: structure.getMeasures()){
                    repoTree.addItem(meas);
                    repoTree.setParent(meas, measString);
                }
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
        
        dataset = ds.getUri();
        contentLayout.removeAllComponents();;
        dataTree = new Tree("Dataset");
        dataTree.setNullSelectionAllowed(true);
        dataTree.setImmediate(true);
        populateDataTree();
        contentLayout.addComponent(dataTree);
        repoTree = new Tree("Matching Structures");
        repoTree.setNullSelectionAllowed(true);
        repoTree.setImmediate(true);
        populateRepoTree();
        contentLayout.addComponent(repoTree);
        
        if (struct != null) {
//            if (dsdRepo.containsDSD(struct.getUri()))
//                if (dsdRepo.isIdenticalDSD(graph.getUri(), struct.getUri())) {
//                    everythingFine(ds);
//                }
//                else { 
//                    changeUriAndPut(ds);
//                }
//            else {
//                putInRepo(ds);
//            }
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
