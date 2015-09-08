/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.pupin.jpo.esta_ld;

import com.vaadin.Application;
import com.vaadin.terminal.ParameterHandler;
import com.vaadin.ui.Window;
import java.util.Map;

/**
 *
 * @author vukm
 */
public class InspectApplication extends Application {
    private boolean first = true;
    private Window window;

    @Override
    public void init()
    {
        window = new Window("ESTA-LD: Inspect and Prepare");
        window.addStyleName("estald-window");
        window.addParameterHandler(new ParameterHandler() {
            public void handleParameters(Map<String, String[]> parameters) {
                if (!first) return;
                first = false;
                String[] titleParam = parameters.get("endpoint");
                String endpointURL = (titleParam == null)?null:titleParam[0];
                System.out.println(endpointURL);
                
                InspectWrapperComponent component = new InspectWrapperComponent(endpointURL);
        
                window.addComponent(component);
            }
        });
        
        window.addListener(new Window.CloseListener() {
            public void windowClose(Window.CloseEvent e) {
                window.getApplication().close();
            }
        });
        
        setMainWindow(window);
        setTheme("esta-ld");
        
       
    }
    
}
