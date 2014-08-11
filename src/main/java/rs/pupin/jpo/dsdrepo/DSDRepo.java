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
public interface DSDRepo {
    public boolean containsDSD(String uri);
    public boolean isIdenticalDSD(String sGraph, String uri);
}
