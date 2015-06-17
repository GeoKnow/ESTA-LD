/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.pupin.jpo.esta_ld;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sparql.SPARQLRepository;

/**
 *
 * @author vukm
 */
public class EndpointWindow extends Window {
    
    private CheckBox authCheckBox;
    private CheckBox owCheckBox;
    
    public static class EndpointState {
        public Repository repository = null;
        public String endpoint;
    }
    
    private final EndpointState state;
    private final GridLayout settingsLayout;
    private TextField endpointInput;
    private Button btnOK;
    private Button btnCancel;
    
    public EndpointWindow(EndpointState state){
        this.state = state;
        setCaption("Select Endpoint");
        setModal(true);
        setClosable(false);
        setResizable(false);
        setDraggable(false);
        setWidth("700px");
        setHeight("200px");
        
        settingsLayout = new GridLayout(2,3);
        settingsLayout.setMargin(true);
        settingsLayout.setSizeFull();
        settingsLayout.setColumnExpandRatio(1, 2.0f);
        settingsLayout.setRowExpandRatio(9, 2.0f);
        settingsLayout.setSpacing(true);
        setContent(settingsLayout);
    }

    @Override
    public void detach() {
        super.detach();
    }

    @Override
    public void attach() {
        super.attach(); 
        
        // add endpoint field
        Label lbl = new Label("Endpoint:");
        lbl.setSizeUndefined();
        settingsLayout.addComponent(lbl, 0, 0, 0, 0);
        settingsLayout.setComponentAlignment(lbl, Alignment.MIDDLE_LEFT);
        endpointInput = new TextField();
        endpointInput.setValue(state.endpoint);
        endpointInput.setWidth("100%");
        settingsLayout.addComponent(endpointInput, 1, 0, 1, 0);
        settingsLayout.setComponentAlignment(endpointInput, Alignment.MIDDLE_RIGHT);
        
        // add blank row
        lbl = new Label("");
        lbl.setHeight("30px");
        settingsLayout.addComponent(lbl, 0, 1, 1, 1);
        
        // add buttons
        HorizontalLayout hl = new HorizontalLayout();
        hl.setSpacing(true);
        hl.setSizeUndefined();
        btnOK = new Button("OK");
        btnCancel = new Button("Cancel");
        hl.addComponent(btnOK);
        hl.addComponent(btnCancel);
        settingsLayout.addComponent(hl, 1, 2);
        settingsLayout.setComponentAlignment(hl, Alignment.MIDDLE_RIGHT);
        
        createListeners();
        center();
    }
    
    private void createListeners(){
        btnCancel.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                close();
            }
        });
        btnOK.addListener(new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                if (state.endpoint.equals(endpointInput.getValue())) {
                    close();
                    return;
                }
                
                SPARQLRepository r = new SPARQLRepository(endpointInput.getValue().toString());
                try {
                    r.initialize();
                    System.out.println(r.isWritable());
                    String qString = "PREFIX qb: <http://purl.org/linked-data/cube#> \n"
                            + "ASK {graph ?g { ?ds qb:structure ?dsd . } }";
                    boolean graphExists = r.getConnection().prepareBooleanQuery(QueryLanguage.SPARQL, qString).evaluate();
                    if (!graphExists) {
                        getWindow().showNotification("Selected endpoint doesn't contain data cubes", Notification.TYPE_ERROR_MESSAGE);
                        return;
                    }
                    state.repository = r;
                    state.endpoint = endpointInput.getValue().toString();
                    // TODO denote if input changed or not
                } catch (RepositoryException ex) {
                    Logger.getLogger(EndpointWindow.class.getName()).log(Level.SEVERE, null, ex);
                    getWindow().showNotification("Error connecting to the endpoint: " + ex.getMessage() + "\nCaused by: " + ex.getCause().getMessage());
                    return;
                } catch (MalformedQueryException ex) {
                    Logger.getLogger(EndpointWindow.class.getName()).log(Level.SEVERE, null, ex);
                    getWindow().showNotification("Error connecting to the endpoint: " + ex.getMessage() + "\nCaused by: " + ex.getCause().getMessage());
                    return;
                } catch (QueryEvaluationException ex) {
                    Logger.getLogger(EndpointWindow.class.getName()).log(Level.SEVERE, null, ex);
                    // TODO notify user
                    getWindow().showNotification("Error connecting to the endpoint: " + ex.getMessage() + "\nCaused by: " + ex.getCause().getMessage());
                    return;
                }
                close();
            }
        });
    }
    
}
