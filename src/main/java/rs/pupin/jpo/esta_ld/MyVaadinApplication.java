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
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sparql.SPARQLRepository;

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
        setMainWindow(window);
        
        String query = "SELECT DISTINCT isiri(?val) as ?i datatype(?val) as ?t \n"
                + "FROM <@gSource> \n"
                + "WHERE { \n"
                + "  ?obs a qb:Observation . \n"
                + "  ?obs <@prop> ?val . \n"
                + "}";
        query = query.replace("@prop", "http://purl.org/linked-data/sdmx/2009/measure#obsValue").replace("@gSource", "http://elpo.stat.gov.rs/test/cvmod/noDataSet/");
        final StringBuilder output = new StringBuilder();
        SPARQLRepository repo = new SPARQLRepository("http://jpo.imp.bg.ac.rs/sparql");
        try {
            repo.initialize();
            RepositoryConnection con = repo.getConnection();
            TupleQuery q = con.prepareTupleQuery(QueryLanguage.SPARQL, query);
            TupleQueryResult result = q.evaluate();
            while (result.hasNext()){
                BindingSet set = result.next();
                Value iVal = set.getValue("i");
                Value tVal = set.getValue("t");
                output.append("isIRI: ");
                if (iVal != null)
                    output.append(iVal.stringValue());
                output.append("\nType: ");
                if (tVal != null)
                    output.append(tVal.stringValue());
                output.append("\n");
            }
        } catch (RepositoryException ex) {
            Logger.getLogger(MyVaadinApplication.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedQueryException ex) {
            Logger.getLogger(MyVaadinApplication.class.getName()).log(Level.SEVERE, null, ex);
        } catch (QueryEvaluationException ex) {
            Logger.getLogger(MyVaadinApplication.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        Button btn = new Button("Click me nigga");
        btn.addListener(new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                getMainWindow().showNotification(output.toString());
            }
        });
        
        EstaLdComponent component = new EstaLdComponent();
        
        window.addComponent(component);
        window.executeJavaScript("estamainInitVuk()");
        window.executeJavaScript("sparqlqueryInitVuk()");
        window.executeJavaScript("rammapInitVuk()");
        window.executeJavaScript("chartsInitVuk()");
        window.executeJavaScript("timechartInitVuk()");
        component.refreshJS();
    }
    
}
