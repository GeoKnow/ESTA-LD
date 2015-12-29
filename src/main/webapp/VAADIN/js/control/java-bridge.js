/* global geoForMapAllTimesData, cbfuncTwoFreeVuk, cbfuncOneFreeVuk, geoData, currentChart, chartBarMultiple */

$(document).ready(function() {
    $('body').append('<div id="esta-modal"><i class="fa fa-spinner fa-pulse"></i></div>');
});

var javaSelectedDimensions = [];
var javaDimensionValues = [];
var javaPossibleValues = [[]];
var javaFreeDimensions = [0];
var javaAggregDimensions = [];
var javaGraph = '';
var javaDataSet = '';
var javaMeasures = [];
var javaMeasureNames = [];
var javaSelectedMeasure = null;

var javaGeoDimension = '';
var javaGeoPossibleValues = [];
var javaGeoValue = '';
var javaGeoFree = false;
var javaGeoAggregated = false;

var javaHasTimeDimension = false;

var vaadinRedrawsMap = false;

var javaAggregatedColoring = false;

function toggleCompare() {
    // get position of #btn-compare and calculate position of the popup
    var btnCompareElem = $('#btn-compare').parent();
    var btnComparePos = btnCompareElem.offset();
    var posTop = btnComparePos.top + btnCompareElem.height() + 5;
    var posRight = $(document).width() - btnComparePos.left - btnCompareElem.width();
    // open popup just under #btn-compare
    $("#popup-mask").css("display", "block");
    var popupElem = $("#popup-measures");
    popupElem.css({
        top: posTop,
        right: posRight
    });
    popupElem.slideDown();
//    popupElem.animate({
//        transform: "none"
//    }, 1000);
    // TODO: CSS animation, aka transform: "translate(50%, -50%) scale(0,0)" then "transform: none;"
}

function toggleSwap(){
    if (currentChart === chartBarMultiple){
        // iterate through all series
        var newCategories = [];
        var newSeries = [];
        var newSeriesNames = chartBarMultiple.xAxis[0].categories;
        for (var i=0; i<newSeriesNames.length; i++) {
            newSeries.push([]);
        }
        $.each(chartBarMultiple.series, function (index, series){
            series.setVisible(true, false);
        });
        chartBarMultiple.redraw(false);
        while (chartBarMultiple.series.length > 0) {
            var curSeries = chartBarMultiple.series[chartBarMultiple.series.length - 1];
            newCategories.push(curSeries.name);
            // push each value into appropriate series
            $(curSeries.data).each(function(index, value){
                newSeries[index].push(value.y);
            });
            // remove the series but do not redraw afterwards (false)
            curSeries.remove(false);
        }
        var moreThanFiveSeries = (newSeries.length > 5);
        
        chartBarMultiple.setTitle({text: ''}, {text: ''});
        chartBarMultiple.xAxis[0].setCategories(newCategories, false);
        for (var i=newSeries.length-1; i >= 0; i--){
            var s = chartBarMultiple.addSeries({
                name: newSeriesNames[i],
                legendIndex: i+1,
                data: newSeries[i],
                visible: true
//                visible: (moreThanFiveSeries) ? (i === 0) : true
            }, false);
        }
//        chartBarMultiple.xAxis[0].update({}, false);
//        chartBarMultiple.yAxis[0].update({}, false);
//        $(chartBarMultiple.series).each(function (k,v) {
//            v.update({}, false);
//        });
        chartBarMultiple.redraw(false);
        $(chartBarMultiple.series).each(function(index, series){
            if (moreThanFiveSeries && index < newSeries.length - 1) 
                series.setVisible(false,false);
        });
        chartBarMultiple.redraw();
    }
}

function toggleStacking() {
    if (currentChart === chartBarMultiple) {
        if (chartBarMultiple.options.plotOptions.column.stacking)
            delete chartBarMultiple.options.plotOptions.column.stacking;
        else {
            var plotOptionsObject = {
                column: {
                    stacking: 'normal'
                }
            };
            $.extend(true, chartBarMultiple.options.plotOptions, plotOptionsObject);
        }

//        $(chartBarMultiple.xAxis).each(function(k,v) {
//            v.update({}, false);
//        });
//        $(chartBarMultiple.yAxis).each(function(k,v) {
//            v.update({}, false);
//        });
        chartBarMultiple.xAxis[0].update({}, false);
        chartBarMultiple.yAxis[0].update({}, false);
        $(chartBarMultiple.series).each(function (k,v) {
            v.update({}, false);
        });

        chartBarMultiple.redraw();
    }
}

