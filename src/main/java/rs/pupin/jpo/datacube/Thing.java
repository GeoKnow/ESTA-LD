/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package rs.pupin.jpo.datacube;

/**
 *
 * @author vukm
 */
public interface Thing {
    
    public String getUri();
    public String getGraph();
    public String getLabel();
    public boolean hasLabel();
    
}
