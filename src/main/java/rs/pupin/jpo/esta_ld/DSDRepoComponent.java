/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package rs.pupin.jpo.esta_ld;

import com.vaadin.data.Property;
import com.vaadin.event.Action;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
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
import org.openrdf.query.GraphQuery;
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
    private String highlighted;
    
    private ThemeResource icon_structure = new ThemeResource("icons/icon_structure.png");
    private ThemeResource icon_property = new ThemeResource("icons/icon_property.png");
    
    private static Action ACTION_SET_AS_DIM = new Action("Set as Dimension");
    private static Action ACTION_SET_AS_MEAS = new Action("Set as Measure");
    private static Action ACTION_SET_AS_ATTR = new Action("Set as Attribute");
    private static Action ACTION_SET_AS_UNDEF = new Action("Set as Undefined");
    private static Action ACTION_HIGHLIGHT_MATCHING = new Action("Highlight Matching");
    private static Action ACTION_SET_AS_DSD = new Action("Set as qb:DataStructureDefinition");
    
    private static Action ACTION_EXPAND_ALL = new Action("Expand All");
    private static Action ACTION_COLLAPSE_ALL = new Action("Collapse All");
    private static Action [] ACTIONS_NAVI = new Action [] { ACTION_EXPAND_ALL, ACTION_COLLAPSE_ALL };

    private static Action [] ACTIONS = new Action[] { 
        ACTION_SET_AS_DIM, ACTION_SET_AS_MEAS, ACTION_SET_AS_ATTR, 
        ACTION_SET_AS_UNDEF, ACTION_HIGHLIGHT_MATCHING };
    
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
//        graph = new SparqlDCGraph(repository, "http://validation-test/regular-all/");
        graph = new SparqlDCGraph(repository, "http://regular-data-replica/");
        dataGraph = graph.getUri();
        repoGraph = "http://validation-test/regular-dsd/";
        
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
    
    private String generatePropertiesTable(String uri, String graph){
        StringBuilder builder = new StringBuilder();
        try {
            RepositoryConnection conn = repository.getConnection();
            TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, DSDRepoUtils.qResourcePorperties(uri, graph));
            TupleQueryResult res = query.evaluate();
            builder.append("<table style=\"border-spacing:7px\">");
            while (res.hasNext()) {
                BindingSet set = res.next();
                builder.append("<tr><td>");
                builder.append(set.getValue("p").stringValue());
                builder.append("</td><td>");
                builder.append(set.getValue("o").stringValue());
                builder.append("</td></tr>");
            }
            builder.append("</table>");
        } catch (RepositoryException ex) {
            Logger.getLogger(DSDRepoComponent.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedQueryException ex) {
            Logger.getLogger(DSDRepoComponent.class.getName()).log(Level.SEVERE, null, ex);
        } catch (QueryEvaluationException ex) {
            Logger.getLogger(DSDRepoComponent.class.getName()).log(Level.SEVERE, null, ex);
        }
        return builder.toString();
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
                addItemProperty(dataTree, component);
                dataTree.setParent(component, undef);
            }
            
            dataTree.addActionHandler(new Action.Handler() {

                public Action[] getActions(Object target, Object sender) {
                    if (!(target instanceof String)) return null;
                    return ACTIONS;
                }

                public void handleAction(Action action, Object sender, Object target) {
                    if (!(target instanceof String)) return;
                    String e = (String) target;
                    if (action == ACTION_SET_AS_DIM) {
                        dataTree.setParent(e, dim);
                    }
                    else if (action == ACTION_SET_AS_MEAS){
                        dataTree.setParent(e, meas);
                    } else if (action == ACTION_SET_AS_ATTR){
                        dataTree.setParent(e, attr);
                    } else if (action == ACTION_SET_AS_UNDEF){
                        dataTree.setParent(e, undef);
                    } else if (action == ACTION_HIGHLIGHT_MATCHING){
                        // notify repo tree about the change
//                        highlighted = e;
                        // update repo tree
//                        repoTree.containerItemSetChange(null);
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
                            dataTree.setChildrenAllowed(elem, false);
                        }
                        else if (count==0 && datatypes.size()!=1) {
                            elem = new CodeDatatypeTreeElement("", false, 1);
                            dataTree.addItem(elem);
                            dataTree.setParent(elem, obj);
                            dataTree.setChildrenAllowed(elem, false);
                        }
                        else if (count==0){
                            CountingTreeHeader countTypes = createCountingTreeHeader(dataTree, "Datatypes");
                            dataTree.setParent(countTypes, obj);
                            String e = datatypes.iterator().next();
                            elem = new CodeDatatypeTreeElement(e, false, 0);
                            dataTree.addItem(elem);
                            dataTree.setParent(elem, countTypes);
                            dataTree.setChildrenAllowed(elem, false);
                        }
                        else if (count == values.size()){
                            CountingTreeHeader countValues = createCountingTreeHeader(dataTree, "Codes");
                            dataTree.setParent(countValues, obj);
                            for (String element: values){
                                CodeDatatypeTreeElement e = new CodeDatatypeTreeElement(element, true, 0);
                                dataTree.addItem(e);
                                dataTree.setParent(e, countValues);
                                dataTree.setChildrenAllowed(e, false);
                            }
                        } else {
                            elem = new CodeDatatypeTreeElement("", false, 3);
                            dataTree.addItem(elem);
                            dataTree.setParent(elem, obj);
                            dataTree.setChildrenAllowed(elem, false);
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
            dataTree.setItemDescriptionGenerator(new AbstractSelect.ItemDescriptionGenerator() {
                public String generateDescription(Component source, Object itemId, Object propertyId) {
                    // no description for Counting elements
                    if (itemId instanceof CountingTreeHeader) return null;
                    // URI is 
                    String uri = itemId.toString();
                    StringBuilder builder = new StringBuilder();
                    builder.append("<h2>Properties of ");
                    builder.append(itemId);
                    builder.append("</h2><br>");
                    builder.append(generatePropertiesTable(uri, dataGraph));
                    return builder.toString();
                }
            });
        } catch (RepositoryException ex) {
            Logger.getLogger(DSDRepoComponent.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedQueryException ex) {
            Logger.getLogger(DSDRepoComponent.class.getName()).log(Level.SEVERE, null, ex);
        } catch (QueryEvaluationException ex) {
            Logger.getLogger(DSDRepoComponent.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        dataTree.addListener(new Property.ValueChangeListener() {
            public void valueChange(Property.ValueChangeEvent event) {
                Object selected = dataTree.getValue();
                if (selected instanceof String){
                    for (Object item: repoTree.getItemIds()){
                        if (item instanceof ComponentProperty){
                            ComponentProperty cp = (ComponentProperty) item;
                            if (cp.getUri().equalsIgnoreCase(selected.toString())){
                                repoTree.select(item);
                                Object parent = repoTree.getParent(item);
                                while (parent != null){
                                    repoTree.expandItem(parent);
                                    parent = repoTree.getParent(parent);
                                }
                                break;
                            }
                        }
                    }
                }
            }
        });
        
        dataTree.addActionHandler(new Action.Handler() {
            public Action[] getActions(Object target, Object sender) {
                if (target == null) return null;
                if (dataTree.hasChildren(target)) return ACTIONS_NAVI;
                else return null;
            }

            public void handleAction(Action action, Object sender, Object target) {
                if (action == ACTION_EXPAND_ALL)
                    dataTree.expandItemsRecursively(target);
                else if (action == ACTION_COLLAPSE_ALL)
                    dataTree.collapseItemsRecursively(target);
            }
        });
    }
    
    private void addItemStructure(Tree t, Structure s){
        t.addItem(s);
        t.setItemIcon(s, icon_structure);
    }
    
    private void addItemProperty(Tree t, ComponentProperty p){
        t.addItem(p);
        t.setItemIcon(p, icon_property);
    }
    
    private void addItemProperty(Tree t, String p){
        t.addItem(p);
        t.setItemIcon(p, icon_property);
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
                final Structure structure = new SparqlStructure(repository, dsd, repoGraph);
                addItemStructure(repoTree, structure);
                CountingTreeHeader dimCountHeader = createCountingTreeHeader(repoTree, "Dimensions");
                repoTree.setParent(dimCountHeader, structure);
                CountingTreeHeader measCountHeader = createCountingTreeHeader(repoTree, "Measures");
                repoTree.setParent(measCountHeader, structure);
                CountingTreeHeader attrCountHeader = createCountingTreeHeader(repoTree, "Attributes");
                repoTree.setParent(attrCountHeader, structure);
                
                for (Dimension dim: structure.getDimensions()){
                    addItemProperty(repoTree, dim);
                    repoTree.setParent(dim, dimCountHeader);
                }
                for (Attribute attr: structure.getAttributes()){
                    addItemProperty(repoTree, attr);
                    repoTree.setParent(attr, attrCountHeader);
                }
                for (Measure meas: structure.getMeasures()){
                    addItemProperty(repoTree, meas);
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
                            repoTree.setChildrenAllowed(elem, false);
                        }
                    } else {
                        CountingTreeHeader datatypes = createCountingTreeHeader(repoTree, "Datatypes");
                        repoTree.setParent(datatypes, id);
                        CodeDatatypeTreeElement elem = new CodeDatatypeTreeElement(prop.getRange(), false, 0);
                        repoTree.addItem(elem);
                        repoTree.setParent(elem, datatypes);
                        repoTree.setChildrenAllowed(elem, false);
                    }
                }
            }
        });
        
        repoTree.setItemDescriptionGenerator(new AbstractSelect.ItemDescriptionGenerator() {
            public String generateDescription(Component source, Object itemId, Object propertyId) {
                // description only for Counting elements ComponentProperties and Codes/TYpes
                String uri = null;
                if (itemId instanceof ComponentProperty)
                    uri = ((ComponentProperty)itemId).getUri();
                else if (itemId instanceof CodeDatatypeTreeElement)
                    uri = ((CodeDatatypeTreeElement)itemId).getValue();
                else return null;
                
                // URI is 
                StringBuilder builder = new StringBuilder();
                builder.append("<h2>Properties of ");
                builder.append(uri);
                builder.append("</h2><br>");
                builder.append(generatePropertiesTable(uri, repoGraph));
                return builder.toString();
            }
        });
        
        repoTree.addActionHandler(new Action.Handler() {
            public Action[] getActions(Object target, Object sender) {
                if (target instanceof Structure) 
                    return new Action [] { ACTION_SET_AS_DSD };
                else
                    return null;
            }

            public void handleAction(Action action, Object sender, Object target) {
                if (action != ACTION_SET_AS_DSD) return;
                String dsd = ((Structure)target).getUri();
                try {
                    RepositoryConnection conn = repository.getConnection();
                    for (String q: DSDRepoUtils.qCopyDSD(dsd, repoGraph, dataGraph)){
                        GraphQuery query = conn.prepareGraphQuery(QueryLanguage.SPARQL, q);
                        query.evaluate();
                    }
                } catch (RepositoryException ex) {
                    Logger.getLogger(DSDRepoComponent.class.getName()).log(Level.SEVERE, null, ex);
                } catch (MalformedQueryException ex) {
                    Logger.getLogger(DSDRepoComponent.class.getName()).log(Level.SEVERE, null, ex);
                } catch (QueryEvaluationException ex) {
                    Logger.getLogger(DSDRepoComponent.class.getName()).log(Level.SEVERE, null, ex);
                }
                getWindow().showNotification("Set " + dsd + " as qb:DataStructureDefinition");
                dataTree.containerItemSetChange(null);
            }
        });
        
        repoTree.setItemStyleGenerator(new Tree.ItemStyleGenerator() {
            public String getStyle(Object itemId) {
//                getWindow().showNotification("In the style");
//                if ((itemId instanceof ComponentProperty)){
//                    getWindow().showNotification("In the style 2");
//                    if (((ComponentProperty)itemId).getUri().equalsIgnoreCase(highlighted))
//                        return "highlight";
//                }
                return null;
            }
        });
        
        repoTree.addActionHandler(new Action.Handler() {
            public Action[] getActions(Object target, Object sender) {
                if (target == null) return null;
                if (repoTree.hasChildren(target)) return ACTIONS_NAVI;
                else return null;
            }

            public void handleAction(Action action, Object sender, Object target) {
                if (action == ACTION_EXPAND_ALL)
                    repoTree.expandItemsRecursively(target);
                else if (action == ACTION_COLLAPSE_ALL)
                    repoTree.collapseItemsRecursively(target);
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
