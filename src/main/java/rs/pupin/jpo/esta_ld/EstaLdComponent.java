/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package rs.pupin.jpo.esta_ld;

import com.vaadin.data.Property;
import com.vaadin.event.LayoutEvents;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
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
import org.vaadin.jouni.animator.AnimatorProxy;
import org.vaadin.jouni.animator.client.ui.VAnimatorProxy;
import rs.pupin.jpo.datacube.DataCubeGraph;
import rs.pupin.jpo.datacube.DataCubeRepository;
import rs.pupin.jpo.datacube.DataSet;
import rs.pupin.jpo.datacube.Dimension;
import rs.pupin.jpo.datacube.Measure;
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
    private GridLayout dimLayout;
    private VerticalLayout mapLayout;
    private VerticalLayout rightLayout;
    private VerticalLayout chartLayout;
    private HorizontalLayout datasetLayout;
    private HorizontalSplitPanel contentLayout;
    private DataCubeRepository dcRepo;
    private ComboBox selectGraph;
    private ComboBox selectDataSet;
    private Button[] dimNames;
    private ComboBox[] dimValues;
    private static final String LEVEL_LABEL_CONTENT = 
//            "<span>Geo granularity level  </span>"
//            + "<input id=\"geominus\" type=\"button\" style=\"height:100%;\" value=\"-\"></input>"
//            + "<input id=\"geoLevelLabel\" type=\"text\" readonly=\"\" value=\"Level 0\" style=\"width: 140px; text-align: center;\"></input>"
//            + "<input id=\"geoplus\" type=\"button\" style=\"height:100%;\" value=\"+\"></input> ";
            "Geo granularity level &nbsp; "
            + "<input id=\"geominus\" type=\"button\" style=\" width:25px; height:25px;\" value=\"-\"></input>"
            + "<input id=\"geoLevelLabel\" type=\"text\" readonly=\"\" value=\"Level 0\" style=\"width: 140px; height: 25 px; text-align: center;\"></input>"
            + "<input id=\"geoplus\" type=\"button\" style=\" width:25px; height:25px;\" value=\"+\"></input> ";
    private static final String GEO_PART_WIDTH = "600px";
    private static final String CONTENT_ELEM_HEIGHT = "25px";
    private static final String CONTENT_ELEM_WIDTH = "150px";
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
    private Dimension timeDimension;
    private Collection<Measure> measures;
    private Button measName;
    private ComboBox measValues;
    private HorizontalLayout brandLayout;
    private VerticalLayout settingsLayout;
    private Button btnInvert;
    private Button btnStack;
    private AnimatorProxy animator;
    private boolean indSettingsVisible;
    private boolean indAnimatorEnabled = false;
    private boolean indShowInspect = false;
    private String graph;
    private Button btnSwap;
    private Label[] dimAggregIndicators;
    private Label btnAggregGeo;
    
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
        
        setSizeFull();
        createGUI();
        createDimAndGeoListeners();
        
        setCompositionRoot(mainLayout);
    }
    
    public EstaLdComponent(String endpoint, String graph){
        this.endpoint = endpoint;
        this.graph = graph;
        
        setSizeFull();
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
            endpoint = "http://geoknow.imp.bg.ac.rs/sparql";
        }
        repository = new SPARQLRepository(endpoint);
        dcRepo = null;
        try {
            repository.initialize();
        } catch (RepositoryException ex) {
            Logger.getLogger(EstaLdComponent.class.getName()).log(Level.SEVERE, null, ex);
            Logger.getLogger(EstaLdComponent.class.getName()).log(Level.WARNING, "Couldn't connect to the endpoint, setting dummy DC repo");
            dcRepo = new DummyDCRepository();
        }
        if (dcRepo == null) {
            Logger.getLogger(EstaLdComponent.class.getName()).log(Level.FINE, "Creating DC repo");
            dcRepo = new SparqlDCRepository(repository);
            if (dcRepo.getDataCubeGraphs() == null) {
                Logger.getLogger(EstaLdComponent.class.getName()).log(Level.WARNING, "There was an error connecting to the endpoint, setting dummy DC repo");
                dcRepo = new DummyDCRepository();
            }
        }
        geoDimension = null;
    }
    
    private void createGUI(){
        mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.setSpacing(false);
        mainLayout.setDebugId("l-main");
        
        if (indAnimatorEnabled) {
            animator = new AnimatorProxy();
            mainLayout.addComponent(animator);
            indSettingsVisible = true;
        }
        
        brandLayout = new HorizontalLayout();
        brandLayout.setSpacing(true);
        brandLayout.setMargin(true);
        brandLayout.setWidth("100%");
        brandLayout.setDebugId("l-brand");
        Label brandSpan = new Label("<span id='brand'>ESTA-LD</span>", Label.CONTENT_XHTML);
        brandLayout.addComponent(brandSpan);
        brandLayout.setExpandRatio(brandSpan, 2.0f);
        brandLayout.setComponentAlignment(brandSpan, Alignment.MIDDLE_LEFT);
        Button btnEndpoint = new Button("Endpoint");
        brandLayout.addComponent(btnEndpoint);
        brandLayout.setComponentAlignment(btnEndpoint, Alignment.MIDDLE_RIGHT);
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
        btnInspect = new Button("Inspect");
        if (indShowInspect) {
            brandLayout.addComponent(btnInspect);
            brandLayout.setExpandRatio(btnInspect, 0.0f);
        }
        Button btnSettings = new Button("Parameters");
        brandLayout.addComponent(btnSettings);
        brandLayout.setComponentAlignment(btnSettings, Alignment.MIDDLE_RIGHT);
        btnSettings.addListener(new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
//                getWindow().executeJavaScript("$('#l-dataset').parent().parent().slideToggle(function(){ vaadin.forceLayout(); })");
                
                if (!indAnimatorEnabled) {
                    getWindow().executeJavaScript("$('#l-settings').parent().parent().slideToggle()");
                    settingsLayout.setVisible(!settingsLayout.isVisible());
                } else {
//                    if (indSettingsVisible) {
//                        animator.animate(settingsLayout, VAnimatorProxy.AnimType.ROLL_UP_CLOSE);
//                    } else {
//                        animator.animate(settingsLayout, VAnimatorProxy.AnimType.ROLL_DOWN_OPEN_POP);
//                    }
//                    indSettingsVisible = !indSettingsVisible;
                }
                
//                getWindow().executeJavaScript("setTimeout(function(){ vaadin.forceSync(); map.invalidateSize(); }, 0)");
//                getWindow().executeJavaScript("setTimeout(function(){ runSparqlDimensionValueChangedVuk(); map.invalidateSize() }, 200)");
//                getWindow().executeJavaScript("setTimeout(function(){ currentChart.reflow(); map.invalidateSize() }, 200)");
            }
        });
        
        settingsLayout = new VerticalLayout();
        settingsLayout.setDebugId("l-settings");
        settingsLayout.setSpacing(true);
        settingsLayout.setMargin(true);
        settingsLayout.setWidth("100%");
        
        datasetLayout = new HorizontalLayout();
        datasetLayout.setSpacing(true);
        datasetLayout.setWidth("100%");
        datasetLayout.setDebugId("l-dataset");
        
        mainLayout.addComponent(brandLayout);
        mainLayout.setExpandRatio(brandLayout, 0.0f);
        settingsLayout.addComponent(datasetLayout);
        Label lblSettingsSeparator = new Label("<hr/>", Label.CONTENT_XHTML);
        lblSettingsSeparator.addStyleName("settings-separator");
        settingsLayout.addComponent(lblSettingsSeparator);
        mainLayout.addComponent(settingsLayout);
        mainLayout.setExpandRatio(settingsLayout, 0.0f);
        
        // in place of this divide borders and shadows will be added
