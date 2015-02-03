/*
 * Copyright 2009 IT Mill Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package rs.pupin.jpo.esta_ld;

import com.vaadin.Application;
import com.vaadin.terminal.ParameterHandler;
import com.vaadin.ui.Window;
import java.util.Map;

/**
 * The Application's "main" class
 */
@SuppressWarnings("serial")
public class MyVaadinApplication extends Application
{
    private Window window;

    @Override
    public void init()
    {
        window = new Window("ESTA-LD");
        final StringBuilder sb = new StringBuilder();
        window.addParameterHandler(new ParameterHandler() {
            public void handleParameters(Map<String, String[]> parameters) {
                if (sb.length() > 0) return;
                String[] titleParam = parameters.get("title");
                sb.append((titleParam == null)?null:titleParam[0]);
                window.showNotification(sb.toString()); // TODO remove when finished with 
                System.out.println(sb.toString());
                
                EstaLdComponent component = new EstaLdComponent();
        
                window.addComponent(component);
                window.executeJavaScript("estamainInitVuk()");
                window.executeJavaScript("sparqlqueryInitVuk()");
                window.executeJavaScript("rammapInitVuk()");
                window.executeJavaScript("chartsInitVuk()");
                window.executeJavaScript("timechartInitVuk()");
                component.refreshJS();
            }
        });
        setMainWindow(window);
        setTheme("esta-ld");
        
       
    }
    
}
