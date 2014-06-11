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
            + "<input id=\"geoLevelLabel\" type=\"text\" readonly=\"\" value=\"Level 1\" style=\"width: 140px; height: 25 px; text-align: center;\"></input>"
            + "<input id=\"geoplus\" type=\"button\" style=\" width:25px; height:25px;\" value=\"+\"></input>";
    private static final String GEO_PART_WIDTH = "600px";
    private static final String CONTENT_ELEM_HEIGHT = "25px";
    private Property.ValueChangeListener dimListener;
    
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
        
        dimListener = new Property.ValueChangeListener() {
            public void valueChange(Property.ValueChangeEvent event) {
                if (dimNames == null && dimNames.length == 0){
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
                String function = "javaSetAll(" + javaDims + "," + javaVals + "," + javaFree + ");";
                getWindow().executeJavaScript(function);
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
                    for (DataSet ds: dcRepo.getDataSets())
                        selectDataSet.addItem(ds);
                } else {
                    DataCubeGraph graph = (DataCubeGraph) prop;
                    for (DataSet ds: graph.getDataSets())
                        selectDataSet.addItem(ds);
                }
            }
        });
        selectDataSet.addListener(new Property.ValueChangeListener() {
            public void valueChange(Property.ValueChangeEvent event) {
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
        mapLayout.setHeight("500px");
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
        chartLayout.setDebugId("highchartsbarsingle");
        rightLayout.addComponent(chartLayout);
        VerticalLayout chartLayout2 = new VerticalLayout();
        chartLayout2.setSizeFull();
        chartLayout2.setWidth("100%");
        chartLayout2.setDebugId("highchartsbarmultiple");
        rightLayout.addComponent(chartLayout2);
    }
    
    private void refreshDimensions(){
        dimLayout.removeAllComponents();
        
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
        dimNames = new Button[dimsForShow.size()];
        dimValues = new ComboBox[dimsForShow.size()];
        int i=0;
        
        for (Dimension dim: dimsForShow){
            // add dimension pick
            // first create a button to represent dimension name
            Button btnName = new Button(dim.toString());
            btnName.setSizeUndefined();
            btnName.setWidth("100%");
            btnName.setHeight(CONTENT_ELEM_HEIGHT);
            btnName.setData(dim);
            btnName.addStyleName("dim-name");
            dimNames[i] = btnName;
            
            // create a combo box for picking dimension value
            ComboBox boxValue = new ComboBox(null, ds.getValuesForDimension(dim));
            boxValue.setImmediate(true);
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
    }
    
}
