/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package rs.pupin.jpo.esta_ld;

import com.vaadin.data.Property;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import java.util.Collection;
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
//        BeanContainer<DataCubeGraph,DataCubeGraph> graphContainer = new BeanContainer<DataCubeGraph, DataCubeGraph>(DataCubeGraph.class);
//        graphContainer.setBeanIdProperty("uri");
//        graphContainer.addAll(dcRepo.getDataCubeGraphs());
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
        
        geoLayout = new VerticalLayout();
        geoLayout.setSizeUndefined();
        geoLayout.setWidth("500px");
        geoLayout.setSpacing(true);
        contentLayout.addComponent(geoLayout);
        contentLayout.setExpandRatio(geoLayout, 0.0f);
        //TODO treba ukolpiti onaj slajder za levo desno
        mapLayout = new VerticalLayout();
        mapLayout.setSizeUndefined();
        mapLayout.setWidth("100%");
        mapLayout.setDebugId("mapContainer");
        geoLayout.addComponent(mapLayout);
        
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
        chartLayout.setDebugId("chartContainer");
        rightLayout.addComponent(chartLayout);
    }
    
    private void refreshDimensions(){
        dimLayout.removeAllComponents();
        
        if (selectDataSet.getValue() == null) return;
        
        final DataSet ds = (DataSet) selectDataSet.getValue();
        Collection<Dimension> dimensions = ds.getStructure().getDimensions();
        dimNames = new Button[dimensions.size()];
        dimValues = new ComboBox[dimensions.size()];
        int i=0;
        
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
        
        for (Dimension dim: ds.getStructure().getDimensions()){
            // add dimension pick
            // first create a button to represent dimension name
            Button btnName = new Button(dim.toString());
            btnName.setSizeUndefined();
            btnName.setWidth("100%");
            btnName.setData(dim);
            btnName.addStyleName("dim-name");
            dimNames[i] = btnName;
            
            // create a combo box for picking dimension value
            ComboBox boxValue = new ComboBox(null, ds.getValuesForDimension(dim));
            boxValue.setImmediate(true);
            boxValue.setSizeUndefined();
            boxValue.setWidth("100%");
            boxValue.addStyleName("dim-value");
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
        }
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
