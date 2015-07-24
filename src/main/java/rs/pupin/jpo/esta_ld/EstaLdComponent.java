/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package rs.pupin.jpo.esta_ld;

import com.vaadin.data.Property;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sparql.SPARQLRepository;
import rs.pupin.jpo.datacube.DataCubeGraph;
import rs.pupin.jpo.datacube.DataCubeRepository;
import rs.pupin.jpo.datacube.DataSet;
import rs.pupin.jpo.datacube.Dimension;
import rs.pupin.jpo.datacube.sparql_impl.DummyDCRepository;
import rs.pupin.jpo.datacube.sparql_impl.SparqlDCRepository;

/**
 *
 * @author vukm
 */
public class EstaLdComponent extends CustomComponent {
    
    private Repository repository;
    private String endpoint = "http://147.91.50.167/sparql";
    private VerticalLayout mainLayout;
    private VerticalLayout geoLayout;
    private HorizontalLayout dimLayout;
    private VerticalLayout mapLayout;
    private VerticalLayout rightLayout;
    private VerticalLayout chartLayout;
    private HorizontalLayout datasetLayout;
    private HorizontalLayout contentLayout;
    private DataCubeRepository dcRepo;
    private ComboBox selectGraph;
    private ComboBox selectDataSet;
    private Button[] dimNames;
    private ComboBox[] dimValues;
    private static final String LEVEL_LABEL_CONTENT = 
            "Geo granularity level "
            + "<input id=\"geominus\" type=\"button\" style=\" width:25px; height:25px;\" value=\"-\"></input>"
            + "<input id=\"geoLevelLabel\" type=\"text\" readonly=\"\" value=\"Level 2\" style=\"width: 140px; height: 25 px; text-align: center;\"></input>"
            + "<input id=\"geoplus\" type=\"button\" style=\" width:25px; height:25px;\" value=\"+\"></input>";
    private static final String GEO_PART_WIDTH = "600px";
    private static final String CONTENT_ELEM_HEIGHT = "25px";
    private Property.ValueChangeListener dimListener;
    private Property.ValueChangeListener geoListener;
    private Dimension geoDimension;
    private Button btnGeo;
    private ComboBox boxGeo;
    private Property.ValueChangeListener graphListener;
    private Property.ValueChangeListener datasetListener;
    private Button btnVisualize;
    private Button btnInspect;
    private VerticalLayout inspectLayout;
    
