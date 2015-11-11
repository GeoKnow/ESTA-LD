var endpoint = 'http://147.91.50.167/sparql';
//var endpoint = 'http://jpo.imp.bg.ac.rs/sparql';
//var endpoint = 'http://localhost:8890/sparql';
var sessionEndpoint;

var server = 'http://147.91.50.167/';
//var server = 'http://jpo.imp.bg.ac.rs/';
//var server = 'http://localhost:8890/';
var index;

var graph = 'http://stat.apr.gov.rs/lod2/Register/Regional_Development';
var sessionGraph;






//FIND DIMENSIONS:
//NIJE OVAKO
//select distinct ?relation 
//		where { ?y a qb:Observation. 
//?y ?relation ?o.
//}
//		?y <http://purl.org/linked-data/sdmx/2009/measure#obsValue> ?observation. 
		
		

//FIND DATASETS IN THE GRAPH
//SELECT DISTINCT ?dataset
//WHERE { 
//graph <http://stat.apr.gov.rs/lod2/id/Register/RegionalDevelopmentMeasuresandIncentives> {  
//               
//               ?dataset a qb:DataSet . 
//    
//
//} 
//} 

//FIND DIMENSIONS IN THE DATASET
//select distinct ?dim
//from <graph>								<http://stat.apr.gov.rs/lod2/id/Register/RegionalDevelopmentMeasuresandIncentives>
//where {
//<ds> qb:structure ?dsd . 					?ds ili <http://stat.apr.gov.rs/lod2/RS-DATA/RegDev/data_08>
//?dsd qb:component ?comp . 
//{
//   { 
//     ?comp qb:dimension ?dim . 
//   } 
//   UNION 
//   {
//     ?comp qb:componentProperty ?dim . 
//     ?dim a qb:DimensionProperty . 
//   }
//}
//}



function execSparqlTopGeoBroaderNarrower(callbackFunction) {//FIND TOP ELEMENT (BROADER/NARROWER)
	var sparqlQuery = 'prefix skos: <http://www.w3.org/2004/02/skos/core#> ' +
		'prefix rs: <http://elpo.stat.gov.rs/lod2/RS-DIC/rs/> ' +
                'prefix ogc: <http://www.opengis.net/ont/geosparql#> ' +
		'select distinct ?rsgeo ?geom ' +
                'from <' + javaGraph + '> ' +
		'where { ' + 
		'{{?y1 <' + javaGeoDimension + '> ?rsgeo. ' + 
                'OPTIONAL { ?rsgeo ogc:hasDefaultGeometry ?g . ?g ogc:asWKT ?geom . } ' + 
		//'?rsgeo1 skos:broader ?rsgeo.} ' +  // Vuk: why this line, what if there's only one level?
                '} ' +
		'FILTER NOT EXISTS {{?rsgeo2 skos:narrower ?rsgeo} UNION ' + 
		'{?rsgeo skos:broader ?rsgeo3.}}} ' + 
		
		'UNION  ' + 
		'{{?y2 <' + javaGeoDimension + '> ?rsgeo. ' + 
                'OPTIONAL { ?rsgeo ogc:hasDefaultGeometry ?g . ?g ogc:asWKT ?geom . } ' + 
		//'?rsgeo skos:narrower ?rsgeo4.} ' +  // Vuk: again, why this line, what if there's only one level?
                '} ' +
		'FILTER NOT EXISTS {{?rsgeo5 skos:narrower ?rsgeo} UNION ' + 
		'{?rsgeo skos:broader ?rsgeo6. }}} ' + 
		'}';
	
	var queryUrlEncoded = endpoint + '?query=' + $.URLEncode(sparqlQuery.replace('gYear','date'))+'&format=json';
	
//	$.getJSON(queryUrlEncoded, callbackFunction).error(function() { alert("There was an error during communication with the sparql endpoint"); });
	
	//needs to be synchronous
	$.ajax({
	    async: false,
	    url: queryUrlEncoded,
            dataType: 'jsonp',
	    success: callbackFunction,
	    error: function(msg) { alert("There was an error during communication with the sparql endpoint"); console.error(msg); }
	});
}

