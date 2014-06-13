var javaSelectedDimensions = [];
var javaDimensionValues = [];
var javaFreeDimensions = [];
var javaGraph = '';
var javaDataSet = '';

function javaSetSelectedDimensions(dims){
    javaSelectedDimensions = dims;
}

function javaSetDimensionValues(vals){
    javaDimensionValues = vals;
}

function javaSetFreeDimensions(dims){
    javaFreeDimensions = dims;
}

function javaSetAll(dims,vals,free){
    javaSelectedDimensions = dims;
    javaDimensionValues = vals;
    javaFreeDimensions = free;
    runSparqlForGeoMapVuk();
}

function javaSetGraphAndDataSet(graph,ds){
    javaGraph = graph;
    javaDataSet = ds;
    sessionStorage.setItem('endpoint','http://jpo.imp.bg.ac.rs/sparql');
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
}

function javaPrintAll(){
    alert('Graph: ' + javaGraph +
            '\nDataSet: ' + javaDataSet +
            '\nSelected dims: ' + javaSelectedDimensions.toString() +
            '\nSelected values: ' + javaDimensionValues.toString() +
            '\nFree dims: ' + javaFreeDimensions.toString());
}