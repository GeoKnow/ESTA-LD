/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package rs.pupin.jpo.esta_ld;

import com.vaadin.Application;
import com.vaadin.terminal.gwt.server.ApplicationServlet;
import com.vaadin.ui.Window;
import java.io.BufferedWriter;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author vukm
 */
public class MyJavascriptLoadingServlet extends ApplicationServlet {
    
    private void writeStuff(BufferedWriter page) throws IOException{
        page.write("<link rel=\"stylesheet\" href=\"/ESTA-LD/VAADIN/resources/css/leaflet.css\" />\n");
        page.write("<link rel=\"stylesheet\" href=\"/ESTA-LD/VAADIN/resources/css/geoknow.css\" />\n");
        page.write("<script src=\"/ESTA-LD/VAADIN/resources/libs/jquery/1.9.1/jquery.js\"></script>\n");
//        page.write("<script src=\"/ESTA-LD/VAADIN/resources/libs/highcharts/4.1.8/highcharts.js\"></script>\n");
        page.write("<script src=\"/ESTA-LD/VAADIN/resources/libs/highstock/2.1.8/highstock.js\"></script>\n");
        page.write("<script src=\"/ESTA-LD/VAADIN/resources/libs/highstock/2.0.1/exporting.js\"></script>\n");
        page.write("<script src=\"/ESTA-LD/VAADIN/resources/libs/leaflet/0.7.2/leaflet.js\"></script>\n");
        page.write("<script src=\"/ESTA-LD/VAADIN/resources/libs/urlEncode.js\"></script>\n");
        page.write("<script src=\"/ESTA-LD/VAADIN/resources/libs/wellknown.js\"></script>\n");
        page.write("<script src=\"/ESTA-LD/VAADIN/resources/libs/resize/ElementQueries.js\"></script>\n");
        page.write("<script src=\"/ESTA-LD/VAADIN/resources/libs/resize/ResizeSensor.js\"></script>\n");
//        page.write("<script src=\"/ESTA-LD/VAADIN/js/geojson/geojson-data.js\" ></script>\n");
//        page.write("<script src=\"/ESTA-LD/VAADIN/js/geojson/geojson-area.js\" ></script>\n");
//        page.write("<script src=\"/ESTA-LD/VAADIN/js/geojson/geojson-region.js\" ></script>\n");
//        page.write("<script src=\"/ESTA-LD/VAADIN/js/geojson/geojson-municipality.js\" ></script>\n");
        page.write("<script src=\"/ESTA-LD/VAADIN/js/geojson/geojson-country.js\" ></script>\n");
    }
    
    private void writeOtherStuff(BufferedWriter page) throws IOException {
        page.write("<script src=\"/ESTA-LD/VAADIN/js/control/java-bridge.js\" ></script>\n");
        page.write("<script src=\"/ESTA-LD/VAADIN/js/control/selectionmain.js\" ></script>\n");
        page.write("<script src=\"/ESTA-LD/VAADIN/js/control/estamain-vuk.js\" ></script>\n");
        page.write("<script src=\"/ESTA-LD/VAADIN/js/sparql/sparqlquery-vuk.js\" ></script>\n");
        page.write("<script src=\"/ESTA-LD/VAADIN/js/map/rammap-vuk.js\" ></script>\n");
        page.write("<script src=\"/ESTA-LD/VAADIN/js/highcharts/charts-vuk.js\" ></script>\n");
        page.write("<script src=\"/ESTA-LD/VAADIN/js/highcharts/timechart-vuk.js\" ></script>\n");
    }

    @Override
    protected void writeAjaxPageHtmlHeader(BufferedWriter page, String title, String themeUri, HttpServletRequest request) throws IOException {
        writeStuff(page);
        writeOtherStuff(page);
        super.writeAjaxPageHtmlHeader(page, title, themeUri, request); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void writeAjaxPageHtmlVaadinScripts(Window window, String themeName, Application application, BufferedWriter page, String appUrl, String themeUri, String appId, HttpServletRequest request) throws ServletException, IOException {
        super.writeAjaxPageHtmlVaadinScripts(window, themeName, application, page, appUrl, themeUri, appId, request); //To change body of generated methods, choose Tools | Templates.
    }
    
}
