/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package rs.pupin.jpo.esta_ld;

import com.vaadin.data.Property;
import com.vaadin.event.Action;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Arrays;
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
import rs.pupin.jpo.datacube.sparql_impl.SparqlDataSet;
import rs.pupin.jpo.dsdrepo.CodeDatatypeTreeElement;
import rs.pupin.jpo.dsdrepo.DSDRepo;
import rs.pupin.jpo.dsdrepo.DSDRepoUtils;
import rs.pupin.jpo.dsdrepo.CountingTreeHeader;
import rs.pupin.jpo.esta_ld.utils.SpatialDimensionManipulator;
import rs.pupin.jpo.esta_ld.utils.TimeDimensionTransformator;

/**
 *
 * @author vukm
 */
public class InspectComponent extends CustomComponent {
    private Repository repository;
    private DataSet ds;
    private VerticalLayout mainLayout;
    private HorizontalLayout datasetLayout;
    private String endpoint;
    private SparqlDCRepository dcRepo;
    private HorizontalLayout contentLayout;
    private DataCubeGraph graph;
    private DSDRepo dsdRepo;
    private Tree dataTree;
    private Tree repoTree;
    
    private String dataGraph;
    private String dataset;
    private String highlighted;
    
    private ThemeResource icon_property = new ThemeResource("icons/icon-prop-blue_16.png");
    private ThemeResource icon_structure = new ThemeResource("icons/icon-struct-blue_16.png");
    
    private static final Action ACTION_SET_AS_DIM = new Action("Set as Dimension");
    private static final Action ACTION_SET_AS_MEAS = new Action("Set as Measure");
    private static final Action ACTION_SET_AS_ATTR = new Action("Set as Attribute");
    private static final Action ACTION_SET_AS_UNDEF = new Action("Set as Undefined");
    private static final Action ACTION_HIGHLIGHT_MATCHING = new Action("Highlight Matching");
    private static final Action ACTION_SET_AS_DSD = new Action("Set as qb:DataStructureDefinition");
    
    private static final Action ACTION_EXPAND_ALL = new Action("Expand All");
    private static final Action ACTION_COLLAPSE_ALL = new Action("Collapse All");
    private static final Action [] ACTIONS_NAVI = new Action [] { ACTION_EXPAND_ALL, ACTION_COLLAPSE_ALL };
    
    private static final Action ACTION_CREATE_CL = new Action("Create Code List");
    private static final Action ACTION_DELETE_CL = new Action("Delete Code List");
    private static final Action ACTION_SET_AS_CL = new Action("Set as Code List");
    
    private static final Action ACTION_STORE = new Action("Store DSD in repository");
    
    private static final Action ACTION_TRANSFORM_DIM = new Action("Manage as Temporal Dimension");
    private static final Action ACTION_MANAGE_GEO = new Action("Manage as Spatial Dimension");

    private static final Action [] ACTIONS = new Action[] { 
        ACTION_SET_AS_DIM, ACTION_SET_AS_MEAS, ACTION_SET_AS_ATTR, 
        ACTION_SET_AS_UNDEF, ACTION_HIGHLIGHT_MATCHING };
    
    private static final Action [] ACTIONS_NAVI_PLUS = new Action [] {
        ACTION_SET_AS_DIM, ACTION_SET_AS_MEAS, ACTION_SET_AS_ATTR, 
        ACTION_SET_AS_UNDEF, ACTION_HIGHLIGHT_MATCHING,
        ACTION_EXPAND_ALL, ACTION_COLLAPSE_ALL
    };
    private static final Action [] ACTIONS_DSD_NAVI = new Action [] {
        ACTION_SET_AS_DSD, ACTION_EXPAND_ALL, ACTION_COLLAPSE_ALL
    };
    
    private VerticalLayout statusLayout;
    
    private MenuBar.Command cmdFindDSD;
    private MenuBar.Command cmdCreateDSD;
    private CountingTreeHeader dim;
    private CountingTreeHeader meas;
    private CountingTreeHeader attr;
    private CountingTreeHeader undef;
    private Tree compatibleCodeLists;
    private MenuBar.Command cmdStoreDSD;
    
    private int numUndefinedComponents = 0;
    private int numMissingCodeLists = 0;
    private Label lblMissingCodeLists;
    private Label lblUndefined;
    private VerticalLayout dimTransformLayout;
    private final Logger logger;
    
    private void updateUndefinedAndMissing(){
        int num = 0;
        for (Object obj: dataTree.rootItemIds()){
            CountingTreeHeader h = (CountingTreeHeader) obj;
            if (h.getHeader().toString().startsWith("U"))
                numUndefinedComponents = h.getCount();
            
            num += countMissingCodeListsInHeader(dataTree, h);
        }
        numMissingCodeLists = num;
        lblUndefined.setValue("There are still " + numUndefinedComponents + " undefined components");
        lblMissingCodeLists.setValue("There are still " + numMissingCodeLists + " missing code lists");
    }
    