function execSparqlBroaderNarrowerForArray(codePrefix, geoStringArray, callbackFunction) {//geostring: '<http://elpo.stat.gov.rs/lod2/RS-DIC/geo/RS>'
	var querySubstring = '{ ?rsgeo skos:broader <' + geoStringArray[0] + '> . OPTIONAL { ?rsgeo ogc:hasDefaultGeometry ?g . ?g ogc:asWKT ?geom . } } ' +
							'UNION { <' + geoStringArray[0] + '> skos:narrower ?rsgeo . OPTIONAL { ?rsgeo ogc:hasDefaultGeometry ?g . ?g ogc:asWKT ?geom . } } ';
	for (var i = 1; i < geoStringArray.length; i++) {
		querySubstring += 'UNION { ?rsgeo skos:broader  <' + geoStringArray[i] + '> . OPTIONAL { ?rsgeo ogc:hasDefaultGeometry ?g . ?g ogc:asWKT ?geom . } } ' +
		'UNION { <' + geoStringArray[i] + '> skos:narrower ?rsgeo . OPTIONAL { ?rsgeo ogc:hasDefaultGeometry ?g . ?g ogc:asWKT ?geom . } } ';
	}
	
	var sparqlQuery = 'prefix skos: <http://www.w3.org/2004/02/skos/core#> ' +
                'prefix ogc: <http://www.opengis.net/ont/geosparql#> ' + 
		'select distinct ?rsgeo ?geom ' +
                'from <' + javaGraph + '> ' +
		'where { ' +
		querySubstring +
		' }';
	
	var queryUrlEncoded = endpoint + '?query=' + $.URLEncode(sparqlQuery.replace('gYear','date'))+'&format=json';
        console.log(queryUrlEncoded);
	
//	$.getJSON(queryUrlEncoded, callbackFunction).error(function() { alert("There was an error during communication with the sparql endpoint"); });
	
	// just a test/workaround for huge queries
//        if (sparqlQuery.length > 10000) {
//            callbackFunction({
//                head: {
//                    link: [], 
//                    vars: ["rsgeo","geom"]
//                }, 
//                results: {
//                    distinct: false, 
//                    ordered: true, 
//                    bindings: []
//                }
//            });
//        } else
        //needs to be synchronous
	$.ajax({
	    async: false,
//	    url: queryUrlEncoded, 
            url: endpoint, 
            data: {
                query: sparqlQuery.replace('gYear','date'), 
                format: 'json'
            }, 
            type: 'POST', 
            method: 'POST', 
//            dataType: 'jsonp',
	    success: callbackFunction,
	    error: function(msg) { 
                alert("There was an error during communication with the sparql endpoint"); 
                console.error(msg); 
                console.error(callbackFunction); 
            }
	});
}

function execSparqlAllGeoCodes(callbackFunction) {//FIND TOP ELEMENT (BROADER/NARROWER)
	var sparqlQuery = 'prefix rs: <http://elpo.stat.gov.rs/lod2/RS-DIC/rs/> ' +
                'prefix ogc: <http://www.opengis.net/ont/geosparql#>' + 
		'select distinct ?rsgeo ?geom ' +
                'from <' + javaGraph + '> ' +
		'where {?y1 <' + javaGeoDimension + '> ?rsgeo. OPTIONAL { ?rsgeo ogc:hasDefaultGeometry ?g . ?g ogc:asWKT ?geom . } }';
	
	var queryUrlEncoded = endpoint + '?query=' + $.URLEncode(sparqlQuery.replace('gYear','date'))+'&format=json';
	
//	$.getJSON(queryUrlEncoded, callbackFunction).error(function() { alert("There was an error during communication with the sparql endpoint"); });
	
	//needs to be synchronous
	$.ajax({
	    url: queryUrlEncoded,
            dataType: 'jsonp',
	    success: callbackFunction,
	    error: function(msg) { alert("There was an error during communication with the sparql endpoint"); console.error(msg); }
	});
}
//function execSparqlNarrower(geoString, callbackFunction) {//geostring: '<http://elpo.stat.gov.rs/lod2/RS-DIC/geo/RS>'
//	var sparqlQuery = 'prefix skos: <http://www.w3.org/2004/02/skos/core#> ' +
//		'select distinct ?rsgeo ' +
//		'where { ' + geoString + ' skos:narrower ?rsgeo.}';
//	
//	var queryUrlEncoded = endpoint + '?query=' + $.URLEncode(sparqlQuery.replace('gYear','date'));
//	
//	$.getJSON(queryUrlEncoded, callbackFunction).error(function() { alert("There was an error during communication with the sparql endpoint"); });
//}