//        Label lblDivider = new Label("<hr/>", Label.CONTENT_XHTML);
//        mainLayout.addComponent(lblDivider);
//        mainLayout.setExpandRatio(lblDivider, 0.0f);
        
//        contentLayout = new HorizontalLayout();
        contentLayout = new HorizontalSplitPanel();
        contentLayout.setMargin(true);
        contentLayout.setSizeFull();
        contentLayout.setWidth("100%");
//        contentLayout.setSpacing(true);
        contentLayout.setSplitPosition(50, UNITS_PERCENTAGE);
        contentLayout.setDebugId("l-content");
        mainLayout.addComponent(contentLayout);
        mainLayout.setExpandRatio(contentLayout, 2.0f);
        
        createDataSetLayout();
    }
    
    private void createDimAndGeoListeners(){
        dimListener = new Property.ValueChangeListener() {
            public void valueChange(Property.ValueChangeEvent event) {
                if (dimNames == null || dimNames.length == 0){
                    getWindow().executeJavaScript("javaSetAll([],[],[]);");
                    return;
                }
                if (measures != null && measures.size() > 1) {
                    Measure m = (Measure)measValues.getValue();
                    getWindow().executeJavaScript("javaSelectMeasure('" + m.getUri() + "')");
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
                String execPopulate = "";
                if (event == null) execPopulate = ", true";
                String function = "javaSetDimsVals(" + javaDims + "," + javaVals + execPopulate + ");";
                getWindow().executeJavaScript(function);
//                getWindow().executeJavaScript("javaPrintAll()");
            }
        };
        geoListener = new Property.ValueChangeListener() {
            public void valueChange(Property.ValueChangeEvent event) {
                if (geoDimension == null){
                    getWindow().executeJavaScript("javaSetGeoAll(null,[],null,true);");
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
        Label lbl = new Label("Graph: ");
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
        
        lbl = new Label(" Dataset: ");
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
//        datasetLayout.addComponent(btnVisualize);
//        datasetLayout.setExpandRatio(btnVisualize, 0.0f);
//        btnInspect = new Button("Inspect");
//        btnInspect.addStyleName("btn-switch-view");
//        btnInspect.addStyleName("dim-name");
//        datasetLayout.addComponent(btnInspect);
//        datasetLayout.setExpandRatio(btnInspect, 0.0f);
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
//                btnInspect.addStyleName("selected");
//                btnVisualize.removeStyleName("selected");
                
//                contentLayout.setVisible(false);
//                inspectLayout.setVisible(true);
                
                String extForm = getWindow().getURL().toExternalForm();
                int end = extForm.lastIndexOf("/ESTA-LD") + 8;
                String targetURL = extForm.substring(0, end) + "/inspect";
                getWindow().executeJavaScript("window.open('" + targetURL + "','_blank')");
//                getWindow().open(new ExternalResource(targetURL, "_blank"));
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
            if (endpoint.equals("http://geoknow.imp.bg.ac.rs/sparql") && graph.getUri().equals("http://demo/reg-dev-polygons/"))
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
        geoLayout.setDebugId("l-geo");
        geoLayout.setSizeUndefined();
//        geoLayout.setWidth(GEO_PART_WIDTH);
//        geoLayout.setHeight("100%");
        geoLayout.setSizeFull();
        geoLayout.setSpacing(true);
        contentLayout.addComponent(geoLayout);
        getWindow().executeJavaScript("$('div#l-geo').append('<div id=\"esta-map-popup\"><p></p></div>')");
//        contentLayout.setExpandRatio(geoLayout, 0.0f);
        // create Level and +- controls
        HorizontalLayout hl = new HorizontalLayout();
        hl.setSpacing(true);
        final Label levelLabel = new Label(LEVEL_LABEL_CONTENT, Label.CONTENT_XHTML);
        hl.addComponent(levelLabel);
        hl.setComponentAlignment(levelLabel, Alignment.MIDDLE_LEFT);
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
        geoLayout.setExpandRatio(hl, 0.0f);
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
//        mapLayout.setHeight("620px");
        mapLayout.setHeight("100%");
        mapLayout.setDebugId("map");
        mapLayout.addStyleName("leaflet-container");
        mapLayout.addStyleName("leaflet-fade-anim");
        geoLayout.addComponent(mapLayout);
        geoLayout.setExpandRatio(mapLayout, 2.0f);
        
        rightLayout = new VerticalLayout();
        rightLayout.setSizeUndefined();
        rightLayout.setWidth("100%");
        rightLayout.setSizeFull();
        rightLayout.setSpacing(true);
        contentLayout.addComponent(rightLayout);
//        contentLayout.setExpandRatio(rightLayout, 2.0f);
        dimLayout = new GridLayout(6, 1);
        dimLayout.setColumnExpandRatio(0, 0.0f);
        dimLayout.setColumnExpandRatio(1, 0.0f);
        dimLayout.setColumnExpandRatio(2, 2.0f);
        dimLayout.setColumnExpandRatio(3, 0.0f);
        dimLayout.setColumnExpandRatio(4, 0.0f);
        dimLayout.setColumnExpandRatio(5, 2.0f);
        dimLayout.setDebugId("dim-layout");
//        dimLayout.setSizeFull();
        dimLayout.setWidth("100%");
        dimLayout.setSpacing(true);
//        rightLayout.addComponent(dimLayout);
        settingsLayout.addComponent(dimLayout);
        
        dimLayout.addListener(new LayoutEvents.LayoutClickListener() {
            public void layoutClick(LayoutEvents.LayoutClickEvent event) {
                Component btnAggreg = event.getClickedComponent();
                if (!(btnAggreg instanceof Label)) return;
                if (btnAggreg.getStyleName().contains("selected")) {
                    btnAggreg.removeStyleName("selected");
                } else {
                    btnAggreg.addStyleName("selected");
                }
                aggregDimensionsChanged();
            }
        });
        
        refreshDimensions();
        
        HorizontalLayout chartControlsLayout = new HorizontalLayout();
        chartControlsLayout.setDebugId("l-chart-controls");
        chartControlsLayout.setWidth("100%");
        chartControlsLayout.setSpacing(true);
        rightLayout.addComponent(chartControlsLayout);
        rightLayout.setExpandRatio(chartControlsLayout, 0.0f);
        btnSwap = new Button("Swap");
        btnSwap.setDebugId("btn-swap");
        btnSwap.addListener(new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                getWindow().executeJavaScript("toggleSwap()");
            }
        });
        chartControlsLayout.addComponent(btnSwap);
        btnInvert = new Button("Switch Axes");
        btnInvert.setDebugId("btn-invert");
        btnInvert.addListener(new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                getWindow().executeJavaScript("toggleInvert()");
            }
        });
        chartControlsLayout.addComponent(btnInvert);
        btnStack = new Button("Stack");
        btnStack.setDebugId("btn-stack");
        btnStack.addListener(new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                getWindow().executeJavaScript("toggleStacking()");
            }
        });
        chartControlsLayout.addComponent(btnStack);
        Label lblEmpty = new Label(" ");
        chartControlsLayout.addComponentAsFirst(lblEmpty);
        chartControlsLayout.setExpandRatio(lblEmpty, 2.0f);
        
        chartLayout = new VerticalLayout();
        chartLayout.setSizeFull();
        chartLayout.setDebugId("highchartsbarsingle");