    private int countMissingCodeListsInHeader(Tree tree, CountingTreeHeader h){
        int c = 0;
        Collection<?> children = tree.getChildren(h);
        if (children != null) {
            for (Object obj: children){
                // obj is either dimension, measure or attribute
                Collection<?> infants = tree.getChildren(obj);
                if (infants.size() != 2 && infants.iterator().next().toString().startsWith("C"))
                    c++;
            }
        }
        return c;
    }
    
    private class CodeItem {
        private String code;
        public CodeItem (String code){
            this.code = code;
        }
        @Override
        public String toString() {
            return code;
        }
        
    }
    
    private class CodeListUriWindow extends Window {
        private String uri = null;
        public String getUri() {
            return uri;
        }
        public void show() {
            InspectComponent.this.getWindow().addWindow(CodeListUriWindow.this);
        }
        public CodeListUriWindow (){
            setCaption("Enter URI");
            setModal(true);
            setClosable(false);
            setWidth("450px");
            final TextField field = new TextField("Enter code list URI");
            addComponent(field);
            field.setWidth("100%");
            HorizontalLayout btnLayout = new HorizontalLayout();
            addComponent(btnLayout);
            Button ok = new Button("OK");
            btnLayout.addComponent(ok);
            Button cancel = new Button("Cancel");
            btnLayout.addComponent(cancel);
            ok.addListener(new Button.ClickListener() {
                public void buttonClick(Button.ClickEvent event) {
                    uri = field.getValue().toString();
                    if (!isUri(uri)) {
                        InspectComponent.this.getWindow().showNotification("Not a valid URI", Window.Notification.TYPE_ERROR_MESSAGE);
                        uri = null;
                        return;
                    }
                    InspectComponent.this.getWindow().removeWindow(CodeListUriWindow.this);
                }
            });
            cancel.addListener(new Button.ClickListener() {
                public void buttonClick(Button.ClickEvent event) {
                    uri = null;
                    InspectComponent.this.getWindow().removeWindow(CodeListUriWindow.this);
                }
            });
        }
    }
    
    public InspectComponent(Repository repository, String dataGraph, String dataset){
        this.repository = repository;
        this.dataGraph = dataGraph;
        
        this.logger = Logger.getLogger(InspectComponent.class.getName());
        
        dcRepo = new SparqlDCRepository(repository);
        graph = new SparqlDCGraph(repository, dataGraph);
        ds = new SparqlDataSet(repository, dataset, dataGraph);
        
        VerticalLayout rootLayout = new VerticalLayout();
        rootLayout.setWidth("100%");
        rootLayout.setSpacing(true);
        rootLayout.setMargin(false);
        
        mainLayout = new VerticalLayout();
        mainLayout.setWidth("100%");
        mainLayout.setSpacing(true);
        mainLayout.setMargin(false);
        
        rootLayout.addComponent(mainLayout);
        
        storeDSD();
        
        setCompositionRoot(rootLayout);
    }
    
    private void findDSDs(){
        mainLayout.removeAllComponents();
        
        contentLayout = new HorizontalLayout();
        contentLayout.setSizeFull();
        contentLayout.setWidth("100%");
        contentLayout.setSpacing(true);
        mainLayout.addComponent(contentLayout);
        refreshContentFindDSDs(ds);
    }
    
    private void createDSD(){
        mainLayout.removeAllComponents();
        
        contentLayout = new HorizontalLayout();
        contentLayout.setSizeFull();
        contentLayout.setWidth("100%");
        contentLayout.setSpacing(true);
        mainLayout.addComponent(contentLayout);
        refreshContentCreateDSD(ds);
    }
    
    private void storeDSD(){
        mainLayout.removeAllComponents();
        
        contentLayout = new HorizontalLayout();
        contentLayout.setSizeFull();
        contentLayout.setWidth("100%");
        contentLayout.setSpacing(true);
        mainLayout.addComponent(contentLayout);
        refreshContentStoreDSD(ds);
    }
    
    private CountingTreeHeader createCountingTreeHeader(Tree t, String header){
        CountingTreeHeader h = new CountingTreeHeader(t, header);
        t.addItem(h);
        return h;
    }
    