function execSparqlForGeoMapVuk(callbackFunction){
    var sparqlQuery = 'prefix rs: <http://elpo.stat.gov.rs/lod2/RS-DIC/rs/> ' +
				'prefix geo: <http://elpo.stat.gov.rs/lod2/RS-DIC/geo/> ' +
				'prefix apr: <http://stat.apr.gov.rs/lod2/> ' +
                                'prefix qb: <http://purl.org/linked-data/cube#> ' + 
				'prefix sdmx-measure: <http://purl.org/linked-data/sdmx/2009/measure#> ' +
				'select distinct ?rsgeo ?observation ' + 
                                'from <' + javaGraph + '> ' +
                                'where { ?y qb:dataSet <' + javaDataSet + '> . ' + 
                                '?y <' + javaGeoDimension + '> ?rsgeo . ' + 
                                '?y <' + getMeasureUri() + '> ?observation . ';
    var hasTimeDimension = false;
    var timeDimensionUri = '';
    var timeDimensionValue = '';
    for (i=0; i<javaSelectedDimensions.length; i++){
        if (javaHasTimeDimension && i === 0) {
            // if it is a time dimension make it a free variable
            sparqlQuery += '?y <' + javaSelectedDimensions[i] + '> ?rstime . ';
            hasTimeDimension = true;
            timeDimensionUri = javaSelectedDimensions[i];
            timeDimensionValue = javaDimensionValues[i];
        }
        else sparqlQuery += '?y <' + javaSelectedDimensions[i] + '> ' + javaDimensionValues[i] + ' . ';
    }
    sparqlQuery += '}';
    if (hasTimeDimension) { 
        sparqlQuery = sparqlQuery.replace('select distinct ?rsgeo ?observation ', 
            'select distinct ?rstime ?rsgeo ?observation ');
        sparqlQuery += ' order by ?rsgeo';
    }
    
    var queryUrlEncoded = endpoint + '?query=' + $.URLEncode(sparqlQuery.replace('gYear','date'))+'&format=json';

//    $.getJSON(queryUrlEncoded, callbackFunction).error(function() { alert("There was an error during communication with the sparql endpoint\n"+sparqlQuery); });
    
    if (hasTimeDimension) {
        proxyForGeoMapAllTimes(queryUrlEncoded, timeDimensionUri, timeDimensionValue, callbackFunction);
//        $.ajax({
//            url: queryUrlEncoded,
//            dataType: 'jsonp',
//            success: cbfuncForGeoMapAllTimes,
//            error: function() { alert("There was an error during communication with the sparql endpoint");}
//        });
    } else {
//        $('#esta-modal').show();
        $.ajax({
            url: queryUrlEncoded,
            dataType: 'jsonp',
            success: function(data, status, xhr) {
                $('#esta-modal').hide();
                callbackFunction(data, status, xhr);
            },
            error: function() { 
                $('#esta-modal').hide();
                alert("There was an error during communication with the sparql endpoint");
            }
        });
    }
}

