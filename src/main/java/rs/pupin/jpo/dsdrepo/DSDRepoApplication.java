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
package rs.pupin.jpo.dsdrepo;

import com.vaadin.Application;
import com.vaadin.ui.Window;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sparql.SPARQLRepository;
import rs.pupin.jpo.esta_ld.DSDRepoComponent;
import rs.pupin.jpo.esta_ld.EstaLdComponent;

/**
 * The Application's "main" class
 */
@SuppressWarnings("serial")
public class DSDRepoApplication extends Application
{
    private Window window;

    @Override
    public void init()
    {
        window = new Window("DSD Repo");
        setMainWindow(window);
        setTheme("esta-ld");
        
        String endpoint = "http://localhost:8890/sparql";
        
        SPARQLRepository repository = new SPARQLRepository(endpoint);
        try {
            repository.initialize();
        } catch (RepositoryException ex) {
            Logger.getLogger(EstaLdComponent.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try {
            RepositoryConnection conn = repository.getConnection();
            conn.prepareGraphQuery(QueryLanguage.SPARQL, "DROP GRAPH <http://regular-data-replica/>").evaluate();
            conn.prepareGraphQuery(QueryLanguage.SPARQL, "CREATE GRAPH <http://regular-data-replica/>").evaluate();
            String query = "INSERT INTO GRAPH <http://regular-data-replica/> {?s ?p ?o } "
                    + "WHERE { GRAPH <http://validation-test/regular-data-nolabels/> { ?s ?p ?o } }";
            conn.prepareGraphQuery(QueryLanguage.SPARQL, query).evaluate();
        } catch (RepositoryException ex) {
            Logger.getLogger(DSDRepoComponent.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedQueryException ex) {
            Logger.getLogger(DSDRepoComponent.class.getName()).log(Level.SEVERE, null, ex);
        } catch (QueryEvaluationException ex) {
            Logger.getLogger(DSDRepoComponent.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        DSDRepoComponent component = new DSDRepoComponent(repository, 
                "http://regular-data-replica/", 
                "http://validation-test/regular-dsd-nolabels/");
        
        window.addComponent(component);
    }
    
}
