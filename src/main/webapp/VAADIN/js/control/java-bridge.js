/* global geoForMapAllTimesData, cbfuncTwoFreeVuk, cbfuncOneFreeVuk, geoData */

var javaSelectedDimensions = [];
var javaDimensionValues = [];
var javaPossibleValues = [[]];
var javaFreeDimensions = [0];
var javaGraph = '';
var javaDataSet = '';

var javaGeoDimension = '';
var javaGeoPossibleValues = [];
var javaGeoValue = '';
var javaGeoFree = false;

var javaHasTimeDimension = false;

var vaadinRedrawsMap = false;

var javaAggregatedColoring = false;

function javaSetSelectedDimensions(dims){
    javaSelectedDimensions = dims;
}

function javaSetDimensionValues(vals){
    javaDimensionValues = vals;
}

function javaSetFreeDimensions(dims){
    javaFreeDimensions = dims;
    runSparqlFreeDimensionsChangedVuk();
}

function javaSetGeoFree(isFree){
    javaGeoFree = isFree;
}

function javaSetDimsVals(dims,vals){
    javaSelectedDimensions = dims;
    javaDimensionValues = vals;
//    runSparqlForGeoMapVuk();
    runSparqlDimensionValueChangedVuk();
}

function javaSetGeoAll(geo,vals,selectedVal){
    javaGeoDimension = geo;
    javaGeoPossibleValues = vals;
    javaGeoValue = selectedVal;
    populateGeoLevelsLists();
}

function javaSetGeoValue(val){
    javaGeoValue = val;
    // call chart printing
    if (javaFreeDimensions.length > 2) {
        alert('Number of free dimensions is greater than 2, namely ' + javaFreeDimensions.length);
        return;
    }
    execSparqlDimensionValueChangedVuk(cbfuncOneFreeVuk,cbfuncTwoFreeVuk);
}

function javaSetHasTimeDimension(hasTimeDim){
    javaHasTimeDimension = hasTimeDim;
}

function javaSetAll(dims,vals,free){
    javaSelectedDimensions = dims;
    javaDimensionValues = vals;
    javaFreeDimensions = free;
//    runSparqlForGeoMapVuk();
    runSparqlDimensionValueChangedVuk();
}

function javaSetPossibleValues(vals){
    javaPossibleValues = vals;
}

function javaSetGraphAndDataSet(graph, ds, endpoint){
    setVisibleGeoLevel(0);
    javaGraph = graph;
    javaDataSet = ds;
//    sessionStorage.setItem('endpoint','http://147.91.50.167/sparql');
    sessionStorage.setItem('endpoint', endpoint);
    window.endpoint = endpoint;
//    sessionStorage.setItem('endpoint','http://localhost:8890/sparql');
//    sessionStorage.setItem('endpoint','http://jpo.imp.bg.ac.rs/sparql');
    sessionStorage.setItem('graph', javaGraph);
}

function javaPrintAll(){
    alert('Graph: ' + javaGraph +
            '\nDataSet: ' + javaDataSet +
            '\nSelected dims: ' + javaSelectedDimensions.toString() +
            '\nSelected values: ' + javaDimensionValues.toString() +
            '\nFree dims: ' + javaFreeDimensions.toString());
}

function findIndexForDimension(dimNumber, uri){
    for (var i=0; i<javaPossibleValues[dimNumber].length; i++){
        if (uri == cleanValue(javaPossibleValues[dimNumber][i]))
            return i;
    }
    console.log(javaPossibleValues[dimNumber]);
    alert('Couldnt find index of ' + uri + ' in ' + javaSelectedDimensions[dimNumber]);
}

function findIndexForGeoDimension(uri){
    for (var i=0; i<javaGeoPossibleValues.length; i++){
        if (uri == cleanValue(javaGeoPossibleValues[i]))
            return i;
    }
    console.log(javaGeoPossibleValues);
    alert('Couldnt find index of geo ' + uri + ' in ' + javaGeoPossibleValues);
}

function uriLastPart(uri){
    return uri.substring(uri.lastIndexOf('/')+1, uri.length);
}

function javaSelectAggregColoring(){
    console.log('Select Aggreg Coloring');
    javaAggregatedColoring = true;
    if (geoForMapAllTimesData.cbFunction && geoForMapAllTimesData.newData)
        geoForMapAllTimesData.cbFunction(geoForMapAllTimesData.newData, true);
}

function javaUnselectAggregColoring(){
    console.log('Unselect Aggreg Coloring');
    javaAggregatedColoring = false;
    if (geoForMapAllTimesData.cbFunction && geoForMapAllTimesData.newData)
        geoForMapAllTimesData.cbFunction(geoForMapAllTimesData.newData, true);
}

function setVisibleGeoLevel(lvl) {
    visibleGeoLevel = lvl;
    $('#geoLevelLabel').val('Level ' + visibleGeoLevel);
}