function execSparqlRegionalDevelopment(querySubstring, callbackFunction) {
	
	var sparqlQuery = 'prefix rs: <http://elpo.stat.gov.rs/lod2/RS-DIC/rs/> ' +
				'prefix geo: <http://elpo.stat.gov.rs/lod2/RS-DIC/geo/> ' +
				'prefix apr: <http://stat.apr.gov.rs/lod2/> ' +
				'prefix sdmx-measure: <http://purl.org/linked-data/sdmx/2009/measure#> ' +
				'select distinct ?time ?incentive ?rsgeo ?observation ' +
				'where { ?y <' + javaGeoDimension + '> ?rsgeo. ' +
				'?y rs:time ?time. ' +
				'?y apr:incentiveAim ?incentive. ' +
				querySubstring +
				'?y <' + getMeasureUri() + '> ?observation. }';
	
	var queryUrlEncoded = endpoint + '?query=' + $.URLEncode(sparqlQuery.replace('gYear','date'))+'&format=json';

//	$.getJSON(queryUrlEncoded, callbackFunction).error(function() { alert("There was an error during communication with the sparql endpoint"); });
	$.ajax({
	    url: queryUrlEncoded,
            dataType: 'jsonp',
	    success: callbackFunction,
	    error: function() { alert("There was an error during communication with the sparql endpoint");}
	});
	
//	var query = endpoint + '?query=PREFIX+rs%3A+%3Chttp%3A%2F%2Felpo.stat.gov.rs%2Flod2%2FRS%2DDIC%2Frs%2F%3E+PREFIX+geo%3A+%3Chttp%3A%2F%2Felpo.stat.gov.rs%2Flod2%2FRS%2DDIC%2Fgeo%2F%3E+PREFIX+apr%3A+%3Chttp%3A%2F%2Fstat.apr.gov.rs%2Flod2%2F%3E+PREFIX+sdmx%2Dmeasure%3A+%3Chttp%3A%2F%2Fpurl.org%2Flinked%2Ddata%2Fsdmx%2F2009%2Fmeasure%23%3E+SELECT+DISTINCT+%3Ftime+%3Fincentive+%3Frsgeo+%3Fobservation+WHERE+%7B+%3Fy+rs%3Ageo+%3Frsgeo.+%3Fy+rs%3Atime+%3Ftime.+%3Fy+apr%3AincentiveAim+%3Fincentive.+%3Fy+rs%3Atime+%3Chttp%3A%2F%2Felpo.stat.gov.rs%2Flod2%2FRS%2DDIC%2Ftime%2FY' + year + '%3E.+%3Fy+apr%3AincentiveAim+%3Chttp%3A%2F%2Fstat.apr.gov.rs%2Flod2%2FRS%2DDIC%2FIncentivePurpose%2F' + incentive + '%3E.+%3Fy+sdmx%2Dmeasure%3AobsValue+%3Fobservation.}';
	
//	$.sparql(server + 'sparql')
//	  .prefix('rs','http://elpo.stat.gov.rs/lod2/RS-DIC/rs/')
//	  .prefix('geo','http://elpo.stat.gov.rs/lod2/RS-DIC/geo/')
//	  .prefix('apr','http://stat.apr.gov.rs/lod2/')
//	  .prefix('sdmx-measure','http://purl.org/linked-data/sdmx/2009/measure#')
//	  .select(['?time', '?incentive', '?rsgeo', '?observation'])
//	    .where('?y','rs:geo', '?rsgeo')
//	    .where('?y','rs:time', '?time')
//	    .where('?y','apr:incentiveAim', '?incentive')
//	    .where('?y','rs:time', yearUrlString )
//	    .where('?y','apr:incentiveAim', incentiveUrlString )
//	    .where('?y','<' + getMeasureUri() + '>', '?observation')
//	    .distinct()
//	  .execute(callbackFunction);
	
}

function execSparqlYearIncentive(yearUrlString, incentiveUrlString, callbackFunction) {
	var querySubstring = '?y rs:time ' + yearUrlString + '. ' +
							'?y apr:incentiveAim ' + incentiveUrlString + '. ';
	execSparqlRegionalDevelopment(querySubstring, callbackFunction);
}

function execSparqlRsgeoIncentive(rsgeoString, incentiveUrlString, callbackFunction) {
	var querySubstring = '?y <' + javaGeoDimension + '> <' + rsgeoString + '>. ' +
							'?y apr:incentiveAim ' + incentiveUrlString + '. ';
	execSparqlRegionalDevelopment(querySubstring, callbackFunction);
}

function execSparqlRsgeoYear(rsgeoString, yearUrlString, callbackFunction) {
	var querySubstring = '?y <' + javaGeoDimension + '> <' + rsgeoString + '>. ' +
							'?y rs:time ' + yearUrlString + '. ' ;
	execSparqlRegionalDevelopment(querySubstring, callbackFunction);
}

function execSparqlRsgeo(rsgeoString, callbackFunction) {
	var querySubstring = '?y <' + javaGeoDimension + '> <' + rsgeoString + '>. ' ;
	execSparqlRegionalDevelopment(querySubstring, callbackFunction);
}

