/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package rs.pupin.jpo.esta_ld;

import com.vaadin.data.Property;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sparql.SPARQLRepository;
import rs.pupin.jpo.datacube.DataCubeGraph;
import rs.pupin.jpo.datacube.DataSet;
import rs.pupin.jpo.datacube.Dimension;
import rs.pupin.jpo.datacube.sparql_impl.SparqlDCRepository;

/**
 *
 * @author vukm
 */
public class EstaLdComponent extends CustomComponent {
    
    private final Repository repository;
    private final String endpoint = "http://fraunhofer2.imp.bg.ac.rs/sparql";
    private final VerticalLayout mainLayout;
    private VerticalLayout geoLayout;
    private HorizontalLayout dimLayout;
    private VerticalLayout mapLayout;
    private VerticalLayout rightLayout;
    private VerticalLayout chartLayout;
    private HorizontalLayout datasetLayout;
    private HorizontalLayout contentLayout;
    private SparqlDCRepository dcRepo;
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
    
    public EstaLdComponent(Repository repository){
        this.repository = repository;
        
        mainLayout = new VerticalLayout();
        setCompositionRoot(mainLayout);
    }
    
    public EstaLdComponent(){
        repository = new SPARQLRepository(endpoint);
        try {
            repository.initialize();
        } catch (RepositoryException ex) {
            Logger.getLogger(EstaLdComponent.class.getName()).log(Level.SEVERE, null, ex);
        }
        dcRepo = new SparqlDCRepository(repository);
        geoDimension = null;
        
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
                    String val = (String) dimValues[i].getValue();
                    if (val == null){
                        builderFree.append(",'").append(d.getUri()).append("'");
                    } else {
                        builderDims.append(",'").append(d.getUri()).append("'");
                        builderVals.append(",'").append(val).append("'");
                    }
                }
                String javaDims = (builderDims.length()==0)?"[]":"[" + builderDims.substring(1) + "]";
                String javaVals = (builderVals.length()==0)?"[]":"[" + builderVals.substring(1) + "]";
                String javaFree = (builderFree.length()==0)?"[]":"[" + builderFree.substring(1) + "]";
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
                builder.append("javaSetGeoValue('");
                builder.append((String)boxGeo.getValue());
                builder.append("')");
                
                getWindow().executeJavaScript(builder.toString());
            }
        };
        
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
    
    private void createDataSetLayout(){
        Label lbl = new Label("Choose graph: ");
        lbl.setSizeUndefined();
        datasetLayout.addComponent(lbl);
        datasetLayout.setExpandRatio(lbl, 0.0f);
        selectGraph = new ComboBox(null, dcRepo.getDataCubeGraphs());
        selectGraph.setImmediate(true);
        selectGraph.setNewItemsAllowed(false);
        selectGraph.setSizeUndefined();
        selectGraph.setWidth("100%");
        datasetLayout.addComponent(selectGraph);
        datasetLayout.setExpandRatio(selectGraph, 2.0f);
        
        lbl = new Label("Choose dataset: ");
        lbl.setSizeUndefined();
        datasetLayout.addComponent(lbl);
        datasetLayout.setExpandRatio(lbl, 0.0f);
        selectDataSet = new ComboBox(null, dcRepo.getDataSets());
        selectDataSet.setImmediate(true);
        selectDataSet.setNewItemsAllowed(false);
        selectDataSet.setNullSelectionAllowed(false);
        selectDataSet.setSizeUndefined();
        selectDataSet.setWidth("100%");
        datasetLayout.addComponent(selectDataSet);
        datasetLayout.setExpandRatio(selectDataSet, 2.0f);
        
        selectGraph.addListener(new Property.ValueChangeListener() {
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
        });
        selectDataSet.addListener(new Property.ValueChangeListener() {
            public void valueChange(Property.ValueChangeEvent event) {
                DataSet ds = (DataSet) event.getProperty().getValue();
                final String function = "javaSetGraphAndDataSet('" +
                        ds.getGraph() + "','" + ds.getUri() + "')";
                getWindow().executeJavaScript(function);
                refreshDimensions();
            }
        });
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
        final Label levelLabel = new Label(LEVEL_LABEL_CONTENT, Label.CONTENT_XHTML);
        geoLayout.addComponent(levelLabel);
        // create a layout for the map
        mapLayout = new VerticalLayout();
        mapLayout.setSizeUndefined();
        mapLayout.setWidth("100%");
        mapLayout.setHeight("700px");
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
        chartLayout.setHeight("500px");
        chartLayout.setDebugId("highchartsbarsingle");
//        rightLayout.addComponent(chartLayout);
        VerticalLayout chartLayout2 = new VerticalLayout();
        chartLayout2.setSizeFull();
        chartLayout2.setWidth("100%");
        chartLayout2.setHeight("500px");
        chartLayout2.setDebugId("highchartsbarmultiple");
        rightLayout.addComponent(chartLayout2);
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
            Collection<String> vals = ds.getValuesForDimension(dim);
            builderPossibleValues.append(",").append(stringifyCollection(vals));
            ComboBox boxValue = new ComboBox(null, vals);
            boxValue.setImmediate(true);
            boxValue.setNullSelectionAllowed(false);
            boxValue.select(vals.iterator().next());
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
            Collection<String> posVals = ds.getValuesForDimension(geoDimension);
            String selectedVal = posVals.iterator().next();
            builder.append("javaSetGeoAll('").append(geoDimension.getUri());
            builder.append("',").append(stringifyCollection(posVals));
            builder.append(",'").append(selectedVal).append("')");
            boxGeo = new ComboBox(null, posVals);
            boxGeo.setDebugId("geoValue");
            boxGeo.setData(posVals);
            boxGeo.setImmediate(true);
            boxGeo.setNullSelectionAllowed(false);
            boxGeo.select(selectedVal);
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
        refresh();
        super.attach();
        Iterator<DataCubeGraph> iter = dcRepo.getDataCubeGraphs().iterator();
        while (iter.hasNext()){
            DataCubeGraph g = iter.next();
            if (g.getUri().equalsIgnoreCase("http://stat.apr.gov.rs/lod2/id/Register/RegionalDevelopmentMeasuresandIncentives")){
                selectGraph.select(g);
                break;
            }
        }
    }
    
    public void refreshJS(){
        dimListener.valueChange(null);
    }

    private String stringifyCollection(Collection<String> vals) {
        StringBuilder builder = new StringBuilder();
        for (String s: vals)
            builder.append(",'").append(s).append("'");
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