function javaInsertPolygons(pairs){
    // add property ogc:hasGeometry and ogc:hasDefaultGeometry to the codes
    // these geometry instances will then have ogc:asWKT properties
    var intro = "PREFIX ogc: <http://www.opengis.net/ont/geosparql#>\n\
INSERT INTO GRAPH <" + javaGraph + "> {";
    var outro = "}";
    var triples = "";
//    console.log('Number of pairs is: ' + pairs.length);
//    console.log(pairs);
    if (pairs.length === 0) return;
    // TODO: this branch could be removed as it serves only our code list for Serbian regions
    if (pairs[0].uri.substr(0,40) ==='http://elpo.stat.gov.rs/lod2/RS-DIC/geo/')
        $(pairs).each(function(index, pair){
//            console.log('Entered into RS branch, exiting...');
//            return;
            var iteration = 0;
            $(geoData).each(function(index, geoLevel){
                $(geoLevel.features).each(function(index, feature){
                    if (feature.properties.NSTJ_CODE === pair.code) {
                        // create wkt literal
                        iteration++;
                        console.log(feature.geometry);
                        var wktLiteral = "\"" + stringify(feature.geometry) + "\"^^ogc:wktLiteral";
                        // define URIs for code and the new geometry
                        var geometryURI = "<" + pair.uri + "/defaultGeometry>";
                        var codeURI = "<" + pair.uri + ">";

                        triples = codeURI + " ogc:hasDefaultGeometry " + geometryURI + " . \n" +
                                codeURI + " ogc:hasGeometry " + geometryURI + " . \n" +
                                geometryURI + " ogc:asWKT " + wktLiteral + " . \n";
    //                    console.log(intro + triples + outro);
                        // or do triples += and then execute full query
                        var queryString = intro + triples + outro;
                        console.log(queryString);
                        $.ajax({
                            async: false,
                            url: endpoint,
                            method: 'POST',
                            type: 'POST', 
                            data: { 
                                query: queryString
                            }, 
                            success: function(data){
                                console.log('Query executed correctly for iteration: ' + iteration);
                                console.log(data);
                            },
                            error: function(data) { 
                                console.error('Query error for iteration ' + iteration + '!!!');
                                console.log(data); 
                            }
                        });
                    }
                });
            });
        });
    else {
        // query LGD and insert polygons into the store
        var lgdIntro = 'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n' +
                'PREFIX ogc: <http://www.opengis.net/ont/geosparql#> \n' +
                'PREFIX geom: <http://geovocab.org/geometry#> \n' +
                ' \n' +
                'SELECT * { \n' +
                '  GRAPH <http://linkedgeodata.org/ne/> { \n' +
                '    ?s a <http://linkedgeodata.org/ne/ontology/Country> ; \n' +
                '    rdfs:label ?l ; \n' +
                '    geom:geometry [ \n' +
                '      ogc:asWKT ?g \n' +
                '    ] .  \n' +
                '  } \n' +
                '  FILTER (?l = "';
        var lgdOutro = '") \n }';
        $('body').css('cursor', 'wait');
        $(pairs).each(function(index, pair){
            $.ajax({
                async: false, 
                url: 'http://linkedgeodata.org/vsparql/', 
                type: 'POST', 
                method: 'POST', 
                data: {
//                    format: 'json', 
                    query: lgdIntro + pair.code + lgdOutro
                }, 
                success: function(data) {
                    var polygons = $(data).find('literal[datatype="http://www.opengis.net/ont/geosparql#wktLiteral"]');
                    var wktString = null;
                    if (polygons && polygons.length > 0) wktString = polygons[0].innerHTML;
                    if (!wktString || wktString === '') {
                        console.log('No polygon for: ' + pair.uri);
                    } else {
//                        console.log('Polygon for ' + pair.uri + ': ' + wktLiteral.length);
                        // now upload the polygon to the endpoint
                        var wktLiteral = "\"" + wktString + "\"^^ogc:wktLiteral";
                        // define URIs for code and the new geometry
                        var geometryURI = "<" + pair.uri + "/defaultGeometry>";
                        var codeURI = "<" + pair.uri + ">";

                        triples = codeURI + " ogc:hasDefaultGeometry " + geometryURI + " . \n" +
                                codeURI + " ogc:hasGeometry " + geometryURI + " . \n" +
                                geometryURI + " ogc:asWKT " + wktLiteral + " . \n";
                        $.ajax({
                            async: false, 
                            url: endpoint, 
                            type: 'POST', 
                            method: 'POST', 
                            data: {
                                query: intro + triples + outro
                            }, 
                            success: function (data, textStatus, jqXHR) {
                                console.log('Successfuly uploaded polygon ' + pair.uri);
                            }, 
                            error: function (jqXHR, textStatus, errorThrown) {
                                console.error('Error uploading polygon ' + pair.uri);
                                console.error(errorThrown);
                            }
                        });
                    }
                }, 
                error: function(data) {
                    console.error('Error fetching ' + pair.uri);
                    console.error(data);
                }
            });
        });
        $('body').css('cursor', 'default');
        
    }
    // execute query
//    var queryUrlEncoded = endpoint + '?query=' + $.URLEncode(intro + triples + outro)+'&format=json';
//    $.ajax({
//        async: false,
//        url: endpoint,
//        method: 'POST',
//        data: { 
//            query: intro+triples+outro
//        }, 
//        success: function(data){
//            console.log('Query executed correctly');
//            console.log(data);
//        },
//        error: function(data) { 
//            alert("There was an error during communication with the sparql endpoint"); 
//            console.log('Query error!!!');
//            console.log(data); 
//        }
//    });
}