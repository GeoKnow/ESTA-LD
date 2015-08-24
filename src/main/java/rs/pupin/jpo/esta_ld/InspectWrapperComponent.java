/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.pupin.jpo.esta_ld;

import com.vaadin.data.Property;
import static com.vaadin.terminal.Sizeable.UNITS_PERCENTAGE;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sparql.SPARQLRepository;
import rs.pupin.jpo.datacube.DataCubeGraph;
import rs.pupin.jpo.datacube.DataSet;
import rs.pupin.jpo.datacube.sparql_impl.SparqlDCRepository;

/**
 *
 * @author vukm
 */
public class InspectWrapperComponent extends CustomComponent {
    
    private String endpoint;
    private Repository repository;
    private SparqlDCRepository dcRepo;
    private VerticalLayout mainLayout;
    private HorizontalLayout brandLayout;
    private VerticalLayout settingsLayout;
    private HorizontalLayout datasetLayout;
    private VerticalLayout contentLayout;
    private ComboBox selectGraph;
    private ComboBox selectDataSet;
    private Property.ValueChangeListener graphListener;
    private Property.ValueChangeListener datasetListener;

    public InspectWrapperComponent(String endpoint) {
        this.endpoint = endpoint;
        
        initializeRepository();
        createGUI();
        
        setCompositionRoot(mainLayout);
    }
    
    private void initializeRepository(){
        if (endpoint == null) {
//            repository = null;
//            dcRepo = new DummyDCRepository();
//            geoDimension = null;
//            return;
            endpoint = "http://jpo.imp.bg.ac.rs/sparql";
        }
        repository = new SPARQLRepository(endpoint);
        try {
            repository.initialize();
        } catch (RepositoryException ex) {
            Logger.getLogger(EstaLdComponent.class.getName()).log(Level.SEVERE, null, ex);
        }
        dcRepo = new SparqlDCRepository(repository);
    }
    
    private void createGUI(){
        mainLayout = new VerticalLayout();
        mainLayout.setSpacing(false);
        mainLayout.setDebugId("l-main");
        
        brandLayout = new HorizontalLayout();
        brandLayout.setSpacing(true);
        brandLayout.setMargin(true);
        brandLayout.setWidth("100%");
        brandLayout.setDebugId("l-brand");
        Label brandSpan = new Label("<span id='brand'>ESTA-LD: <i>Inspect and Prepare</i></span>", Label.CONTENT_XHTML);
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
//        mainLayout.setExpandRatio(brandLayout, 0.0f);
        settingsLayout.addComponent(datasetLayout);
        mainLayout.addComponent(settingsLayout);
//        mainLayout.setExpandRatio(settingsLayout, 0.0f);
        
        // in place of this divide borders and shadows will be added
//        Label lblDivider = new Label("<hr/>", Label.CONTENT_XHTML);
//        mainLayout.addComponent(lblDivider);
//        mainLayout.setExpandRatio(lblDivider, 0.0f);
        
//        contentLayout = new HorizontalLayout();
        contentLayout = new VerticalLayout();
        contentLayout.setMargin(true);
//        contentLayout.setSizeFull();
        contentLayout.setWidth("100%");
//        contentLayout.setSpacing(true);
        contentLayout.addStyleName("l-content-inspect");
        mainLayout.addComponent(contentLayout);
//        mainLayout.setExpandRatio(contentLayout, 2.0f);
        
        createDataSetLayout();
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
            if (endpoint.equals("http://jpo.imp.bg.ac.rs/sparql") && graph.getUri().equals("http://demo/reg-dev-polygons/"))
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
                    contentLayout.removeAllComponents();
                    InspectComponent c = new InspectComponent(
                            repository,
                            ds.getGraph(), 
                            ds.getUri()
                    );
                    c.addStyleName("inspect-component");
                    contentLayout.addComponent(c);
                }
            }
        };
        selectDataSet.addListener(datasetListener);
    }
    
    private void refresh(){
//        contentLayout.removeAllComponents();
        
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
    
}