    static {
        try {
            System.out.println(EstaLdComponent.class.getResourceAsStream("/logger.properties"));
            LogManager.getLogManager().readConfiguration(EstaLdComponent.class.getResourceAsStream("/logger.properties"));
        } catch (IOException ex) {
            Logger.getLogger(EstaLdComponent.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(EstaLdComponent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private Button btnAggregColor;
    
    public static class ValueWrapper {
        private final Value value;
        public ValueWrapper(Value value){
            this.value = value;
        }
        public Value getValue(){
            return value;
        }
        @Override
        public String toString(){
            return value.stringValue();
        }
    }
    
    public EstaLdComponent(Repository repository){
        this.repository = repository;
        
        createGUI();
        createDimAndGeoListeners();
        
        setCompositionRoot(mainLayout);
    }
    
    public EstaLdComponent(String endpoint){
        this.endpoint = endpoint;
        
        initializeRepository();
        createGUI();
        createDimAndGeoListeners();
        
        setCompositionRoot(mainLayout);
    }
    
    private void initializeRepository(){
        if (endpoint == null) {
//            repository = null;
//            dcRepo = new DummyDCRepository();
//            geoDimension = null;
//            return;
            endpoint = "http://localhost:8890/sparql";
        }
        repository = new SPARQLRepository(endpoint);
        try {
            repository.initialize();
        } catch (RepositoryException ex) {
            Logger.getLogger(EstaLdComponent.class.getName()).log(Level.SEVERE, null, ex);
        }
        dcRepo = new SparqlDCRepository(repository);
        geoDimension = null;
    }
    
    private void createGUI(){
        mainLayout = new VerticalLayout();
        mainLayout.setWidth("100%");
        mainLayout.setSpacing(true);
        datasetLayout = new HorizontalLayout();
        datasetLayout.setSpacing(true);
        datasetLayout.setWidth("100%");
        
        mainLayout.addComponent(datasetLayout);
        
        mainLayout.addComponent(new Label("<hr/>", Label.CONTENT_XHTML));
        
        contentLayout = new HorizontalLayout();
        contentLayout.setSizeFull();
        contentLayout.setWidth("100%");
        contentLayout.setSpacing(true);
        mainLayout.addComponent(contentLayout);
        
        createDataSetLayout();
    }
    
    private void createDimAndGeoListeners(){
        dimListener = new Property.ValueChangeListener() {
            public void valueChange(Property.ValueChangeEvent event) {
                if (dimNames == null || dimNames.length == 0){
                    getWindow().executeJavaScript("javaSetAll([],[],[]);");
                    return;
                }
                StringBuilder builderDims = new StringBuilder();
                StringBuilder builderVals = new StringBuilder();
                StringBuilder builderFree = new StringBuilder();
                for (int i=0; i<dimNames.length; i++){
                    Dimension d = (Dimension) dimNames[i].getData();
                    Value val = ((ValueWrapper) dimValues[i].getValue()).getValue();
                    if (val == null){
                        builderFree.append(",'").append(d.getUri()).append("'");
                    } else {
                        builderDims.append(",'").append(d.getUri()).append("'");
                        builderVals.append(",'");
                        if (val instanceof URI)
                            builderVals.append("<")
                                    .append(val.stringValue())
                                    .append(">");
                        else {
                            Literal l = (Literal)val;
                            builderVals.append("\"").append(l.stringValue()).append("\"");
                            URI dataType = l.getDatatype();
                            if (dataType != null && !dataType.stringValue().contains("string")) 
                                builderVals.append("^^<")
                                        .append(dataType.stringValue())
                                        .append(">");
                        }
                        builderVals.append("'");
                    }
                }
                String javaDims = (builderDims.length()==0)?"[]":"[" + builderDims.substring(1) + "]";
                String javaVals = (builderVals.length()==0)?"[]":"[" + builderVals.substring(1) + "]";
                String javaFree = (builderFree.length()==0)?"[]":"[" + builderFree.substring(1) + "]"; // This is unused!!!
                String function = "javaSetDimsVals(" + javaDims + "," + javaVals + ");";
                getWindow().executeJavaScript(function);
//                getWindow().executeJavaScript("javaPrintAll()");
            }
        };
        geoListener = new Property.ValueChangeListener() {
            public void valueChange(Property.ValueChangeEvent event) {
                if (geoDimension == null){
                    getWindow().executeJavaScript("javaSetGeoAll(null,[],null);");
                    return;
                }
                StringBuilder builder = new StringBuilder();
                Value val = ((ValueWrapper) boxGeo.getValue()).getValue();
                builder.append("javaSetGeoValue('");
                if (val instanceof URI)
                    builder.append("<").append(val.stringValue()).append(">");
                else {
                    URI dataType = ((Literal)val).getDatatype();
                    builder.append("\"").append(val.stringValue()).append("\"");
                    if (dataType != null && !dataType.stringValue().contains("string")) 
                        builder.append("^^<").append(dataType.stringValue()).append(">");
                }
                builder.append("')");
                
                getWindow().executeJavaScript(builder.toString());
            }
        };
    }
    
    public EstaLdComponent(){
        initializeRepository();
        createGUI();
        createDimAndGeoListeners();
        setCompositionRoot(mainLayout);
    }
    
    private void createDataSetLayout(){
        Button btnEndpoint = new Button("Endpoint");
        datasetLayout.addComponent(btnEndpoint);
        datasetLayout.setExpandRatio(btnEndpoint, 0.0f);
        datasetLayout.setComponentAlignment(btnEndpoint, Alignment.TOP_LEFT);
        
        Label lbl = new Label("Choose graph: ");
        lbl.setSizeUndefined();
        datasetLayout.addComponent(lbl);
        datasetLayout.setExpandRatio(lbl, 0.0f);
        datasetLayout.setComponentAlignment(lbl, Alignment.MIDDLE_LEFT);
        selectGraph = new ComboBox(null, dcRepo.getDataCubeGraphs());
        selectGraph.setImmediate(true);
        selectGraph.setNewItemsAllowed(false);
        selectGraph.setSizeUndefined();
        selectGraph.setWidth("100%");
        datasetLayout.addComponent(selectGraph);
        datasetLayout.setExpandRatio(selectGraph, 2.0f);
        datasetLayout.setComponentAlignment(selectGraph, Alignment.MIDDLE_LEFT);
        
        lbl = new Label("Choose dataset: ");
        lbl.setSizeUndefined();
        datasetLayout.addComponent(lbl);
        datasetLayout.setExpandRatio(lbl, 0.0f);
        datasetLayout.setComponentAlignment(lbl, Alignment.MIDDLE_LEFT);
        selectDataSet = new ComboBox(null, dcRepo.getDataSets());
        selectDataSet.setImmediate(true);
        selectDataSet.setNewItemsAllowed(false);
        selectDataSet.setNullSelectionAllowed(false);
        selectDataSet.setSizeUndefined();
        selectDataSet.setWidth("100%");
        datasetLayout.addComponent(selectDataSet);
        datasetLayout.setExpandRatio(selectDataSet, 2.0f);
        datasetLayout.setComponentAlignment(selectDataSet, Alignment.MIDDLE_LEFT);
        
        btnVisualize = new Button("Visualize");
        btnVisualize.addStyleName("btn-switch-view");
        btnVisualize.addStyleName("dim-name");
        btnVisualize.addStyleName("selected");
        datasetLayout.addComponent(btnVisualize);
        datasetLayout.setExpandRatio(btnVisualize, 0.0f);
        btnInspect = new Button("Inspect");
        btnInspect.addStyleName("btn-switch-view");
        btnInspect.addStyleName("dim-name");
        datasetLayout.addComponent(btnInspect);
        datasetLayout.setExpandRatio(btnInspect, 0.0f);
        btnVisualize.addListener(new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                btnVisualize.addStyleName("selected");
                btnInspect.removeStyleName("selected");
                
                inspectLayout.setVisible(false);
                contentLayout.setVisible(true);
//                refreshJS();
            }
        });
        btnInspect.addListener(new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                btnInspect.addStyleName("selected");
                btnVisualize.removeStyleName("selected");
                
                contentLayout.setVisible(false);
                inspectLayout.setVisible(true);
                
//                getWindow().open(new ExternalResource("http://localhost:8080/ESTA-LD/dsdrepo"), "_blank");
            }
        });
        
        graphListener = new Property.ValueChangeListener() {
            public void valueChange(Property.ValueChangeEvent event) {
                Object prop = event.getProperty().getValue();
                selectDataSet.removeAllItems();
                if (prop == null) {
                    if (dcRepo.getDataSets() == null) return;
                    boolean firstPass = true;
                    for (DataSet ds: dcRepo.getDataSets()){
                        selectDataSet.addItem(ds);
                        if (firstPass){
                            selectDataSet.select(ds);
                            firstPass = false;
                        }
                    }
                } else {
                    DataCubeGraph graph = (DataCubeGraph) prop;
                    if (graph.getDataSets() == null) return;
                    boolean firstPass = true;
                    for (DataSet ds: graph.getDataSets()){
                        selectDataSet.addItem(ds);
                        if (firstPass){
                            selectDataSet.select(ds);
                            firstPass = false;
                        }
                    }
                }
            }
        };
        selectGraph.addListener(graphListener);
        datasetListener = new Property.ValueChangeListener() {
            public void valueChange(Property.ValueChangeEvent event) {
                DataSet ds = (DataSet) event.getProperty().getValue();
                if (ds != null){
                    final String function = "javaSetGraphAndDataSet('" +
                            ds.getGraph() + "','" + ds.getUri() + "','" + 
                            endpoint + "')";
                    getWindow().executeJavaScript(function);
                    refreshDimensions();
                    
                    inspectLayout.removeAllComponents();
                    inspectLayout.addComponent(new InspectComponent(
                            repository, 
                            ds.getGraph(), 
                            ds.getUri()
                    ));
                }
            }
        };
        selectDataSet.addListener(datasetListener);
        
        btnEndpoint.addListener(new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                final EndpointWindow.EndpointState state = new EndpointWindow.EndpointState();
                state.endpoint = endpoint;
                Window w = new EndpointWindow(state);
                w.addListener(new Window.CloseListener() {
                    @Override
                    public void windowClose(Window.CloseEvent e) {
                        try {
                            if (!endpoint.equals(state.endpoint))
                                repository.shutDown();
                        } catch (RepositoryException ex) {
                            Logger.getLogger(EstaLdComponent.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        if (state.repository != null && state.repository.isInitialized()) {
                            repository = state.repository;
                            endpoint = state.endpoint;
                            endpointChanged();
                        }
                    }
                });
                getWindow().addWindow(w);
            }
        });
    }
    