function execSparqlGeoSelectedVuk(rsgeoString, callbackFunction) {
    var sparqlQuery = 'prefix rs: <http://elpo.stat.gov.rs/lod2/RS-DIC/rs/> ' +
				'prefix geo: <http://elpo.stat.gov.rs/lod2/RS-DIC/geo/> ' +
				'prefix apr: <http://stat.apr.gov.rs/lod2/> ' +
                                'prefix qb: <http://purl.org/linked-data/cube#> ' + 
				'prefix sdmx-measure: <http://purl.org/linked-data/sdmx/2009/measure#> ' +
				'select distinct ?observation ?dim1 ?dim2 ' + 
                                'from <' + javaGraph + '> ' +
                                'where { ?y qb:dataSet <' + javaDataSet + '> . ' + 
                                '?y <' + javaGeoDimension + '> <' + rsgeoString + '> . ' + 
                                '?y <' + getMeasureUri() + '> ?observation . ' + 
                                '?y <' + javaSelectedDimensions[0] + '> ?dim1 . ' +
                                '?y <' + javaSelectedDimensions[1] + '> ?dim2 . }' +
                                'order by ?dim1 ?dim2';
    
    var queryUrlEncoded = endpoint + '?query=' + $.URLEncode(sparqlQuery.replace('gYear','date'))+'&format=json';

//    $.getJSON(queryUrlEncoded, callbackFunction).error(function() { alert("There was an error during communication with the sparql endpoint\n"+sparqlQuery); });


    $.ajax({
        url: queryUrlEncoded,
        dataType: 'jsonp',
        success: callbackFunction,
        error: function() { alert("There was an error during communication with the sparql endpoint");}
    });
}

function execSparqlDimensionValueChangedVuk(cbfuncOneFreeVuk,cbfuncTwoFreeVuk){
    var sparqlQuery = 'prefix rs: <http://elpo.stat.gov.rs/lod2/RS-DIC/rs/> ' +
				'prefix geo: <http://elpo.stat.gov.rs/lod2/RS-DIC/geo/> ' +
				'prefix apr: <http://stat.apr.gov.rs/lod2/> ' +
                                'prefix qb: <http://purl.org/linked-data/cube#> ' + 
				'prefix sdmx-measure: <http://purl.org/linked-data/sdmx/2009/measure#> ' +
				'select distinct ?observation ?dim1 ?dim2 ' + 
                                'from <' + javaGraph + '> ' +
                                'where { ?y qb:dataSet <' + javaDataSet + '> . ' + 
                                '?y <' + getMeasureUri() + '> ?observation . ';
    if (javaGeoValue != null && javaGeoValue != ''){
        if (javaGeoFree){
            if (javaFreeDimensions.length == 0)
                sparqlQuery += '?y <' + javaGeoDimension + '> ?dim1 . ';
            else
                sparqlQuery += '?y <' + javaGeoDimension + '> ?dim2 . ';
        } else {
            sparqlQuery += '?y <' + javaGeoDimension + '> ' + javaGeoValue + ' . ';
        }
    }
    var dateValue = null;
    for (var i=0; i<javaSelectedDimensions.length; i++){
        var freeIndex = javaFreeDimensions.indexOf(i);
        if (freeIndex == -1){
            if (i === 0 && javaHasTimeDimension) {
                // workaround for date values
                sparqlQuery += '?y <' + javaSelectedDimensions[i] + '> ?date . ';
                // now augment the dateValue
                // first remove type information if it is literal
                dateValue = javaDimensionValues[i];
                var caretsIndex = dateValue.indexOf('^^');
                if (caretsIndex > -1) dateValue = dateValue.slice(0, caretsIndex);
                // ending with Z bothers Virtuoso
                dateValue = dateValue.replace('Z"','"');
                // ending with +HH:MM also bother virtuoso
                var plusIndex = dateValue.lastIndexOf('+');
                if (plusIndex > -1 && caretsIndex > -1) {
                    dateValue = dateValue.slice(0, plusIndex) + '"';
                }
            } else {
                sparqlQuery += '?y <' + javaSelectedDimensions[i] + '> ' + javaDimensionValues[i] + ' . ';
            }
        } else {
            var dim = '?dim' + (freeIndex+1).toString();
            sparqlQuery += '?y <' + javaSelectedDimensions[i] + '> ' + dim + ' . ';
        }
    }
    if (dateValue !== null) {
        sparqlQuery += 'FILTER(STRSTARTS(STR(?date), STR(' + dateValue + '))) ';
    }
    sparqlQuery += '} order by ?dim1';
    var numFree = javaFreeDimensions.length;
    if (javaGeoValue != null && javaGeoValue != '' && javaGeoFree) numFree++;
    if (numFree == 2) sparqlQuery += ' ?dim2';
    else if (numFree === 1) sparqlQuery = sparqlQuery.replace('?dim2','');
    
    var queryUrlEncoded = endpoint + '?query=' + $.URLEncode(sparqlQuery.replace('gYear','date'))+'&format=json';
    
    if (numFree == 2){
//        $.getJSON(queryUrlEncoded, cbfuncTwoFreeVuk).error(function() { alert("There was an error during communication with the sparql endpoint\n"+sparqlQuery); });
        
//        $('#esta-modal').show();
        $.ajax({
	    url: queryUrlEncoded,
		dataType: 'jsonp',
	    success: function(data, status, xhr) {
                $('#esta-modal').hide();
                cbfuncTwoFreeVuk(data, status, xhr);
            },
	    error: function() { 
                $('#esta-modal').hide(); 
                alert("There was an error during communication with the sparql endpoint");
            }
	});
    } else if (numFree == 1){
//        $.getJSON(queryUrlEncoded, cbfuncOneFreeVuk).error(function() { alert("There was an error during communication with the sparql endpoint\n"+sparqlQuery); });
        
//        $('#esta-modal').show();
        $.ajax({
	    url: queryUrlEncoded,
		dataType: 'jsonp',
	    success: function(data, status, xhr) {
                $('#esta-modal').hide();
                cbfuncOneFreeVuk(data, status, xhr);
            },
	    error: function() { 
                $('#esta-modal').hide();
                alert("There was an error during communication with the sparql endpoint");
            }
	});
    } else {
        $('#esta-modal').hide();
    }
}

