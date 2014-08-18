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
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openrdf.model.Value;
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
import rs.pupin.jpo.datacube.CodeList;
import rs.pupin.jpo.datacube.ComponentProperty;
import rs.pupin.jpo.datacube.DataCubeGraph;
import rs.pupin.jpo.datacube.DataSet;
import rs.pupin.jpo.datacube.Dimension;
import rs.pupin.jpo.datacube.Measure;
import rs.pupin.jpo.datacube.Structure;
import rs.pupin.jpo.datacube.sparql_impl.SparqlDCGraph;
import rs.pupin.jpo.datacube.sparql_impl.SparqlDCRepository;
import rs.pupin.jpo.datacube.sparql_impl.SparqlStructure;
import rs.pupin.jpo.dsdrepo.CodeDatatypeTreeElement;
import rs.pupin.jpo.dsdrepo.DSDRepo;
import rs.pupin.jpo.dsdrepo.DSDRepoUtils;
import rs.pupin.jpo.dsdrepo.CountingTreeHeader;

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
    
    private static Action ACTION_1 = new Action("Set as Dimension");
    private static Action ACTION_2 = new Action("Set as Measure");
    private static Action ACTION_3 = new Action("Set as Attribute");
    private static Action ACTION_4 = new Action("Set as Undefined");

    private static Action [] ACTIONS = new Action[] { ACTION_1, ACTION_2, ACTION_3, ACTION_4 };
    
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
    
    private CountingTreeHeader createCountingTreeHeader(Tree t, String header){
        CountingTreeHeader h = new CountingTreeHeader(t, header);
        t.addItem(h);
        return h;
    }
    
    private void populateDataTree(){
        dataTree.removeAllItems();
        try {
            RepositoryConnection con = repository.getConnection();
            TupleQuery q = con.prepareTupleQuery(QueryLanguage.SPARQL, DSDRepoUtils.qPossibleComponents(dataGraph, dataset));
            TupleQueryResult res = q.evaluate();
            
            final CountingTreeHeader dim = createCountingTreeHeader(dataTree, "Dimensions");
            final CountingTreeHeader meas = createCountingTreeHeader(dataTree, "Measures");
            final CountingTreeHeader attr = createCountingTreeHeader(dataTree, "Attributes");
            final CountingTreeHeader undef = createCountingTreeHeader(dataTree, "Undefined");
            
            while (res.hasNext()){
                BindingSet set = res.next();
                String component = set.getValue("comp").stringValue();
//                undef.addElement(component);
                dataTree.addItem(component);
                dataTree.setParent(component, undef);
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
                    if (!(target instanceof String)) return;
                    String e = (String) target;
                    if (action == ACTION_1) {
                        dataTree.setParent(e, dim);
                    }
                    else if (action == ACTION_2){
                        dataTree.setParent(e, meas);
                    } else if (action == ACTION_3){
                        dataTree.setParent(e, attr);
                    } else if (action == ACTION_4){
                        dataTree.setParent(e, undef);
                    }
                }
            });
            dataTree.addListener(new Tree.ExpandListener() {
                public void nodeExpand(Tree.ExpandEvent event) {
                    Object obj = event.getItemId();
                    if (!(obj instanceof String)) return;
                    if (dataTree.hasChildren(obj)) return;
                    String component = (String)obj;
                    
                    try {
                        RepositoryConnection con = repository.getConnection();
                        TupleQuery q = con.prepareTupleQuery(QueryLanguage.SPARQL, DSDRepoUtils.qCodesTypes(component, dataset, dataGraph));
                        TupleQueryResult res = q.evaluate();
                        Collection<String> values = new LinkedList<String>();
                        Collection<String> datatypes = new HashSet<String>();
                        int count = 0;
                        
                        while (res.hasNext()){
                            BindingSet set = res.next();
                            String val = set.getValue("val").stringValue();
                            values.add(val);
                            String iri = set.getValue("iri").stringValue();
                            Value datatypeObj = set.getValue("datatype");
                            if (datatypeObj != null) datatypes.add(datatypeObj.stringValue());
                            count += Integer.valueOf(iri).intValue();
                        }
                        
                        CodeDatatypeTreeElement elem = null;
                        if (count > 0 && count < values.size()) {
                            elem = new CodeDatatypeTreeElement("", false, 1);
                            dataTree.addItem(elem);
                            dataTree.setParent(elem, obj);
                        }
                        else if (count==0 && datatypes.size()!=1) {
                            elem = new CodeDatatypeTreeElement("", false, 1);
                            dataTree.addItem(elem);
                            dataTree.setParent(elem, obj);
                        }
                        else if (count==0){
                            CountingTreeHeader countTypes = createCountingTreeHeader(dataTree, "Datatypes");
                            dataTree.setParent(countTypes, obj);
                            String e = datatypes.iterator().next();
                            elem = new CodeDatatypeTreeElement(e, false, 0);
                            dataTree.addItem(elem);
                            dataTree.setParent(elem, countTypes);
                        }
                        else if (count == values.size()){
                            CountingTreeHeader countValues = createCountingTreeHeader(dataTree, "Codes");
                            dataTree.setParent(countValues, obj);
                            for (String element: values){
                                CodeDatatypeTreeElement e = new CodeDatatypeTreeElement(element, true, 0);
                                dataTree.addItem(e);
                                dataTree.setParent(e, countValues);
                            }
                        } else {
                            elem = new CodeDatatypeTreeElement("", false, 3);
                            dataTree.addItem(elem);
                            dataTree.setParent(elem, obj);
                        }
                    } catch (RepositoryException ex) {
                        Logger.getLogger(DSDRepoComponent.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (MalformedQueryException ex) {
                        Logger.getLogger(DSDRepoComponent.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (QueryEvaluationException ex) {
                        Logger.getLogger(DSDRepoComponent.class.getName()).log(Level.SEVERE, null, ex);
                    }
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
            
            while (res.hasNext()){
                BindingSet set = res.next();
                String dsd = set.getValue("dsd").stringValue();
                final Structure structure = new SparqlStructure(repository, dsd, graph.getUri());
                           
                repoTree.addItem(structure);
                CountingTreeHeader dimCountHeader = createCountingTreeHeader(repoTree, "Dimensions");
                repoTree.setParent(dimCountHeader, structure);
                CountingTreeHeader measCountHeader = createCountingTreeHeader(repoTree, "Measures");
                repoTree.setParent(measCountHeader, structure);
                CountingTreeHeader attrCountHeader = createCountingTreeHeader(repoTree, "Attributes");
                repoTree.setParent(attrCountHeader, structure);
                
                for (Dimension dim: structure.getDimensions()){
                    repoTree.addItem(dim);
                    repoTree.setParent(dim, dimCountHeader);
                }
                for (Attribute attr: structure.getAttributes()){
                    repoTree.addItem(attr);
                    repoTree.setParent(attr, attrCountHeader);
                }
                for (Measure meas: structure.getMeasures()){
                    repoTree.addItem(meas);
                    repoTree.setParent(meas, measCountHeader);
                }
            }
        } catch (RepositoryException ex) {
            Logger.getLogger(DSDRepoComponent.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedQueryException ex) {
            Logger.getLogger(DSDRepoComponent.class.getName()).log(Level.SEVERE, null, ex);
        } catch (QueryEvaluationException ex) {
            Logger.getLogger(DSDRepoComponent.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        repoTree.addListener(new Tree.ExpandListener() {
            public void nodeExpand(Tree.ExpandEvent event) {
                Object id = event.getItemId();
                if (id instanceof ComponentProperty && !repoTree.hasChildren(id)) {
                    ComponentProperty prop = (ComponentProperty) id;
                    CodeList codeList = prop.getCodeList();
                    if (codeList != null) {
                        CountingTreeHeader codeCountingHeader = createCountingTreeHeader(repoTree, "Codes");
                        repoTree.setParent(codeCountingHeader, id);
                        for (String code: codeList.getAllCodes()){
                            CodeDatatypeTreeElement elem = new CodeDatatypeTreeElement(code, true, 0);
                            repoTree.addItem(elem);
                            repoTree.setParent(elem, codeCountingHeader);
                        }
                    } else {
                        CountingTreeHeader datatypes = createCountingTreeHeader(repoTree, "Datatypes");
                        repoTree.setParent(datatypes, id);
                        CodeDatatypeTreeElement elem = new CodeDatatypeTreeElement(prop.getRange(), false, 0);
                        repoTree.addItem(elem);
                        repoTree.setParent(elem, datatypes);
                    }
                }
            }
        });
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