    private void endpointChanged(){
        dcRepo = new SparqlDCRepository(repository);
        selectGraph.removeListener(graphListener);
        selectDataSet.removeListener(datasetListener);
        selectGraph.removeAllItems();
        selectDataSet.removeAllItems();
        if (!repository.isInitialized()) {
            getWindow().showNotification("Repo " + repository.toString() + " not initialized somehow", Window.Notification.TYPE_ERROR_MESSAGE);
        }
        Collection<DataCubeGraph> dcGraphs = dcRepo.getDataCubeGraphs();
        Collection<DataSet> dcDataSets = dcRepo.getDataSets();
        
        DataCubeGraph demoGraph = null;
        for (DataCubeGraph graph: dcGraphs){
            selectGraph.addItem(graph);
            if (endpoint.equals("http://jpo2.imp.bg.ac.rs/sparql") && graph.getUri().equals("http://demo/reg-dev/"))
                demoGraph = graph;
        }
        for (DataSet ds: dcDataSets)
            selectDataSet.addItem(ds);
        
        selectGraph.addListener(graphListener);
        selectDataSet.addListener(datasetListener);
        
        if (demoGraph != null)
            selectGraph.select(demoGraph);
        else if (dcGraphs.iterator().hasNext())
            selectGraph.select(dcGraphs.iterator().next());
    }
    