function toggleInvert() {
    console.log('Current chart: ');
    console.log(currentChart);
    if (currentChart !== undefined && currentChart !== null) {
        if (currentChart.inverted)
            currentChart.inverted = false;
        else
            currentChart.inverted = true;

//        $(currentChart.xAxis).each(function(k,v) {
//            v.update({}, false);
//        });
//        $(currentChart.yAxis).each(function(k,v) {
//            v.update({}, false);
//        });
        currentChart.xAxis[0].update({}, false);
        currentChart.yAxis[0].update({}, false);
//        if (currentChart.xAxis.length > 2) currentChart.xAxis[2].update({}, false);
//        if (currentChart.yAxis.length > 2) currentChart.yAxis[2].update({}, false);
// this doesn't work the axes get inverted, but series take the whole space
        $(currentChart.series).each(function (k,v) {
            v.update({}, false);
        });

        currentChart.redraw();
    }
}

function expandDimNameButtons(){
    // TODO better remove this function completely and any calls to it :)
//    $('.v-button.v-button-dim-name.dim-name').each(function(index, elem){
//        $(this).parent().css("width", "100%");
//        $(this).css("width", "100%");
//    });
}

function javaSetSelectedDimensions(dims){
    javaSelectedDimensions = dims;
}

function javaSetDimensionValues(vals){
    javaDimensionValues = vals;
}

function javaSetFreeDimensions(dims, doNotUpdateCharts){
    var wasTimeGraph = javaHasTimeDimension && javaFreeDimensions.indexOf(0)>=0 && javaFreeDimensions.length===1;
    javaFreeDimensions = dims;
    if (doNotUpdateCharts) return;
    
    populateMeasuresPopup();
    var isTimeGraph = javaHasTimeDimension && javaFreeDimensions.indexOf(0)>=0 && javaFreeDimensions.length===1;
    if (wasTimeGraph || isTimeGraph)
        runSparqlDimensionValueChangedVuk();
    else
        runSparqlFreeDimensionsChangedVuk();
}

function javaSetAggregDimensions(dims, isGeoAggregated) {
    javaAggregDimensions = dims;
    javaGeoAggregated = isGeoAggregated;
    populateMeasuresPopup();
    runSparqlDimensionValueChangedVuk();
}

function javaSetGeoFree(isFree){
    javaGeoFree = isFree;
}

function populateMeasuresPopup() {
    var listElem = $("#popup-measures .list-measures ul.choices");
    listElem.empty();
    for (var i = 0; i<javaMeasures.length; i++) {
        var measureUri = javaMeasures[i];
        if (measureUri == javaSelectedMeasure) continue;
        var measureName = measureUri;
        if (javaMeasureNames && javaMeasureNames[i]) measureName = javaMeasureNames[i];
        listElem.append('<li measure="' + measureUri + '">' + measureName + '</li>');
    }
    $("#popup-measures ul.none li").addClass("selected-measure");
}

function javaSetMeasures(measures, measureNames){
    javaMeasures = measures;
    javaMeasureNames = measureNames;
    if (!javaMeasures || javaMeasures.length < 1) return javaSelectedMeasure = 'sdmx-measure:obsValue';
//    else if (javaMeasures.length >= 3) return javaSelectedMeasure = javaMeasures[2];
    else javaSelectedMeasure = javaMeasures[0];
    populateMeasuresPopup();
}

function javaSelectMeasure(measure){
    javaSelectedMeasure = measure;
    populateMeasuresPopup();
}

function getMeasureUri(){
    if (!javaSelectedMeasure || javaSelectedMeasure === '') return 'sdmx-measure:obsValue';
    return javaSelectedMeasure;
}

function measurePrettyName(measure) {
    var type = typeof measure;
    if (type === "string") {
        var index = javaMeasures.indexOf(measure);
        if (index < 0) return measure;
        var name = javaMeasureNames[index];
        if (name && name !== "") return name;
        else return measure;
    } else if (type === "number") {
        return measure;
    } else return measure;
}

