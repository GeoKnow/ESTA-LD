/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package rs.pupin.jpo.dsdrepo;

/**
 *
 * @author vukm
 */
public class CodeDatatypeTreeElement {
    
    private String value;
    private boolean code;
    private int error;
    
    public CodeDatatypeTreeElement(String value, boolean code, int error){
        this.value = value;
        this.code = code;
        this.error = error;
    }

    public String getValue() {
        return value;
    }

    public boolean isCode() {
        return code;
    }

    public int getError() {
        return error;
    }

    @Override
    public String toString() {
        if (error > 0) return "Error " + error;
        return value;
    }
    
}