    private void refresh(){
        contentLayout.removeAllComponents();
        
        // create left part where the map goes
        // first create a vertical layout for components
        geoLayout = new VerticalLayout();
        geoLayout.setSizeUndefined();
        geoLayout.setWidth(GEO_PART_WIDTH);
        geoLayout.setSpacing(true);
        contentLayout.addComponent(geoLayout);
        contentLayout.setExpandRatio(geoLayout, 0.0f);
        // create Level and +- controls
        HorizontalLayout hl = new HorizontalLayout();
        hl.setSpacing(true);
        final Label levelLabel = new Label(LEVEL_LABEL_CONTENT, Label.CONTENT_XHTML);
        hl.addComponent(levelLabel);
        btnAggregColor = new Button("Aggregated Coloring");
        btnAggregColor.addStyleName("dim-name");
        btnAggregColor.addListener(new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                if (btnAggregColor.getStyleName().contains("selected")){
                    btnAggregColor.removeStyleName("selected");
                    getWindow().executeJavaScript("javaUnselectAggregColoring()");
                } else {
                    btnAggregColor.addStyleName("selected");
                    getWindow().executeJavaScript("javaSelectAggregColoring()");
                }
            }
        });
        hl.addComponent(btnAggregColor);
        geoLayout.addComponent(hl);
        // create a layout for the map
//        mapLayout = new VerticalLayout();
        mapLayout = new VerticalLayout() {
            @Override
            public void attach() {
                getWindow().executeJavaScript("console.log('Attached the map')");
            }
        };
        mapLayout.addListener(new ComponentAttachListener() {

            public void componentAttachedToContainer(ComponentAttachEvent event) {
                getWindow().executeJavaScript("console.log('ComponentAttachedListener triggered')");
            }
        });
        mapLayout.setSizeUndefined();
        mapLayout.setWidth("100%");
        mapLayout.setHeight("620px");
        mapLayout.setDebugId("map");
        mapLayout.addStyleName("leaflet-container");
        mapLayout.addStyleName("leaflet-fade-anim");
        geoLayout.addComponent(mapLayout);
        geoLayout.setExpandRatio(mapLayout, 2.0f);
        
        rightLayout = new VerticalLayout();
        rightLayout.setSizeUndefined();
        rightLayout.setWidth("100%");
        rightLayout.setSpacing(true);
        contentLayout.addComponent(rightLayout);
        contentLayout.setExpandRatio(rightLayout, 2.0f);
        dimLayout = new HorizontalLayout();
        dimLayout.setSizeFull();
        dimLayout.setWidth("100%");
        dimLayout.setSpacing(true);
        rightLayout.addComponent(dimLayout);
        refreshDimensions();
        chartLayout = new VerticalLayout();
        chartLayout.setSizeFull();
        chartLayout.setWidth("100%");
        chartLayout.setHeight("600px");
        chartLayout.setDebugId("highchartsbarsingle");