    private String generatePropertiesTable(String uri, String graph){
        if (uri == null || graph == null) return "<table style=\"border-spacing:7px\"></table>";
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
            logger.log(Level.SEVERE, null, ex);
        } catch (MalformedQueryException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (QueryEvaluationException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return builder.toString();
    }
    
    private void populateDataTree(){
        dataTree.removeAllItems();
        try {
            RepositoryConnection con = repository.getConnection();
            TupleQuery q = con.prepareTupleQuery(QueryLanguage.SPARQL, DSDRepoUtils.qPossibleComponents(dataGraph, dataset));
            TupleQueryResult res = q.evaluate();
            
            dim = createCountingTreeHeader(dataTree, "Dimensions");
            meas = createCountingTreeHeader(dataTree, "Measures");
            attr = createCountingTreeHeader(dataTree, "Attributes");
            undef = createCountingTreeHeader(dataTree, "Undefined");
            
            while (res.hasNext()){
                BindingSet set = res.next();
                String component = set.getValue("comp").stringValue();
//                undef.addElement(component);
                addItemProperty(dataTree, component);
                dataTree.setParent(component, undef);
            }
            
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
                        logger.log(Level.SEVERE, null, ex);
                    } catch (MalformedQueryException ex) {
                        logger.log(Level.SEVERE, null, ex);
                    } catch (QueryEvaluationException ex) {
                        logger.log(Level.SEVERE, null, ex);
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
            logger.log(Level.SEVERE, null, ex);
        } catch (MalformedQueryException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (QueryEvaluationException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        dataTree.expandItemsRecursively(undef);
        dataTree.collapseItemsRecursively(undef);
    }
    
    private void addDataTreeListenersStore(){
        dataTree.addActionHandler(new Action.Handler() {
            public Action[] getActions(Object target, Object sender) {
                if (target instanceof Dimension) {
                    return new Action [] { ACTION_TRANSFORM_DIM, ACTION_MANAGE_GEO };
                } else
                    return null;
            }
            public void handleAction(Action action, Object sender, Object target) {
                if (action == null) return;
                if (action == ACTION_TRANSFORM_DIM) {
                    showTransformDimensionView((Dimension)target);
                }
                if (action == ACTION_MANAGE_GEO) {
                    showManageGeoView((Dimension)target);
                }
            }
        });
        dataTree.addListener(new ItemClickEvent.ItemClickListener() {
            public void itemClick(ItemClickEvent event) {
                Object target = event.getItemId();
                if (target instanceof Dimension) {
                    showDetailsView("Dimension", ((Dimension)target).getUri());
                } else if (target instanceof Attribute) {
                    showDetailsView("Attribute", ((Attribute)target).getUri());
                } else if (target instanceof Measure) {
                    showDetailsView("Measure", ((Measure)target).getUri());
                } else if (target instanceof Structure) {
                    showDetailsView("Structure", ((Structure)target).getUri());
                } 
                System.out.println(target.toString());
            }
        });
    }
    
    private void showTransformDimensionView(Dimension dim) {
        dimTransformLayout.removeAllComponents();
        dimTransformLayout.addComponent(new Label("<h1>Manage Temporal Dimension</h1>", Label.CONTENT_XHTML));
        dimTransformLayout.addComponent(new Label("<h2>Dimension: " + dim.getUri() + "</h2>", Label.CONTENT_XHTML));
        
        // show properties table
        final Table propertiesTable = new Table("Properties");
        propertiesTable.setHeight("250px");
        propertiesTable.setWidth("100%");
        propertiesTable.addContainerProperty("Property", String.class, null);
        propertiesTable.addContainerProperty("Value", String.class, null);
        dimTransformLayout.addComponent(propertiesTable);
        try {
            RepositoryConnection conn = repository.getConnection();
            TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, DSDRepoUtils.qResourcePorperties(dim.getUri(), dataGraph));
            TupleQueryResult res = query.evaluate();
            int i = 0;
            while (res.hasNext()) {
                BindingSet set = res.next();
                Object [] row = new Object [] {
                    set.getValue("p").stringValue(), 
                    set.getValue("o").stringValue()
                };
                propertiesTable.addItem(row, i++);
            }
        } catch (RepositoryException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (MalformedQueryException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (QueryEvaluationException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        
        // add separator
        dimTransformLayout.addComponent(new Label("<hr/>", Label.CONTENT_XHTML));
        
        // TODO: show transform to time dimension 
        // select: { year, month, date }
        final ComboBox comboType = new ComboBox("Choose type:", 
                Arrays.asList(TimeDimensionTransformator.Type.values()));
        comboType.setNullSelectionAllowed(false);
        comboType.select(TimeDimensionTransformator.Type.XSD_YEAR);
        dimTransformLayout.addComponent(comboType);
        // text field: { pattern }
        final TextField fieldPattern = new TextField("Transformation Pattern:");
        fieldPattern.setWidth("400px");
        dimTransformLayout.addComponent(fieldPattern);
        // button: transform
        final Button btnTransform = new Button("Transform");
        dimTransformLayout.addComponent(btnTransform);
        
        final TimeDimensionTransformator timeTransformator = new TimeDimensionTransformator(
                repository, 
                dataGraph, 
                dim.getUri(), 
                (TimeDimensionTransformator.Type)comboType.getValue());
        try {
            timeTransformator.initialize();
        } catch (RepositoryException ex) {
            logger.log(Level.SEVERE, null, ex);
            btnTransform.setEnabled(false);
            getWindow().showNotification(ex.getMessage(), Window.Notification.TYPE_ERROR_MESSAGE);
        } catch (MalformedQueryException ex) {
            logger.log(Level.SEVERE, null, ex);
            btnTransform.setEnabled(false);
            getWindow().showNotification(ex.getMessage(), Window.Notification.TYPE_ERROR_MESSAGE);
        } catch (QueryEvaluationException ex) {
            logger.log(Level.SEVERE, null, ex);
            btnTransform.setEnabled(false);
            getWindow().showNotification(ex.getMessage(), Window.Notification.TYPE_ERROR_MESSAGE);
        }
        
        btnTransform.addListener(new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                if (fieldPattern.getValue() == null) {
                    getWindow().showNotification("Come on, you need to provide the transformation pattern");
                }
                try {
                    // set type according to the value in the combo box
                    timeTransformator.setType((TimeDimensionTransformator.Type)comboType.getValue());
                    // first check if the values can be parsed
                    logger.fine("Parsing...");
                    timeTransformator.parseLean(fieldPattern.getValue().toString());
                    // if parsing went fine fire away
                    logger.fine("Modifying dimension...");
                    timeTransformator.modifyDimension();
                    logger.fine("Removing old...");
                    timeTransformator.removeOld();
                    logger.fine("Inserting new...");
                    timeTransformator.insertNew();
                    logger.fine("Finished transformation!!!");
                    getWindow().showNotification("Dimension transformed");
//                } catch (ParseException ex) {
//                    logger.log(Level.SEVERE, null, ex);
//                    String msg = "Could not parse values \n";
//                    getWindow().showNotification(msg + ex.getMessage(), Window.Notification.TYPE_ERROR_MESSAGE);
                } catch (RepositoryException ex) {
                    logger.log(Level.SEVERE, null, ex);
                    getWindow().showNotification(ex.getMessage(), Window.Notification.TYPE_ERROR_MESSAGE);
                } catch (MalformedQueryException ex) {
                    logger.log(Level.SEVERE, null, ex);
                    getWindow().showNotification(ex.getMessage(), Window.Notification.TYPE_ERROR_MESSAGE);
                } catch (QueryEvaluationException ex) {
                    logger.log(Level.SEVERE, null, ex);
                    getWindow().showNotification(ex.getMessage(), Window.Notification.TYPE_ERROR_MESSAGE);
                }
            }
        });
    }
    
    private void showManageGeoView(Dimension dim) {
        dimTransformLayout.removeAllComponents();
        dimTransformLayout.addComponent(new Label("<h1>Manage Spatial Dimension</h1>", Label.CONTENT_XHTML));
        dimTransformLayout.addComponent(new Label("<h2>Dimension: " + dim.getUri() + "</h2>", Label.CONTENT_XHTML));
        
        // show properties table
        final Table propertiesTable = new Table("Properties");
        propertiesTable.setHeight("250px");
        propertiesTable.setWidth("100%");
        propertiesTable.addContainerProperty("Property", String.class, null);
        propertiesTable.addContainerProperty("Value", String.class, null);
        dimTransformLayout.addComponent(propertiesTable);
        try {
            RepositoryConnection conn = repository.getConnection();
            TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, DSDRepoUtils.qResourcePorperties(dim.getUri(), dataGraph));
            TupleQueryResult res = query.evaluate();
            int i = 0;
            while (res.hasNext()) {
                BindingSet set = res.next();
                Object [] row = new Object [] {
                    set.getValue("p").stringValue(), 
                    set.getValue("o").stringValue()
                };
                propertiesTable.addItem(row, i++);
            }
        } catch (RepositoryException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (MalformedQueryException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (QueryEvaluationException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        
        // add separator
        dimTransformLayout.addComponent(new Label("<hr/>", Label.CONTENT_XHTML));
        
        final SpatialDimensionManipulator manipulator = new SpatialDimensionManipulator(dim, SpatialDimensionManipulator.Kind.BY_CODE);
        
        final ComboBox comboKind = new ComboBox("Choose kind:", 
                Arrays.asList(SpatialDimensionManipulator.Kind.values()));
        comboKind.setNullSelectionAllowed(false);
        comboKind.select(SpatialDimensionManipulator.Kind.BY_CODE);
        dimTransformLayout.addComponent(comboKind);
        // text field: { pattern }
        final TextField fieldPrefix = new TextField("Prefix:");
        fieldPrefix.setWidth("400px");
        dimTransformLayout.addComponent(fieldPrefix);
        // button: insert polygons
        final Button btnInsertPolygons = new Button("Insert Polygons");
        dimTransformLayout.addComponent(btnInsertPolygons);
        
        btnInsertPolygons.addListener(new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                String prefix = fieldPrefix.getValue().toString();
                if (prefix == null) {
                    return;
                }
                
                String array = manipulator.extractPairs(prefix);
                String jsCall = "javaInsertPolygons(" + array + ")";
                getWindow().executeJavaScript(jsCall);
            }
        });
    }
    
    private void showDetailsView(String type, String uri) {
        dimTransformLayout.removeAllComponents();
        dimTransformLayout.addComponent(new Label("<h1>Details View</h1>", Label.CONTENT_XHTML));
        dimTransformLayout.addComponent(new Label("<h2>" + type + ": " + uri + "</h2>", Label.CONTENT_XHTML));
        
        // show properties table
        final Table propertiesTable = new Table("Properties");
        propertiesTable.setHeight("250px");
        propertiesTable.setWidth("100%");
        propertiesTable.addContainerProperty("Property", String.class, null);
        propertiesTable.addContainerProperty("Value", String.class, null);
        dimTransformLayout.addComponent(propertiesTable);
        try {
            RepositoryConnection conn = repository.getConnection();
            TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, DSDRepoUtils.qResourcePorperties(uri, dataGraph));
            TupleQueryResult res = query.evaluate();
            int i = 0;
            while (res.hasNext()) {
                BindingSet set = res.next();
                Object [] row = new Object [] {
                    set.getValue("p").stringValue(), 
                    set.getValue("o").stringValue()
                };
                propertiesTable.addItem(row, i++);
            }
        } catch (RepositoryException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (MalformedQueryException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (QueryEvaluationException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }
    
    private void addDataTreeListenersFind(){
        dataTree.addActionHandler(new Action.Handler() {

            public Action[] getActions(Object target, Object sender) {
                if (target == null) return null;
                if (dataTree.hasChildren(target))
                    if (target instanceof String) {
                        return ACTIONS_NAVI_PLUS;
                    } else {
                        return ACTIONS_NAVI;
                    }
                else {
                    if (target instanceof String) {
                        return ACTIONS;
                    } else {
                        return null;
                    }
                }
            }

            public void handleAction(Action action, Object sender, Object target) {
                if (!(target instanceof String || target instanceof CountingTreeHeader)) return;
                String e = (target instanceof String)?(String)target:null;
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
                } else if (action == ACTION_EXPAND_ALL)
                    dataTree.expandItemsRecursively(target);
                else if (action == ACTION_COLLAPSE_ALL)
                    dataTree.collapseItemsRecursively(target);
            }
        });
        
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
    }
    
    private void addDataTreeListenersCreate(){
        dataTree.addActionHandler(new Action.Handler() {
            // TODO: add options to create and delete code lists

            public Action[] getActions(Object target, Object sender) {
                if (target == null) return null;
                LinkedList<Action> actions = new LinkedList<Action>();
                if (dataTree.hasChildren(target))
                    if (target instanceof String) {
                        for (Action a: ACTIONS_NAVI_PLUS) actions.add(a);
                        Collection<?> children = dataTree.getChildren(target);
                        if (children.size() == 2) 
                            actions.add(ACTION_DELETE_CL);
                        else if (children.iterator().next().toString().startsWith("C"))
                            actions.add(ACTION_CREATE_CL);
                    } else {
                        for (Action a: ACTIONS_NAVI) actions.add(a);
                    }
                else {
                    if (target instanceof String) {
                        for (Action a: ACTIONS) actions.add(a);
                    }
                }
                return actions.toArray(new Action [] {});
            }

            public void handleAction(Action action, Object sender, final Object target) {
                if (!(target instanceof String || target instanceof CountingTreeHeader)) return;
                String e = (target instanceof String)?(String)target:null;
                if (action == ACTION_SET_AS_DIM) {
                    dataTree.setParent(e, dim);
                    updateUndefinedAndMissing();
                }
                else if (action == ACTION_SET_AS_MEAS){
                    dataTree.setParent(e, meas);
                    updateUndefinedAndMissing();
                } else if (action == ACTION_SET_AS_ATTR){
                    dataTree.setParent(e, attr);
                    updateUndefinedAndMissing();
                } else if (action == ACTION_SET_AS_UNDEF){
                    dataTree.setParent(e, undef);
                    updateUndefinedAndMissing();
                } else if (action == ACTION_HIGHLIGHT_MATCHING){
                    // notify repo tree about the change
//                        highlighted = e;
                    // update repo tree
//                        repoTree.containerItemSetChange(null);
                } else if (action == ACTION_EXPAND_ALL)
                    dataTree.expandItemsRecursively(target);
                else if (action == ACTION_COLLAPSE_ALL)
                    dataTree.collapseItemsRecursively(target);
                else if (action == ACTION_CREATE_CL){
                    final String prop = target.toString();
                    Collection<?> children = dataTree.getChildren(target);
                    final Collection<String> codes = new LinkedList<String>();
                    for (Object child: children){
                        if (child.toString().startsWith("Codes")){
                            for (Object obj: dataTree.getChildren(child))
                                codes.add(obj.toString());
                        }
                    }
                    // get code list URI from user
                    final CodeListUriWindow uriWindow = new CodeListUriWindow();
                    uriWindow.addListener(new Window.CloseListener() {
                        public void windowClose(Window.CloseEvent e) {
                            String uri = uriWindow.getUri();
                            if (uri == null) {
                                getWindow().showNotification("Not a valid URI", Window.Notification.TYPE_ERROR_MESSAGE);
                                return;
                            }
                            try {
                                RepositoryConnection conn = repository.getConnection();
                                // insert dataset link
                                String insertQuery = "INSERT INTO GRAPH <" + dataGraph + 
                                        "> { <" + prop + "> <http://purl.org/linked-data/cube#codeList> <" + uri + "> }";
                                conn.prepareGraphQuery(QueryLanguage.SPARQL, insertQuery).evaluate();
                                // insert the rest
                                GraphQuery query = conn.prepareGraphQuery(QueryLanguage.SPARQL, DSDRepoUtils.qCreateCodeList(dataGraph, prop, uri, codes));
                                query.evaluate();
                                Object codesItem = dataTree.getChildren(target).iterator().next();
                                Collection<?> codesForCodeList = dataTree.getChildren(codesItem);
                                CountingTreeHeader headerCodeList = new CountingTreeHeader(dataTree, "Code List");
                                dataTree.addItem(headerCodeList);
                                dataTree.setParent(headerCodeList, target);
                                for (Object elem: codesForCodeList){
                                    CodeItem ci = new CodeItem(elem.toString());
                                    dataTree.addItem(ci);
                                    dataTree.setParent(ci, headerCodeList);
                                }
                                updateUndefinedAndMissing();
                            } catch (RepositoryException ex) {
                                logger.log(Level.SEVERE, null, ex);
                            } catch (MalformedQueryException ex) {
                                logger.log(Level.SEVERE, null, ex);
                            } catch (QueryEvaluationException ex) {
                                logger.log(Level.SEVERE, null, ex);
                            }
                        }
                    });
                    uriWindow.show();
                    
                } else if (action == ACTION_DELETE_CL){
                    String prop = target.toString();
                    Collection<?> children = dataTree.getChildren(target);
                    final Collection<String> codes = new LinkedList<String>();
                    final Collection<Object> items = new LinkedList<Object>();
                    Object itemsHeader = null;
                    for (Object child: children){
                        if (child.toString().startsWith("Code List")){
                            itemsHeader = child;
                            for (Object obj: dataTree.getChildren(child)) {
                                codes.add(obj.toString());
                                items.add(obj);
                            }
                        }
                    }
                    try {
                        RepositoryConnection conn = repository.getConnection();
                        GraphQuery query = conn.prepareGraphQuery(QueryLanguage.SPARQL, DSDRepoUtils.qDeleteCodeList(dataset, prop, "uri", codes));
                        query.evaluate();
                        for (Object o: items) dataTree.removeItem(o);
                        dataTree.removeItem(itemsHeader);
                        updateUndefinedAndMissing();
                    } catch (RepositoryException ex) {
                        logger.log(Level.SEVERE, null, ex);
                    } catch (MalformedQueryException ex) {
                        logger.log(Level.SEVERE, null, ex);
                    } catch (QueryEvaluationException ex) {
                        logger.log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
        
        // show compatible code lists when a user selects a dim with code lists
        dataTree.addListener(new Property.ValueChangeListener() {
            public void valueChange(Property.ValueChangeEvent event) {
                Object selected = dataTree.getValue();
                if (selected instanceof String){
                    Object child = dataTree.getChildren(selected).iterator().next();
                    Object infant = dataTree.getChildren(child).iterator().next();
                    getWindow().showNotification("Finding compatible code lists not available");
                }
            }
        });
    }
    
    private void addItemStructure(Tree t, Structure s){
        t.addItem(s);
        t.setItemIcon(s, icon_structure);
    }
    
    private void addItemProperty(Tree t, Object p){
        t.addItem(p);
        t.setItemIcon(p, icon_property);
    }
    
    private void populateStoreTree(){
        dataTree.removeAllItems();
        String dsd = ds.getStructure().getUri();
        Structure structure = ds.getStructure();
        addItemStructure(dataTree, structure);
        
        CountingTreeHeader dimCountHeader = createCountingTreeHeader(dataTree, "Dimensions");
        dataTree.setParent(dimCountHeader, structure);
        CountingTreeHeader measCountHeader = createCountingTreeHeader(dataTree, "Measures");
        dataTree.setParent(measCountHeader, structure);
        CountingTreeHeader attrCountHeader = createCountingTreeHeader(dataTree, "Attributes");
        dataTree.setParent(attrCountHeader, structure);
        
        for (Dimension dim: structure.getDimensions()){
            addItemProperty(dataTree, dim);
            dataTree.setParent(dim, dimCountHeader);
        }
        for (Attribute attr: structure.getAttributes()){
            addItemProperty(dataTree, attr);
            dataTree.setParent(attr, attrCountHeader);
        }
        for (Measure meas: structure.getMeasures()){
            addItemProperty(dataTree, meas);
            dataTree.setParent(meas, measCountHeader);
        }
        
        dataTree.addListener(new Tree.ExpandListener() {
            public void nodeExpand(Tree.ExpandEvent event) {
                Object id = event.getItemId();
                if (id instanceof ComponentProperty && !dataTree.hasChildren(id)) {
                    ComponentProperty prop = (ComponentProperty) id;
                    CodeList codeList = prop.getCodeList();
                    if (codeList != null) {
                        CountingTreeHeader codeCountingHeader = createCountingTreeHeader(dataTree, "Codes");
                        dataTree.setParent(codeCountingHeader, id);
                        for (String code: codeList.getAllCodes()){
                            CodeDatatypeTreeElement elem = new CodeDatatypeTreeElement(code, true, 0);
                            dataTree.addItem(elem);
                            dataTree.setParent(elem, codeCountingHeader);
                            dataTree.setChildrenAllowed(elem, false);
                        }
                    } else {
                        CountingTreeHeader datatypes = createCountingTreeHeader(dataTree, "Datatypes");
                        dataTree.setParent(datatypes, id);
                        for (String range: prop.getRanges()){
                            CodeDatatypeTreeElement elem = new CodeDatatypeTreeElement(range, false, 0);
                            dataTree.addItem(elem);
                            dataTree.setParent(elem, datatypes);
                            dataTree.setChildrenAllowed(elem, false);
                        }
                        
                        CountingTreeHeader values = createCountingTreeHeader(dataTree, "Values");
                        dataTree.setParent(values, id);
                        for (String val: prop.getValues()){
                            CodeDatatypeTreeElement elem2 = new CodeDatatypeTreeElement(val, false, 0);
                            dataTree.addItem(elem2);
                            dataTree.setParent(elem2, values);
                            dataTree.setChildrenAllowed(elem2, false);
                        }
                    }
                }
            }
        });
        
        dataTree.setItemDescriptionGenerator(new AbstractSelect.ItemDescriptionGenerator() {
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
                builder.append(generatePropertiesTable(uri, dataGraph));
                return builder.toString();
            }
        });
    }
    
    private void populateRepoTree(){
        repoTree.removeAllItems();
        repoTree.addItem("Repo tree not available");
    }
    
    private void refreshContentFindDSDs(DataSet ds){
        if (ds == null) {
            getWindow().showNotification("No dataset selected", Window.Notification.TYPE_ERROR_MESSAGE);
            return;
        }
        Structure struct = ds.getStructure();
        if (struct != null){
            contentLayout.addComponent(new Label("The dataset already has a DSD!"));
            return;
        }
        
        dataset = ds.getUri();
        contentLayout.removeAllComponents();;
        dataTree = new Tree("Dataset");
        dataTree.setWidth("500px");
        dataTree.setNullSelectionAllowed(true);
        dataTree.setImmediate(true);
        populateDataTree();
        addDataTreeListenersFind();
        contentLayout.addComponent(dataTree);
        contentLayout.setExpandRatio(dataTree, 0.0f);
        repoTree = new Tree("Matching Structures");
        repoTree.setNullSelectionAllowed(true);
        repoTree.setImmediate(true);
        populateRepoTree();
        VerticalLayout v = new VerticalLayout();
        contentLayout.addComponent(v);
        contentLayout.setExpandRatio(v, 2.0f);
        v.addComponent(repoTree);
        v.setExpandRatio(repoTree, 2.0f);
    }
    
    private void refreshContentStoreDSD(DataSet ds){
        if (ds == null) {
            getWindow().showNotification("No dataset selected", Window.Notification.TYPE_ERROR_MESSAGE);
            return;
        }
        Structure struct = ds.getStructure();
        if (struct == null){
            contentLayout.addComponent(new Label("The dataset doesn't contain a DSD!"));
            return;
        }
        
        dataset = ds.getUri();
        contentLayout.removeAllComponents();;
        dataTree = new Tree("Dataset");
        dataTree.setWidth("500px");
        dataTree.setNullSelectionAllowed(true);
        dataTree.setImmediate(true);
        populateStoreTree();
        addDataTreeListenersStore();
        contentLayout.addComponent(dataTree);
        contentLayout.setExpandRatio(dataTree, 0.0f);
        dimTransformLayout = new VerticalLayout();
        dimTransformLayout.setSpacing(true);
        contentLayout.addComponent(dimTransformLayout);
        contentLayout.setExpandRatio(dimTransformLayout, 2.0f);
        repoTree = new Tree("Matching Structures");
        repoTree.setNullSelectionAllowed(true);
        repoTree.setImmediate(true);
    }
    
    private void refreshContentCreateDSD(DataSet ds){
        if (ds == null) {
            getWindow().showNotification("No dataset selected", Window.Notification.TYPE_ERROR_MESSAGE);
            return;
        }
        Structure struct = ds.getStructure();
        if (struct != null){
            contentLayout.addComponent(new Label("The dataset already has a DSD!"));
            return;
        }
        
        dataset = ds.getUri();
        contentLayout.removeAllComponents();
        dataTree = new Tree("Dataset");
        dataTree.setNullSelectionAllowed(true);
        dataTree.setImmediate(true);
        dataTree.setWidth("500px");
        populateDataTree();
        addDataTreeListenersCreate();
        contentLayout.addComponent(dataTree);
        contentLayout.setExpandRatio(dataTree, 0.0f);
        
        final VerticalLayout right = new VerticalLayout();
        right.setSpacing(true);
        contentLayout.addComponent(right);
        contentLayout.setExpandRatio(right, 2.0f);
        lblUndefined = new Label("There are still x undefined components", Label.CONTENT_XHTML);
        right.addComponent(lblUndefined);
        lblMissingCodeLists = new Label("There are still y missing code lists", Label.CONTENT_XHTML);
        right.addComponent(lblMissingCodeLists);
        final TextField dsdUri = new TextField("Enter DSD URI");
        dsdUri.setWidth("300px");
        right.addComponent(dsdUri);
        final Button btnCreate = new Button("Create DSD");
        right.addComponent(btnCreate);
        right.addComponent(new Label("<hr/>", Label.CONTENT_XHTML));
        compatibleCodeLists = new Tree("Compatible code lists");
        right.addComponent(compatibleCodeLists);
        
        updateUndefinedAndMissing();
        
        compatibleCodeLists.addActionHandler(new Action.Handler() {
            public Action[] getActions(Object target, Object sender) {
                if (target == null) return null;
                if (compatibleCodeLists.getParent(target) != null) return null;
                return new Action [] { ACTION_SET_AS_CL };
            }
            public void handleAction(Action action, Object sender, Object target) {
                if (action == ACTION_SET_AS_CL){
                    getWindow().showNotification("Action not available");
                }
            }
        });
        btnCreate.addListener(new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                if (numUndefinedComponents > 0){
                    getWindow().showNotification("There can be no undefined components", Window.Notification.TYPE_ERROR_MESSAGE);
                    return;
                }
                if (numMissingCodeLists > 0){
                    getWindow().showNotification("All code lists must first be created or imported", Window.Notification.TYPE_ERROR_MESSAGE);
                    return;
                }
                final String dsd = dsdUri.getValue().toString();
                if (!isUri(dsd)){
                    getWindow().showNotification("Enter a valid URI for the DSD", Window.Notification.TYPE_ERROR_MESSAGE);
                }
                
                try {
                    RepositoryConnection conn = repository.getConnection();
                    LinkedList<String> dList = new LinkedList<String>();
                    LinkedList<String> mList = new LinkedList<String>();
                    LinkedList<String> aList = new LinkedList<String>();
                    LinkedList<String> uList = new LinkedList<String>();
                    LinkedList<String> propList = new LinkedList<String>();
                    LinkedList<String> rangeList = new LinkedList<String>();
                    
                    for (Object id: dataTree.rootItemIds()){
                        Collection<?> children = dataTree.getChildren(id);
                        if (children == null) continue;
                        
                        Collection<String> list = null;
                        if (id.toString().startsWith("D")) list = dList;
                        else if (id.toString().startsWith("M")) list = mList;
                        else if (id.toString().startsWith("A")) list = aList;
                        else if (id.toString().startsWith("U")) list = uList;
                        
                        for (Object prop: dataTree.getChildren(id)){
                            CountingTreeHeader h = (CountingTreeHeader)dataTree.getChildren(prop).iterator().next();
                            propList.add(prop.toString());
                            list.add(prop.toString());
                            if (h.toString().startsWith("C")) {
                                rangeList.add("http://www.w3.org/2004/02/skos/core#Concept");
                            } else {
                                rangeList.add(dataTree.getChildren(h).iterator().next().toString());
                            }
                        }
                    }
                    if (uList.size() > 0){
                        getWindow().showNotification("There are undefined properties!", Window.Notification.TYPE_WARNING_MESSAGE);
                        return;
                    }
                    GraphQuery query = conn.prepareGraphQuery(QueryLanguage.SPARQL, 
                            DSDRepoUtils.qCreateDSD(dataset, dsd, dList, mList, aList, propList, rangeList, dataGraph));
                    query.evaluate();
                    getWindow().showNotification("DSD created!");
                    InspectComponent.this.ds = new SparqlDataSet(repository, 
                            InspectComponent.this.ds.getUri(), dataGraph);
                    createDSD();
                } catch (RepositoryException ex) {
                    logger.log(Level.SEVERE, null, ex);
                } catch (MalformedQueryException ex) {
                    logger.log(Level.SEVERE, null, ex);
                } catch (QueryEvaluationException ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
            }
        });
    }
    
    private boolean isUri(String uri){
        if (uri == null || uri.equals("")) return false;
        try {
            java.net.URI u = new java.net.URI(uri);
        } catch (URISyntaxException ex) {
            return false;
        }
        return true;
    }
    
    private void addCodeListToDataTree(){
        Object obj = compatibleCodeLists.rootItemIds().iterator().next();
        if (obj == null) return;
        Collection<?> children = compatibleCodeLists.getChildren(obj);
        if (children == null) return;
        Object dataTreeElement = compatibleCodeLists.getData();
        CountingTreeHeader codeListHeader = new CountingTreeHeader(dataTree, "Code List");
        dataTree.addItem(codeListHeader);
        dataTree.setParent(codeListHeader, dataTreeElement);
        for (Object child: children){
            CodeItem ci = new CodeItem(child.toString());
            dataTree.addItem(ci);
            dataTree.setParent(ci, codeListHeader);
        }
    }
    
}
