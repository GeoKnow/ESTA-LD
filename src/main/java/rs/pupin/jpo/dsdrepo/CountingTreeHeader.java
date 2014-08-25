/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package rs.pupin.jpo.dsdrepo;

import com.vaadin.ui.Tree;
import java.util.Collection;

/**
 *
 * @author vukm
 */
public class CountingTreeHeader {
    
    private final Tree tree;
    private final Object header;
    
    public CountingTreeHeader(Tree tree, Object header) {
        this.tree = tree;
        this.header = header;
    }

    public Tree getTree() {
        return tree;
    }

    public Object getHeader() {
        return header;
    }
    
    public int getCount(){
        Collection<?> children = tree.getChildren(this);
        return (children == null)?0:children.size();
    }

    @Override
    public String toString() {
        return header.toString() + " (" + getCount() + ")";
    }
    
}