function javaSetDimsVals(dims,vals,execPopulateFirst){
    javaSelectedDimensions = dims;
    javaDimensionValues = vals;
//    runSparqlForGeoMapVuk();
    populateMeasuresPopup();
    if (execPopulateFirst)
        populateGeoLevelsLists(runSparqlDimensionValueChangedVuk);
    else
        runSparqlDimensionValueChangedVuk();
}

function javaSetGeoAll(geo,vals,selectedVal, doNotQuery){
    javaGeoDimension = geo;
    javaGeoPossibleValues = vals;
    javaGeoValue = selectedVal;
    if (doNotQuery) return;
    populateGeoLevelsLists();
}

function javaSetGeoValue(val){
    javaGeoValue = val;
    // call chart printing
    if (javaFreeDimensions.length > 2) {
        alert('Number of free dimensions is greater than 2, namely ' + javaFreeDimensions.length);
        return;
    }
    populateMeasuresPopup();
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
    populateMeasuresPopup();
    runSparqlDimensionValueChangedVuk();
}

function javaSetPossibleValues(vals){
    javaPossibleValues = vals;
}

function javaSetGraphAndDataSet(graph, ds, endpoint){
    javaAggregDimensions = [];
    javaFreeDimensions = [0];
    setVisibleGeoLevel(0);
    javaGraph = graph;
    javaDataSet = ds;
//    sessionStorage.setItem('endpoint','http://147.91.50.167/sparql');
    endpoint = endpoint.replace('localhost', document.location.hostname);
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

function toggleAggregatedColoring() {
    if (javaAggregatedColoring) {
        $(".v-button.btn-aggreg-coloring").removeClass("selected");
        javaUnselectAggregColoring();
    } else {
        $(".v-button.btn-aggreg-coloring").addClass("selected");
        javaSelectAggregColoring();
    }
}

function javaSelectAggregColoring(){
    console.log('Select Aggreg Coloring');
    javaAggregatedColoring = true;
    $('#esta-modal').show();
    // TODO: there has to be a better, cleaner way to do this
    window.setTimeout(function() {
        try { calcAggregMinMax(); } catch(error) { console.err(error); }
        if (geoForMapAllTimesData.cbFunction)
            geoForMapAllTimesData.cbFunction(lastNewData, true);
        $('#esta-modal').hide();
    }, 100);
}

function javaUnselectAggregColoring(){
    console.log('Unselect Aggreg Coloring');
    javaAggregatedColoring = false;
    if (geoForMapAllTimesData.cbFunction)
        geoForMapAllTimesData.cbFunction(lastNewData, true);
}

function clearAggregatedColoring() {
    javaAggregatedColoring = false;
    $(".v-button.btn-aggreg-coloring").removeClass("selected");
}

function calcAggregMinMax() {
    var selectedGeoLevelCodes = geoLevels[visibleGeoLevel];
    var curMin = undefined;
    var curMax = undefined;
    var beforeTime = -1;
    var previousTime = -1;
    var timeSpan = timeChartMax - timeChartMin;
    var hasReachedLastWindow = false;
    var lastGeo = undefined;
    $(geoForMapAllTimesData.dataAllTimes.results.bindings).each(function(index, item) {
        var curGeo = item.rsgeo.value;
        if (!curGeo || selectedGeoLevelCodes.indexOf(curGeo) < 0) return true;
        if (hasReachedLastWindow && curGeo === lastGeo) return true;
        lastGeo = curGeo;
        hasReachedLastWindow = (curDate <= timeChartLastTime-timeSpan);
        
        var curDate = item.parsedTime.millis;
        var curValue = parseFloat(item.observation.value);
        if (curValue === undefined || curValue === null) curValue = 0;
        $(geoForMapAllTimesData.dataAllTimes.results.bindings.slice(index+1)).each(function(indexNew, itemNew) {
            var newGeo = itemNew.rsgeo.value;
            if (newGeo !== curGeo) return false;
            var newDate = itemNew.parsedTime.millis;
            if (newDate > curDate+timeSpan) return false;
            if (!isNaN(itemNew.observation.value))
                curValue += parseFloat(itemNew.observation.value);
        });
        if (curMin === undefined && curMax === undefined) {
            curMin = curMax = curValue;
        } else {
            if (curValue < curMin) curMin = curValue;
            if (curValue > curMax) curMax = curValue;
        }
    });
    maxObservationValueAggregated = curMax;
    minObservationValueAggregated = curMin;
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