function execSparqlIncentive(incentiveUrlString, callbackFunction) {
	var querySubstring = '?y apr:incentiveAim ' + incentiveUrlString + '. ' ;
	execSparqlRegionalDevelopment(querySubstring, callbackFunction);
}

function execSparqlYear(yearUrlString, callbackFunction) {
	var querySubstring = '?y rs:time ' + yearUrlString + '. ' ;
	execSparqlRegionalDevelopment(querySubstring, callbackFunction);
}

function execSparqlGraphs() {
	
	var inputEndpoint = $('#endpoint').val();
	var sparqlQuery = 
	'prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> ' +
	'prefix qb: <http://purl.org/linked-data/cube#> ' +
	'SELECT DISTINCT ?g  ' +
	'{ ' +
	'GRAPH ?g {  ' +
	'?s a qb:Observation ' +
//	'?s ?p ?o' +//	get all graphs
	'} .  }';

	var queryUrlEncoded = inputEndpoint + '?query=' + $.URLEncode(sparqlQuery.replace('gYear','date'))+'&format=json';
	
//	$.getJSON(queryUrlEncoded, cbFuncGraph).error(function() { 
//		$("#error").css('visibility', 'visible');//display error message on screen
//		$('#launch').attr('disabled','disabled');//disable launch button
//		$("#launch").attr('src', 'resources/images/launch_d.png');
//	});

        $.ajax({
	    url: queryUrlEncoded,
		dataType: 'jsonp',
	    success: cbFuncGraph,
	    error: function() { alert("There was an error during communication with the sparql endpoint");}
	});
}

function exploreWithCubeViz() {
	window.open(server + 'ontowiki/cubeviz/?m=' + $.URLEncode(graph));
}
// http://fraunhofer2.imp.bg.ac.rs/ontowiki/cubeviz/?m=http%3A%2F%2Fstat.apr.gov.rs%2Flod2%2FRegister%2FRegional_Development

function sparqlqueryInitVuk(){
    sessionEndpoint = sessionStorage.getItem("endpoint");
if (sessionEndpoint != null) {
	endpoint = sessionEndpoint;
}

index = endpoint.indexOf('sparql');
if (index > -1) {
	server = endpoint.substring(0, index);
}

sessionGraph = sessionStorage.getItem("graph");
if (sessionGraph != null) {
	graph = sessionGraph;
}


}