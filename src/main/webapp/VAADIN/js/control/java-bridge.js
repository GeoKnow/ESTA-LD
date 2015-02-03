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

function javaSetGraphAndDataSet(graph,ds){
    javaGraph = graph;
    javaDataSet = ds;
    sessionStorage.setItem('endpoint','http://147.91.50.167/sparql');
//    sessionStorage.setItem('endpoint','http://localhost:8890/sparql');
//    sessionStorage.setItem('endpoint','http://jpo.imp.bg.ac.rs/sparql');
    sessionStorage.setItem('graph',javaGraph);
//    for (var i=1; i<5; i++) {
//        for (var j=1; j<5; j++) {
//            for (var k=1; k<5; k++){
//                var code = 'RS' + i.toString() + j.toString() + k.toString();
//                var val = i*j*k;
//                hashCodeToObservationValues[code] = val.toString();
//            }
//            var code = 'RS' + i.toString() + j.toString();
//            var val = i*j;
//            hashCodeToObservationValues[code] = val.toString();
//        }
//        var code = 'RS' + i.toString();
//        var val = i;
//        hashCodeToObservationValues[code] = val.toString();
//    }
//    refreshMap();
//    sessionStorage.setItem('analysistype','barspace');
    populateGeoLevelsLists();
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
        if (uri == javaPossibleValues[dimNumber][i])
            return i;
    }
    alert('Couldnt find index of ' + uri + ' in ' + javaSelectedDimensions[dimNumber]);
}

function findIndexForGeoDimension(uri){
    for (var i=0; i<javaGeoPossibleValues.length; i++){
        if (uri == javaGeoPossibleValues[i])
            return i;
    }
    alert('Couldnt find index of ' + uri + ' in ' + javaGeoPossibleValues);
}

function uriLastPart(uri){
    return uri.substring(uri.lastIndexOf('/')+1, uri.length);
}