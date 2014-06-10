
var hashObservationValues = new Object();
	   
	    var prefix = "http://elpo.stat.gov.rs/lod2/RS-DIC/geo/";
	    
	    var cbfunc = function(results) {
			for (var i = 0; i < results.length; i++) {
				var uri = results[i].rsgeo.uri;
				var code = uri.substring(prefix.length, uri.length);
				var value = results[i].observation;
				hashObservationValues[code] = value;
			}
			
//			for (var j = 1; j < 4; j++) {
				
//				var code = "RS1" + j.toString();
// 				alert(code);
// 				alert (hashObservationValues[code]);
//			}

// 			alert (results[0].time.uri);
// 			alert (results[0].observation);
// 			alert (results[0].rsgeo.uri);
	        $("#results").val(JSON.stringify(results));
	    };

//       $(document).ready(function() {
//         $("#run_sparql").click(function(e) {
//           $.sparql("http://dbpedia.org/sparql")
//             .prefix("rdfs","http://www.w3.org/2000/01/rdf-schema#")
//             .select(["?label"])
//               .where("<http://dbpedia.org/resource/Tim_Berners-Lee>","rdfs:label","?label")
//             .execute(cbfunc);
//           return false;
//         });
//          });

	
var yearUrlString = '<http://elpo.stat.gov.rs/lod2/RS-DIC/time/Y2009>';
var incentiveUrlString = '<http://stat.apr.gov.rs/lod2/RS-DIC/IncentivePurpose/Total>';
//
//
//var yearUrlString = '2009';
//var incentiveUrlString = 'PT01';
//
//var query1 = 'http://fraunhofer2.imp.bg.ac.rs/sparql?query=PREFIX+rs%3A+%3Chttp%3A%2F%2Felpo.stat.gov.rs%2Flod2%2FRS%2DDIC%2Frs%2F%3E+PREFIX+geo%3A+%3Chttp%3A%2F%2Felpo.stat.gov.rs%2Flod2%2FRS%2DDIC%2Fgeo%2F%3E+PREFIX+apr%3A+%3Chttp%3A%2F%2Fstat.apr.gov.rs%2Flod2%2F%3E+PREFIX+sdmx%2Dmeasure%3A+%3Chttp%3A%2F%2Fpurl.org%2Flinked%2Ddata%2Fsdmx%2F' + yearUrlString + '%2Fmeasure%23%3E+SELECT+DISTINCT+%3Ftime+%3Fincentive+%3Frsgeo+%3Fobservation+WHERE+%7B+%3Fy+rs%3Ageo+%3Frsgeo.+%3Fy+rs%3Atime+%3Ftime.+%3Fy+apr%3AincentiveAim+%3Fincentive.+%3Fy+rs%3Atime+%3Chttp%3A%2F%2Felpo.stat.gov.rs%2Flod2%2FRS%2DDIC%2Ftime%2FY' + yearUrlString + '%3E.+%3Fy+apr%3AincentiveAim+%3Chttp%3A%2F%2Fstat.apr.gov.rs%2Flod2%2FRS%2DDIC%2FIncentivePurpose%2F' + incentiveUrlString + '%3E.+%3Fy+sdmx%2Dmeasure%3AobsValue+%3Fobservation.}';
//
//var rsgeostring = 'RS111';
//var query2 = 'http://fraunhofer2.imp.bg.ac.rs/sparql?query=PREFIX+rs%3A+%3Chttp%3A%2F%2Felpo.stat.gov.rs%2Flod2%2FRS%2DDIC%2Frs%2F%3E+PREFIX+geo%3A+%3Chttp%3A%2F%2Felpo.stat.gov.rs%2Flod2%2FRS%2DDIC%2Fgeo%2F%3E+PREFIX+apr%3A+%3Chttp%3A%2F%2Fstat.apr.gov.rs%2Flod2%2F%3E+PREFIX+sdmx%2Dmeasure%3A+%3Chttp%3A%2F%2Fpurl.org%2Flinked%2Ddata%2Fsdmx%2F2009%2Fmeasure%23%3E+SELECT+DISTINCT+%3Ftime+%3Fincentive+%3Frsgeo+%3Fobservation+WHERE+%7B+%3Fy+rs%3Ageo+%3Frsgeo.+%3Fy+rs%3Atime+%3Ftime.+%3Fy+apr%3AincentiveAim+%3Fincentive.+%3Fy+rs%3Ageo+%3Chttp%3A%2F%2Felpo.stat.gov.rs%2Flod2%2FRS%2DDIC%2Fgeo%2F' + rsgeostring + '%3E.+%3Fy+sdmx%2Dmeasure%3AobsValue+%3Fobservation.}';
                      