//        rightLayout.addComponent(chartLayout);
        VerticalLayout chartLayout2 = new VerticalLayout();
        chartLayout2.setSizeFull();
        chartLayout2.setWidth("100%");
        chartLayout2.setHeight("500px");
        chartLayout2.setDebugId("highchartsbarmultiple");
        rightLayout.addComponent(chartLayout2);
        
        inspectLayout = new VerticalLayout();
        inspectLayout.setVisible(false);
        inspectLayout.setWidth("100%");
        mainLayout.addComponent(inspectLayout);
    }
    
    private void refreshDimensions(){
        // clean everything just in case
        dimLayout.removeAllComponents();
        geoDimension = null;
        btnGeo = null;
        boxGeo = null;
        dimNames = null;
        dimValues = null;
        
        if (selectDataSet.getValue() == null) return;
        
        VerticalLayout lLayout = new VerticalLayout();
        lLayout.setSizeUndefined();
        lLayout.setSpacing(true);
        lLayout.setDebugId("dim-btn-layout");
        dimLayout.addComponent(lLayout);
        VerticalLayout rLayout = new VerticalLayout();
        rLayout.setSizeUndefined();
        rLayout.setSpacing(true);
        rLayout.setWidth("100%");
        dimLayout.addComponent(rLayout);
        dimLayout.setExpandRatio(rLayout, 2.0f);
        
        final DataSet ds = (DataSet) selectDataSet.getValue();
        LinkedList<Dimension> dimsForShow = new LinkedList<Dimension>();
        for (Dimension dim: ds.getStructure().getDimensions())
            if (!dim.isGeoDimension())
                dimsForShow.add(dim);
            else geoDimension = dim;
        dimNames = new Button[dimsForShow.size()];
        dimValues = new ComboBox[dimsForShow.size()];
        int i=0;
        
        StringBuilder builderPossibleValues = new StringBuilder();
        boolean firstPass = true;
        
        for (Dimension dim: dimsForShow){
            // add dimension pick
            // first create a button to represent dimension name
            final Button btnName = new Button(dim.toString());
            btnName.setSizeUndefined();
            btnName.setWidth("100%");
            btnName.setHeight(CONTENT_ELEM_HEIGHT);
            btnName.setData(dim);
            btnName.addStyleName("dim-name");
            if (firstPass){
                btnName.addStyleName("selected");
                firstPass = false;
            }
            btnName.addListener(new Button.ClickListener() {
                public void buttonClick(Button.ClickEvent event) {
                    if (btnName.getStyleName().contains("selected")){
                        btnName.removeStyleName("selected");
                    } else {
                        btnName.addStyleName("selected");
                    }
                    freeDimensionsChanged();
                }
            });
            dimNames[i] = btnName;
            
            // create a combo box for picking dimension value
            Collection<Value> vals = ds.getValuesForDimension(dim);
            Collection<ValueWrapper> valsWrapped = new LinkedList<ValueWrapper>();
            for (Value v: vals) valsWrapped.add(new ValueWrapper(v));
            builderPossibleValues.append(",").append(stringifyCollection(vals));
            ComboBox boxValue = new ComboBox(null, valsWrapped);
            boxValue.setImmediate(true);
            boxValue.setNullSelectionAllowed(false);
            if (valsWrapped.iterator().hasNext()) boxValue.select(valsWrapped.iterator().next());
            else boxValue.setEnabled(false);
            boxValue.setSizeUndefined();
            boxValue.setWidth("100%");
            boxValue.setHeight(CONTENT_ELEM_HEIGHT);
            boxValue.addStyleName("dim-value");
            boxValue.addListener(dimListener);
            dimValues[i] = boxValue;
            
            // put them in a horizontal layout and add to the view
//            HorizontalLayout layout = new HorizontalLayout();
//            layout.setSizeUndefined();
//            layout.setWidth("100%");
//            layout.setSpacing(true);
//            dimLayout.addComponent(layout);
//            dimLayout.setExpandRatio(layout, 2.0f);
//            layout.addComponent(btnName);
//            layout.addComponent(boxValue);
//            layout.setExpandRatio(boxValue, 2.0f);
            lLayout.addComponent(btnName);
            lLayout.setExpandRatio(btnName, 2.0f);
            rLayout.addComponent(boxValue);
            rLayout.setComponentAlignment(boxValue, Alignment.BOTTOM_LEFT);
            i++;
        }
        if (geoDimension != null){
            btnGeo = new Button(geoDimension.toString());
            btnGeo.setSizeUndefined();
            btnGeo.setWidth("100%");
            btnGeo.setHeight(CONTENT_ELEM_HEIGHT);
            btnGeo.setData(geoDimension);
            btnGeo.addStyleName("geo-name");
            btnGeo.addListener(new Button.ClickListener() {
                public void buttonClick(Button.ClickEvent event) {
                    if (btnGeo.getStyleName().contains("selected")){
                        btnGeo.removeStyleName("selected");
                    } else {
                        btnGeo.addStyleName("selected");
                    }
                    freeDimensionsChanged();
                }
            });
            
            StringBuilder builder = new StringBuilder();
            Collection<Value> posVals = ds.getValuesForDimension(geoDimension);
            Collection<ValueWrapper> posValsWrapped = new LinkedList<ValueWrapper>();
            for (Value v: posVals) posValsWrapped.add(new ValueWrapper(v));
            Value selectedVal = posVals.iterator().next();
            String selectedValString = "";
            if (selectedVal instanceof URI) {
                selectedValString = "<" + selectedVal.stringValue() + ">";
            } else {
                selectedValString = "\"" + selectedVal.stringValue() + "\"";
                URI dataType = ((Literal)selectedVal).getDatatype();
                if (dataType != null && !dataType.stringValue().contains("string")) {
                    selectedValString += "^^<" + dataType.stringValue() + ">";
                }
            }
            builder.append("javaSetGeoAll('").append(geoDimension.getUri());
            builder.append("',").append(stringifyCollection(posVals));
            builder.append(",'").append(selectedValString).append("')");
            boxGeo = new ComboBox(null, posValsWrapped);
            boxGeo.setDebugId("geoValue");
            boxGeo.setData(posVals);
            boxGeo.setImmediate(true);
            boxGeo.setNullSelectionAllowed(false);
            boxGeo.select(posValsWrapped.iterator().next());
            boxGeo.setSizeUndefined();
            boxGeo.setWidth("100%");
            boxGeo.setHeight(CONTENT_ELEM_HEIGHT);
            boxGeo.addStyleName("geo-value");
            boxGeo.addListener(geoListener);
            
            lLayout.addComponent(btnGeo);
            lLayout.setExpandRatio(btnGeo, 2.0f);
            rLayout.addComponent(boxGeo);
            rLayout.setComponentAlignment(boxGeo, Alignment.BOTTOM_LEFT);
            
            getWindow().executeJavaScript(builder.toString());
        } else {
            getWindow().executeJavaScript("javaSetGeoAll('',[],'')");
        }
        // TODO cover the case where there is more than 1 geo dimension
        
        builderPossibleValues.replace(0, 1, "javaSetPossibleValues([");
        builderPossibleValues.append("])");
        getWindow().executeJavaScript(builderPossibleValues.toString());
        dimListener.valueChange(null);
    }

    @Override
    public void detach() {
        super.detach(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void attach() {
        super.attach();
        refresh();
        Iterator<DataCubeGraph> iter = dcRepo.getDataCubeGraphs().iterator();
        DataCubeGraph g = null;
        while (iter.hasNext()){
            g = iter.next();
            if (g.getUri().equalsIgnoreCase("http://demo/reg-dev-polygons/")){
                break;
            }
        }
        if (g != null) selectGraph.select(g);
    }
    
    public void refreshJS(){
        dimListener.valueChange(null);
    }

    private String stringifyCollection(Collection<Value> vals) {
        StringBuilder builder = new StringBuilder();
        for (Value s: vals) {
            builder.append(",'");
            if (s instanceof URI)
                builder.append("<").append(s.stringValue()).append(">");
            else {
                builder.append("\"").append(s.stringValue()).append("\"");
                URI dataType = ((Literal)s).getDatatype();
                if (dataType != null && !dataType.stringValue().contains("string"))
                    builder.append("^^<").append(dataType.stringValue()).append(">");
            }
            builder.append("'");
        }
        return builder.replace(0, 1, "[").append("]").toString();
    }
    
    private void freeDimensionsChanged() {
        // first determine if geo is selected (and exists) because that one doesn't draw graphs
        String selection = null;
        if (btnGeo != null){
            if (btnGeo.getStyleName().contains("selected")) {
                selection = "true";
            }
            else {
                selection = "false";
            }
        } else {
            selection = "false";
        }
        getWindow().executeJavaScript("javaSetGeoFree(" + selection + ")");
        
        StringBuilder builder = new StringBuilder();
        for (int i=0; i<dimNames.length; i++){
            if (dimNames[i].getStyleName().contains("selected"))
                builder.append(",").append(i);
        }
        if (builder.length() > 0)
            builder.replace(0, 1, "javaSetFreeDimensions([").append("])");
        else 
            builder.append("javaSetFreeDimensions([])");
        getWindow().executeJavaScript(builder.toString());
    }
    
}