//        rightLayout.addComponent(chartLayout);
        VerticalLayout chartLayout2 = new VerticalLayout();
        chartLayout2.setSizeFull();
        chartLayout2.setDebugId("highchartsbarmultiple");
        rightLayout.addComponent(chartLayout2);
        rightLayout.setExpandRatio(chartLayout2, 2.0f);
        
        inspectLayout = new VerticalLayout();
        inspectLayout.setVisible(false);
        inspectLayout.setWidth("100%");
        inspectLayout.setDebugId("l-inspect");
//        mainLayout.addComponent(inspectLayout);
//        mainLayout.setExpandRatio(inspectLayout, 2.0f);
    }
    
    private void refreshDimensions(){
        // clean everything just in case
        dimLayout.removeAllComponents();
        geoDimension = null;
        timeDimension = null;
        measures = null;
        btnGeo = null;
        boxGeo = null;
        dimNames = null;
        dimValues = null;
        
        if (selectDataSet.getValue() == null) return;
        
//        VerticalLayout lLayout = new VerticalLayout();
//        lLayout.setSizeUndefined();
//        lLayout.setSpacing(true);
//        lLayout.setDebugId("dim-btn-layout");
//        dimLayout.addComponent(lLayout);
//        VerticalLayout rLayout = new VerticalLayout();
//        rLayout.setSizeUndefined();
//        rLayout.setSpacing(true);
//        rLayout.setWidth("100%");
//        dimLayout.addComponent(rLayout);
//        dimLayout.setExpandRatio(rLayout, 2.0f);
        
        final DataSet ds = (DataSet) selectDataSet.getValue();
        measures = ds.getStructure().getMeasures();
        StringBuilder builderMeasures = new StringBuilder();
        for (Measure m: measures){
            builderMeasures.append(", '");
            builderMeasures.append(m.getUri());
            builderMeasures.append("'");
        }
        builderMeasures.replace(0, 2, "javaSetMeasures([");
        builderMeasures.append("])");
        getWindow().executeJavaScript(builderMeasures.toString());
        measName = new Button("Measure");
//        measName.setSizeUndefined();
//        measName.setWidth("100%");
        measName.setHeight(CONTENT_ELEM_HEIGHT);
        measName.setWidth(CONTENT_ELEM_WIDTH);
        measName.addStyleName("dim-name");
        measName.addStyleName("unselectable");
        measValues = new ComboBox(null, measures);
        measValues.setImmediate(true);
        measValues.setSizeUndefined();
        measValues.setWidth("100%");
        measValues.setHeight(CONTENT_ELEM_HEIGHT);
        measValues.addStyleName("dim-value");
        measValues.setNullSelectionAllowed(false);
        measValues.select(measures.iterator().next());
        measValues.addListener(dimListener);
//        measValues.addListener(new Property.ValueChangeListener() {
//            public void valueChange(Property.ValueChangeEvent event) {
//                Measure m = (Measure)event.getProperty().getValue();
//                // put measure in
//            }
//        });
        int rowIndex = 0;
        int columnIndex = 0;
        dimLayout.addComponent(measName, columnIndex, rowIndex);
        columnIndex++;
//        dimLayout.setExpandRatio(measName, 2.0f);
        dimLayout.addComponent(measValues, columnIndex, rowIndex, columnIndex+1, rowIndex);
        columnIndex+=2;
//        dimLayout.setComponentAlignment(measValues, Alignment.BOTTOM_LEFT);
        LinkedList<Dimension> dimsForShow = new LinkedList<Dimension>();
        for (Dimension dim: ds.getStructure().getDimensions()){
            if (dim.isGeoDimension())
                geoDimension = dim;
            else if (dim.isTimeDimension()) {
                timeDimension = dim;
                dimsForShow.addFirst(dim);
            }
            else 
                dimsForShow.add(dim);
        }
        dimNames = new Button[dimsForShow.size()];
        dimAggregIndicators = new Label[dimsForShow.size()];
        dimValues = new ComboBox[dimsForShow.size()];
        int i=0;
        
        StringBuilder builderPossibleValues = new StringBuilder();
        boolean firstPass = true;
        
        for (Dimension dim: dimsForShow){
            // add dimension pick
            // first create a button to represent dimension name
            final Button btnName = new Button(dim.toString());
//            btnName.setSizeUndefined();
//            btnName.setWidth("100%");
            btnName.setHeight(CONTENT_ELEM_HEIGHT);
            btnName.setWidth(CONTENT_ELEM_WIDTH);
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
            
            final Label btnAggreg = new Label("<span>&Sigma;</span>", Label.CONTENT_XHTML);
            btnAggreg.setWidth("30px");
            btnAggreg.setHeight(CONTENT_ELEM_HEIGHT);
            btnAggreg.setData(dim);
            btnAggreg.addStyleName("dim-name");
            btnAggreg.addStyleName("dim-aggreg");
            // this will have to go to the layout listener
//            btnAggreg.addListener(new Button.ClickListener() {
//                public void buttonClick(Button.ClickEvent event) {
//                    if (btnAggreg.getStyleName().contains("selected")) {
//                        btnAggreg.removeStyleName("selected");
//                    } else {
//                        btnAggreg.addStyleName("selected");
//                        aggregDimensionsChanged();
//                    }
//                }
//            });
            dimAggregIndicators[i] = btnAggreg;
            
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
            
//            lLayout.addComponent(btnName);
//            lLayout.setExpandRatio(btnName, 2.0f);
//            rLayout.addComponent(boxValue);
//            rLayout.setComponentAlignment(boxValue, Alignment.BOTTOM_LEFT);
            dimLayout.addComponent(btnName, columnIndex, rowIndex);
            if (++columnIndex == 6) {
                columnIndex = 0;
                rowIndex++;
                dimLayout.setRows(rowIndex+1);
            }
            dimLayout.addComponent(btnAggreg, columnIndex, rowIndex);
            if (++columnIndex == 6) {
                columnIndex = 0;
                rowIndex++;
                dimLayout.setRows(rowIndex+1);
            }
            dimLayout.addComponent(boxValue, columnIndex, rowIndex);
            if (++columnIndex == 6) {
                columnIndex = 0;
                rowIndex++;
                dimLayout.setRows(rowIndex+1);
            }
            i++;
        }
        
        if (timeDimension != null)
            getWindow().executeJavaScript("javaSetHasTimeDimension(true)");
        else
            getWindow().executeJavaScript("javaSetHasTimeDimension(false)");
        
        if (geoDimension != null){
            btnGeo = new Button(geoDimension.toString());
//            btnGeo.setSizeUndefined();
//            btnGeo.setWidth("100%");
            btnGeo.setHeight(CONTENT_ELEM_HEIGHT);
            btnGeo.setWidth(CONTENT_ELEM_WIDTH);
            btnGeo.setData(geoDimension);
            btnGeo.addStyleName("dim-name");
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
            
            btnAggregGeo = new Label("<span>&Sigma;</span>", Label.CONTENT_XHTML);
            btnAggregGeo.setHeight(CONTENT_ELEM_HEIGHT);
            btnAggregGeo.setData(geoDimension);
            btnAggregGeo.addStyleName("dim-name");
            btnAggregGeo.addStyleName("dim-aggreg");
            
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
            builder.append(",'").append(selectedValString).append("',true)");
            boxGeo = new ComboBox(null, posValsWrapped);
            boxGeo.setData(posVals);
            boxGeo.setImmediate(true);
            boxGeo.setNullSelectionAllowed(false);
            boxGeo.select(posValsWrapped.iterator().next());
            boxGeo.setSizeUndefined();
            boxGeo.setWidth("100%");
            boxGeo.setHeight(CONTENT_ELEM_HEIGHT);
            boxGeo.addStyleName("geo-value");
            boxGeo.addListener(geoListener);
//            lLayout.addComponent(btnGeo);
//            lLayout.setExpandRatio(btnGeo, 2.0f);
//            rLayout.addComponent(boxGeo);
//            rLayout.setComponentAlignment(boxGeo, Alignment.BOTTOM_LEFT);
            dimLayout.addComponent(btnGeo, columnIndex, rowIndex);
            columnIndex++;
            dimLayout.addComponent(btnAggregGeo, columnIndex, rowIndex);
            columnIndex++;
            dimLayout.addComponent(boxGeo, columnIndex, rowIndex);
            columnIndex++;
            
            getWindow().executeJavaScript(builder.toString());
        } else {
            getWindow().executeJavaScript("javaSetGeoAll('',[],'',true)");
        }
        // TODO cover the case where there is more than 1 geo dimension
        
        builderPossibleValues.replace(0, 1, "javaSetPossibleValues([");
        builderPossibleValues.append("])");
        getWindow().executeJavaScript(builderPossibleValues.toString());
        if (dimsForShow.isEmpty()) {
            if (geoDimension != null) getWindow().executeJavaScript("javaSetGeoFree(true)");
            else getWindow().executeJavaScript("javaSetGeoFree(false)");
            getWindow().executeJavaScript("javaSetFreeDimensions([], true)");
        } else {
            getWindow().executeJavaScript("javaSetGeoFree(false)");
            getWindow().executeJavaScript("javaSetFreeDimensions([0], true)");
        }
        dimListener.valueChange(null);
        getWindow().executeJavaScript("setTimeout(expandDimNameButtons(),200)");
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
        String targetGraph = "http://demo/reg-dev-polygons/";
        if (graph != null) targetGraph = graph;
        while (iter.hasNext()){
            g = iter.next();
            if (g.getUri().equalsIgnoreCase(targetGraph)){
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
    
    private void aggregDimensionsChanged() {
        StringBuilder builder = new StringBuilder();
        for (int i=0; i<dimAggregIndicators.length; i++) {
            if (dimAggregIndicators[i].getStyleName().contains("selected"))
                builder.append(",").append(i);
        }
        String isGeoAggregated = "false";
        if (geoDimension != null && btnAggregGeo.getStyleName().contains("selected")) {
            isGeoAggregated = "true";
        }
        if (builder.length() > 0)
            builder.replace(0, 1, "javaSetAggregDimensions([").append("],").append(isGeoAggregated).append(")");
        else 
            builder.append("javaSetAggregDimensions([],").append(isGeoAggregated).append(")");
        getWindow().executeJavaScript(builder.toString());
    }
    
}