//var endpoint = 'http://linkedgeodata.org/sparql';
//
//var sparqlQuery = 'Prefix lgdo: <http://linkedgeodata.org/ontology/>'+
//
//	'Select *'+
//	'from <http://linkedgeodata.org> {'+
//	 ' ?s lgdo:schemaIcon ?o .'+
//	'}';


var endpoint = 'http://fraunhofer2.imp.bg.ac.rs/sparql';
var sparqlQuery = 'prefix rs: <http://elpo.stat.gov.rs/lod2/RS-DIC/rs/> ' +
'prefix geo: <http://elpo.stat.gov.rs/lod2/RS-DIC/geo/> ' +
'prefix apr: <http://stat.apr.gov.rs/lod2/> ' +
'prefix sdmx-measure: <http://purl.org/linked-data/sdmx/2009/measure#> ' +
'select distinct ?time ?incentive ?rsgeo ?observation ' +
'where { ?y rs:geo ?rsgeo. ' +
'?y rs:time ?time. ' +
'?y apr:incentiveAim ?incentive. ' +
'?y rs:time ' + yearUrlString + '. ' +
'?y apr:incentiveAim ' + incentiveUrlString + '. '+
'?y sdmx-measure:obsValue ?observation. }';

var queryUrlEncoded = endpoint + '?query=' + $.URLEncode(sparqlQuery);


      $(document).ready(function() {
          $("#run_sparql").click(function(e) {
//        	  $.sparql("http://fraunhofer2.imp.bg.ac.rs/sparql")
//        	  .prefix("rs","http://elpo.stat.gov.rs/lod2/RS-DIC/rs/")
//        	  .prefix("geo","http://elpo.stat.gov.rs/lod2/RS-DIC/geo/")
//        	  .prefix("apr","http://stat.apr.gov.rs/lod2/")
//        	  .prefix("sdmx-measure","http://purl.org/linked-data/sdmx/2009/measure#")
//        	  .prefix("qb","http://purl.org/linked-data/cube#")
//        	  .select(["?rsgeo", "?obsindicator", "?obsturists", "?time", "?qbdataset", "?qbmeasuretype", "?observation"])
//        	    .where("?y","rs:geo", "?rsgeo")
//        	    .where("?y","rs:obsIndicator", "?obsindicator")
//        	    .where("?y","rs:obsTurists", "?obsturists")
//        	    .where("?y","rs:time", "?time")
//        	    .where("?y","qb:dataSet", "?qbdataset")
//        	    .where("?y","qb:measureType", "?qbmeasuretype")
//        	    .where("?y","sdmx-measure:obsValue", "?observation")
//        	    .distinct()
//        	    .limit(100)
//              .execute(cbfunc);            

        	  $.getJSON(queryUrlEncoded, cbfunc).error(function() { alert("There was an error during communication with the sparql endpoint"); });

        		  
        	  return false;
           });
      });
      

	  function getYear() {
		year = $("#year").val();

		yearLink = '<http://elpo.stat.gov.rs/lod2/RS-DIC/time/Y' + year + '>';
	  }
	  
	  function getIncentive() {
		incentiveCode = $("#incentive").val();

		incentiveLink = '<http://stat.apr.gov.rs/lod2/RS-DIC/IncentivePurpose/' + incentiveCode + '>';
	  }
    	  