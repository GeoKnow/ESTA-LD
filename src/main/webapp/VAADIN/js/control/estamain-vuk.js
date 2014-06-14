var firstRun = true;
											//var visibleLayer = 'Areas';//Area - Region - Municipality selection
var visibleGeoLevel = 2;// visible geo layer level

//selected analysis type
var analysisType;

var YEARS = ['2009', '2010', '2011', '2012'];

var INCENTIVE_NAMES = [['Total', 'PT01', 'PT02', 'PT03', 'PT04', 'PT05', 'PT06', 'PT07', 'PT08', 'PT09', 'PT10', 
                       'PT11', 'PT12', 'PT13', 'PT14', 'PT15', 'PT16', 'PT17', 'PT18', 'PT19', 'PT20'],
                       ['Total', 'Employment', 'Human resources', 'Export', 'Production', 'Agriculture', 'R&D', 'Spatial planning', 'Corporate restructuring', 'Environmental protection', 'Environmental infrastructure',
                       'Traffic infrastructure', 'Communal infrastructure', 'Energy infrastructure', 'Economy infrastructure', 'Infrastructure - other', 'Health', 'Education,science, culture,sport', 'Social protection', 'Institution building', 'Other']
                      ];

var CODE_LENGTH_COUNTRY = 2;//only country data (RS)
var CODE_LENGTH_AREA = 5;//only area data (RS###)
var CODE_LENGTH_REGION = 4;//only region data (RS##)

var hashIncentiveNames = new Object();

var rsgeoSelected = 'RS';

var geoLevels = [];//geo levels of codes (populated by broader-narrower queries

function domReadyVuk() {
	
	//Setup the ajax indicator ('loading')
	$('body').append('<div id="ajaxBusy"><p><img src="resources/images/loading.gif"></p></div>');
	
	$('#ajaxBusy').css({
		display:'none',
		margin:'0px',
		paddingLeft:'0px',
		paddingRight:'0px',
		paddingTop:'0px',
		paddingBottom:'0px',
		position:'absolute',
		left:'900px',
		top:'400px',
		width:'auto'
	});
	
	//Ajax activity indicator bound to ajax start/stop document events
	$(document).ajaxStart(function(){
		$('#ajaxBusy').show();
	}).ajaxStop(function(){
		$('#ajaxBusy').hide();
	});
	
						//onclick function for visiblelayer radio
//						$('input:radio[name=visibleLayer]').click(function() {
//							rsgeoSelected = 'RS';
//							
//							$('input:radio[name=independentParameter][value=Geo]').prop('checked', true);
//							
//							visibleLayer = $(this).val();
//							refreshMap();
//							refreshSpaceChart();
//						});
	
	//onclick for plus and minus buttons for selecting geo level
	// Plus button will increment the visibleLayerLevel till max
    $('#geoplus').click(function(e){
        // Stop acting like a button
        e.preventDefault();
        // Increment
        visibleGeoLevel++;
        
    	$("#geominus").attr("disabled", false);
        
        if (visibleGeoLevel === geoLevels.length - 1) {
        	$("#geoplus").attr("disabled", 'disabled');
        } else {
        	$("#geoplus").attr('disabled',false);
	    }
        
        $('#geoLevelLabel').val('Level ' + visibleGeoLevel);

        geoLevelChanged();
    });
    // Minus button will decrement the visibleLayerLevel till 0
    $("#geominus").click(function(e) {
        // Stop acting like a button
        e.preventDefault();
        // Decrement one
        visibleGeoLevel--;
        
        if (visibleGeoLevel === 0) {
        	$("#geominus").attr('disabled','disabled');
        } else {
        	$("#geominus").attr("disabled", false);
        }
        
        $("#geoplus").attr('disabled',false);
        
        $('#geoLevelLabel').val('Level ' + visibleGeoLevel);

        geoLevelChanged();
    });
	
	//onclick function for independentParameter radio
	$('input:radio[name=independentParameter]').click(function() {
		var independentParameter = $(this).val();
		
		if (independentParameter === 'Geo') {
			runSparqlYearIncentive();
		} else if (independentParameter === 'Incentive') {
			runSparqlRsgeoYear();
		} else if (independentParameter === 'Time') {
			runSparqlRsgeoIncentive();
		}
	});
	
	//onchange independentParameter buttons
	$(".toggle-btn:not('.noscript') input[type=radio]").addClass("visuallyhidden");
	$(".label-btn:not('.noscript') input[type=radio]").addClass("visuallyhidden");
	$(".toggle-btn:not('.noscript') input[type=radio]").change(function() {
	    if( $(this).attr("name") ) {
	        $(this).parent().addClass("success").siblings().removeClass("success");
	    } else {
	        $(this).parent().toggleClass("success");
	    }
	});
	
	//onchange year, incentive dropdown lists
	$('#year, #incentive').change(function() {

		$('input:radio[name=independentParameter][value=Geo]').prop('checked', true);
		runSparqlYearIncentive();
	});
	
	//onclick explorecubeviz
	$('#explorecubeviz').click(function() {
		exploreWithCubeViz();
	});
	
	//set the incentives drop-down values
	for (var i = 0; i < INCENTIVE_NAMES[0].length; i++) {
		var incentiveCode = INCENTIVE_NAMES[0][i];
		var incentiveName = INCENTIVE_NAMES[1][i];
		
		hashIncentiveNames[incentiveCode] = incentiveName;
		
		//fill incentives select element
		var selectedString = (i === 0) ? 'selected="selected"' : '';
		var itemval = '<option value="' + incentiveCode + '" ' + selectedString + '">' + incentiveName + '</option>';
	    $('#incentive').append(itemval);
	}

	//set the years drop-down values
	for (var i = 0; i < YEARS.length; i++) {
		//fill years select element
		var selectedString = (i === 0) ? 'selected="selected"' : '';
		var itemval = '<option value="' + YEARS[i] + '" ' + selectedString + '">' + YEARS[i] + '</option>';
	    $('#year').append(itemval);
	}
	
	
	//populate geo levels lists
	populateGeoLevelsLists();
	
	//initially run sparql query depending on the selected analysis
	runSparqlYearIncentive();//run anyway to get the data for the map
	if (analysisType === 'bartime') {
		runSparqlRsgeoIncentive();
		$('input:radio[name=independentParameter][value=Time]').prop('checked', true);
		
	} else {
		
	}
	
	
	
	
};

function geoLevelChanged() {
	rsgeoSelected = 'RS';
	
	$('input:radio[name=independentParameter][value=Geo]').prop('checked', true);
	
	refreshMap();
	refreshSpaceChart();
}


//charts variables
var chartBarSpaceCategories = [];
var geoLayerNames = [];
//						var regionNames = [];
//						var areaNames = [];
//						var countryNames = [];
var chartBarSpaceValues = [];
var geoLayerValues = [];
//						var regionValues = [];
//						var areaValues = [];
//						var countryValues = [];



var hashCodeToObservationValues = new Object();
var hashCodeToNames = new Object();
hashCodeToNames['RS'] = 'Serbia';
var minObservationValue = 0;
var maxObservationValue = 0;
//						var minAreaObservationValue = 0;
//						var maxAreaObservationValue = 0;
//						var minRegionObservationValue = 0;
//						var maxRegionObservationValue = 0;
//						var minCountryObservationValue = 0;
//						var maxCountryObservationValue = 0;


function loadHashCodeToNames(featureProperties) {

	if (featureProperties.NAME != undefined) {
	
		hashCodeToNames[featureProperties.NSTJ_CODE] = featureProperties.NAME;
	}
	hashCodeToNames[featureProperties.MAT_BR_OPS] = featureProperties.OPSTINA;
}

//Does observation value for given code exist
function isObservationValueDefined(code) {
	return hashCodeToObservationValues[code] != null;
}

//prefixes for sparql queries
var CODE_PREFIX = 'http://elpo.stat.gov.rs/lod2/RS-DIC/geo/';
var YEAR_PREFIX = '<http://elpo.stat.gov.rs/lod2/RS-DIC/time/Y';
var INCENTIVE_PREFIX = '<http://stat.apr.gov.rs/lod2/RS-DIC/IncentivePurpose/';


function loadGeoNamesAndValues() {
	minObservationValue = 0;
	maxObservationValue = 0;

	geoLayerNames.length = 0;
	geoLayerValues.length = 0;
	var selectedGeoLevelCodes = geoLevels[visibleGeoLevel];
	
	for (var i = 0; i < selectedGeoLevelCodes.length; i++) {
		var code = selectedGeoLevelCodes[i];
		var value = hashCodeToObservationValues[code];
		
		var intValue = parseInt(value);
		
		if (maxObservationValue === 0 || intValue > maxObservationValue) {
			maxObservationValue = intValue;
		} 
		if (minObservationValue === 0 || intValue < minObservationValue) {
			minObservationValue = intValue;
		}
		
		geoLayerNames.push(hashCodeToNames[code]);//find area names
		geoLayerValues.push(intValue);
	}
	
	//Sort the area values
	sortArrays(geoLayerValues, geoLayerNames, false);
	
}

//callback function - fills the hash with the keys and values and finds new min and max observation values
//from sparql query with year and incentive fixed
var cbfuncForGeoMap;

var cbfuncYearIncentive;






var cbfuncRsgeoIncentive;







var cbfuncRsgeoYear;

//callback function //from sparql query with Rsgeo fixed
var cbfuncRsgeo;

var cbfuncGeoSelectedVuk;

//callback function //from sparql query with Incentive fixed
var cbfuncIncentive;

//callback function //from sparql query with Year fixed
var cbfuncYear;

var cbfuncGetGeoCodes;

//function sorts arrays with values and names
function sortArrays(arrayWithValues, arrayWithNames, ascending) {
	var size = arrayWithValues.length;
	for(var i = 0;i < size;i++){
	    var j = i;
	    for(var k = i;k < size;k++){
	    	if (ascending) {
		      if(arrayWithValues[j] > arrayWithValues[k]){
		        j = k;
		      }
	    	} else {
	    	  if(arrayWithValues[j] < arrayWithValues[k]){
		        j = k;
		      }
	    	}
	    }
	    var tmp = arrayWithValues[i];
	    arrayWithValues[i] = arrayWithValues[j];
	    arrayWithValues[j] = tmp;
	    
	    var stringTmp = arrayWithNames[i];
	    arrayWithNames[i] = arrayWithNames[j];
	    arrayWithNames[j] = stringTmp;
	}
}

//find the index of the value in array
function findIndex(array, value) {
	for (var i = 0; i < array.length; i++) {
		if (array[i] === value) {
			return i;
		}
	}
	return 0;
}

//refresh map to display new data
function refreshMap() {
	var data = [];

	data = geoData[visibleGeoLevel];
	
	loadGeoNamesAndValues();
	
	
//	if (visibleLayer === 'Country') {
//		data = geoData[0];
//	} else if (visibleLayer === 'Regions') {
//		data = geoData[1];
//	} else if (visibleLayer === 'Areas') {
//		data = geoData[2];
//	} else if (visibleLayer === 'Municipalities') {
//		data = geoData[3];
//	}

//											var minObservationValue = 0;
//											var maxObservationValue = 0;
//											if (visibleLayer === 'Areas') {
//												minObservationValue = minAreaObservationValue;
//												maxObservationValue = maxAreaObservationValue;
//											} else if (visibleLayer === 'Regions') {
//												minObservationValue = minRegionObservationValue;
//												maxObservationValue = maxRegionObservationValue;
//											} else if (visibleLayer === 'Country') {
//												minObservationValue = minCountryObservationValue;
//												maxObservationValue = maxCountryObservationValue;
//											}
	redrawMap(data, minObservationValue, maxObservationValue);
}

//refresh chart to display new data
function refreshSpaceChart() {
	hideCharts();

//											if (visibleLayer === 'Areas') {
//												chartBarSpaceCategories = areaNames;
//												chartBarSpaceValues = areaValues;
//											} else if (visibleLayer === 'Regions') {
//												chartBarSpaceCategories = regionNames;
//												chartBarSpaceValues = regionValues;
//											} else if (visibleLayer === 'Municipalities') {
//												chartBarSpaceCategories = [];
//												chartBarSpaceValues = [];
//											} else if (visibleLayer === 'Country') {
//												chartBarSpaceCategories = countryNames;
//												chartBarSpaceValues = countryValues;
//											}
	chartBarSpaceCategories = geoLayerNames;
	chartBarSpaceValues = geoLayerValues;		

	var displayYear = $('#year').val();//year displayed in the info and charts
	var displayIncentiveName = hashIncentiveNames[$('#incentive').val()];//incentive aim displayed in the info and charts
	
	var subtitle = 'Year: ' + displayYear + ', Incentive aim: ' + displayIncentiveName;
	
	if (analysisType === 'histogramspace') {
		var histogramSpaceCategories = getRangesLabelsForChart();
	//	histogramSpaceCategories.push('No data');
		var categoriesSize = histogramSpaceCategories.length;
		var histogramSpaceValues = [];
		if (chartBarSpaceValues.length > 0) {//prevent creation of histogram chart if there is no data
			for (var i = 0; i < categoriesSize; i++) {
		    	histogramSpaceValues[i] = 0;
		    }
		}
		for (var i = 0; i < chartBarSpaceValues.length; i++) {
			var index = getRangeIndex(chartBarSpaceValues[i]);
			if (index > -1) {//No data
				histogramSpaceValues[index]++;
			} else {
	//			histogramSpaceValues[categoriesSize - 1]++;// increment 'no data'
			}
		}
		
		createChartBarSingle(subtitle, histogramSpaceCategories, histogramSpaceValues, 'Number');
	} else {
		createChartBarSingle(subtitle, chartBarSpaceCategories, chartBarSpaceValues, 'Incentives');
	}
}

//return array of strings representing ranges of values (from map legend)
function getRangesLabelsForChart() {
	var rangesLabels = new Array(colorGradeValues.length);
	for (var i = 0; i < colorGradeValues.length; i++) {
		rangesLabels[i] = addThousandsSeparators(colorGradeValues[i]) + (colorGradeValues[i + 1] ? ' - ' +
	    		addThousandsSeparators(colorGradeValues[i + 1]): '+');
	}
	return rangesLabels;
}

//run sparql query with year and incentive parameters (execute cbfuncYearIncentive in the end)
function runSparqlForGeoMapVuk() {
	hideCharts();
//	loading = true;
	
//	var year = $('#year').val();
//	var incentiveCode = $('#incentive').val();
//	
	$('body').css('cursor', 'wait');
	
	execSparqlForGeoMapVuk(cbfuncForGeoMap);
}


//run sparql query with year and incentive parameters (execute cbfuncYearIncentive in the end)
function runSparqlYearIncentive() {
	hideCharts();
//	loading = true;
	
	var yearUrl = YEAR_PREFIX + $('#year').val() + '>';//year url used in the sparql query
	var incentiveUrl = INCENTIVE_PREFIX + $('#incentive').val() + '>';//incentive url used in the sparql query
	
//	var year = $('#year').val();
//	var incentiveCode = $('#incentive').val();
//	
	$('body').css('cursor', 'wait');
	
	execSparqlYearIncentive(yearUrl, incentiveUrl, cbfuncYearIncentive);
}

//run sparql query with rsgeo and incentive parameters (execute cbfuncRsgeoIncentive in the end)
function runSparqlRsgeoIncentive() {
	hideCharts();
	
	var rsgeoString = CODE_PREFIX + rsgeoSelected;
	var incentiveUrl = INCENTIVE_PREFIX + $('#incentive').val() + '>';//incentive url used in the sparql query
	
	$('body').css('cursor', 'wait');
	
	execSparqlRsgeoIncentive(rsgeoString, incentiveUrl, cbfuncRsgeoIncentive);

}

//run sparql query with rsgeo and year parameters (execute cbfuncRsgeoYear in the end)
function runSparqlRsgeoYear() {
	hideCharts();
	
	if (rsgeoSelected == null) {
		rsgeoSelected = 'RS';
	}
	
	var rsgeoString = CODE_PREFIX + rsgeoSelected;
	
	var yearUrl = YEAR_PREFIX + $('#year').val() + '>';//year url used in the sparql query
	
	$('body').css('cursor', 'wait');
	
	execSparqlRsgeoYear(rsgeoString, yearUrl, cbfuncRsgeoYear);

}

//run sparql query with rsgeo parameter (execute cbfuncRsgeo in the end)
function runSparqlRsgeo(rsgeo) {
	hideCharts();
	//uncheck independentParameter radio buttons
	$('input:radio[name=independentParameter]').prop('checked', false);
	
	$('body').css('cursor', 'wait');
	
	execSparqlRsgeo(CODE_PREFIX + rsgeo, cbfuncRsgeo);
}

// run sparql query with rsgeo parameter (execute cbfuncGeoSelectedVuk)
function runSparqlGeoSelectedVuk(rsgeo) {
    execSparqlGeoSelectedVuk(CODE_PREFIX + rsgeo, cbfuncGeoSelectedVuk);
}

//run sparql query with Incentive parameter (execute cbfuncIncentive in the end)
function runSparqlIncentive() {
	hideCharts();
	//uncheck independentParameter radio buttons
	$('input:radio[name=independentParameter]').prop('checked', false);
	
	$('body').css('cursor', 'wait');
	
	execSparqlIncentive(INCENTIVE_PREFIX + $('#incentive').val() + '>', cbfuncIncentive);
}

//run sparql query with Year parameter (execute cbfuncYear in the end)
function runSparqlYear() {
	hideCharts();
	//uncheck independentParameter radio buttons
	$('input:radio[name=independentParameter]').prop('checked', false);
	
	$('body').css('cursor', 'wait');
	
	execSparqlYear(YEAR_PREFIX + $('#year').val() + '>', cbfuncYear);
}



function getSelectedRsgeoData(e) {
	
	var layer = e.target;
	
	//run appropriate sparql query
	rsgeoSelected = layer.feature.properties.NSTJ_CODE; // save rsgeo selection
	
//	runSparqlRsgeo(rsgeoSelected);
        runSparqlGeoSelectedVuk(rsgeoSelected);
}

function populateGeoLevelsLists() {
	
	//Find top geo level
	execSparqlTopGeoBroaderNarrower(cbfuncGetGeoCodes);//sinchronous call
	
	//If previous sparql query returned empty fetch all geo codes.
	if (geoLevels.length === 0) {
		execSparqlAllGeoCodes(cbfuncGetGeoCodes);//sinchronous call
	}
	
	var i = 0;
	while (geoLevels.length > i) {
		execSparqlBroaderNarrowerForArray(CODE_PREFIX, geoLevels[i], cbfuncGetGeoCodes);//sinchronous call
		i++;
	}
	
}

function estamainInitVuk(){
    analysisType = sessionStorage.getItem("analysistype");
    
    cbfuncForGeoMap = function(data) {
        hashCodeToObservationValues = new Object();
        $(data.results.bindings).each(function(key, val){
            var rsgeoUri  = val.rsgeo.value;
            var code = rsgeoUri.substring(CODE_PREFIX.length, rsgeoUri.length);
            var value = val.observation.value;
            hashCodeToObservationValues[code] = value;//fill the hash with the keys and values
        });
        loadGeoNamesAndValues(); // or not
        $('body').css('cursor', 'default');
    
        //refresh map and chart
	info.update();//to display initial info when mouse is out of the map
        refreshMap();
        
        // TODO see wheter to keep this part
        if ((analysisType === 'bartime') && firstRun) {//if it is the first run and bartime analysis do not show this chart (another one will be displayed)
            firstRun = false;
        } else {
            refreshSpaceChart();
        }
    };


cbfuncYearIncentive = function(data) {
	
//								minAreaObservationValue = 0;
//								maxAreaObservationValue = 0;
//								minRegionObservationValue = 0;
//								maxRegionObservationValue = 0;
//								minCountryObservationValue = 0;
//								maxCountryObservationValue = 0;
	
	hashCodeToObservationValues = new Object();
//								regionNames.length = 0;
//								areaNames.length = 0;
//								countryNames.length = 0;
//								regionValues.length = 0;
//								areaValues.length = 0;
//								countryValues.length = 0;
	
	$(data.results.bindings).each(function(key, val){
		var rsgeoUri  = val.rsgeo.value;
		var code = rsgeoUri.substring(CODE_PREFIX.length, rsgeoUri.length);
		var value = val.observation.value;
		
								//find new min and max observation values
//								var intValue = parseInt(value);
//								if (code.length === CODE_LENGTH_AREA) {
//									if (maxAreaObservationValue === 0 || intValue > maxAreaObservationValue) {
//										maxAreaObservationValue = intValue;
//									} 
//									if (minAreaObservationValue === 0 || intValue < minAreaObservationValue) {
//										minAreaObservationValue = intValue;
//									}
//									if (hashCodeToObservationValues[code] == null) {
//										areaNames.push(hashCodeToNames[code]);//find area names
//										areaValues.push(intValue);
//									}
//									
//								} else if (code.length === CODE_LENGTH_REGION) {
//									if (maxRegionObservationValue === 0 || intValue > maxRegionObservationValue) {
//										maxRegionObservationValue = intValue;
//									} 
//									if (minRegionObservationValue === 0 || intValue < minRegionObservationValue) {
//										minRegionObservationValue = intValue;
//									}
//									if (hashCodeToObservationValues[code] == null) {
//										regionNames.push(hashCodeToNames[code]);//find region names
//										regionValues.push(intValue);
//									}
//								} else if (code.length === CODE_LENGTH_COUNTRY) {
//									if (maxCountryObservationValue === 0 || intValue > maxCountryObservationValue) {
//										maxCountryObservationValue = intValue;
//									} 
//									if (minCountryObservationValue === 0 || intValue < minCountryObservationValue) {
//										minCountryObservationValue = intValue;
//									}
//									if (hashCodeToObservationValues[code] == null) {
//										countryNames.push(hashCodeToNames[code]);//find country names
//										countryValues.push(intValue);
//									}
//								}
		
		hashCodeToObservationValues[code] = value;//fill the hash with the keys and values
		
	});

	loadGeoNamesAndValues();
//								//Sort the area values
//								sortArrays(areaValues, areaNames, false);
//								//Sort the region values
//								sortArrays(regionValues, regionNames, false);

    $('body').css('cursor', 'default');
    
    //refresh map and chart
	info.update();//to display initial info when mouse is out of the map
    refreshMap();
    if ((analysisType === 'bartime') && firstRun) {//if it is the first run and bartime analysis do not show this chart (another one will be displayed)
    	firstRun = false;
    } else {
    	refreshSpaceChart();
    }
};

cbfuncRsgeoIncentive = function(data) {
	//highstock chart
	hideCharts();
	
	var chartYears = [];
	var chartValues = [];
//	var chartData = [[Date.UTC(2009, 0),5],[Date.UTC(2010, 0),10],[Date.UTC(2011, 0),15],[Date.UTC(2012, 0),20]];
	var chartData = [];
	$(data.results.bindings).each(function(key, val){
		var timeUri = val.time.value;
		var year = timeUri.substring(YEAR_PREFIX.length - 1, timeUri.length);// -1, since prefix starts with '<'
		var value = val.observation.value;
		var intValue = parseInt(value);
		
		chartYears.push(year);
		chartValues.push(intValue);
	});
	
	sortArrays(chartYears, chartValues, true);//sort by years (ascending)

	for (var i = 0; i < chartYears.length; i++) {
		var chartItem = [];
		chartItem.push(Date.UTC(chartYears[i], 0));
		chartItem.push(chartValues[i]);
		
		chartData.push(chartItem);
	}
	
	var displayIncentiveName = hashIncentiveNames[$('#incentive').val()];
	var displayRsgeo = hashCodeToNames[rsgeoSelected];//incentive aim displayed in the info and charts
	var geo = 'Area: ' + displayRsgeo;
	
	var subtitle = geo + ', ' + 'Incentive: ' + displayIncentiveName;
	
	$('body').css('cursor', 'default');
	
	createTimeChart('highchartsbarsingle', chartData, 'Regional development incentives', subtitle, 'Incentives', 'column', 'Y');
};

cbfuncRsgeoYear = function(data) {
	hideCharts();
	
	var chartBarCategories = [];
	var chartBarValues = [];
	
	$(data.results.bindings).each(function(key, val){
		var incentiveUri = val.incentive.value;
		var incentiveCode = incentiveUri.substring(INCENTIVE_PREFIX.length - 1, incentiveUri.length);
		var value = val.observation.value;
		var intValue = parseInt(value);
		
		chartBarCategories.push(hashIncentiveNames[incentiveCode]);//find names
		chartBarValues.push(intValue);
	});

	var displayYear = $('#year').val();//year displayed in the info and charts
	var displayRsgeo = hashCodeToNames[rsgeoSelected];//incentive aim displayed in the info and charts
	var geo = 'Area: ' + displayRsgeo;
	
	var subtitle = 'Year: ' + displayYear + ', ' + geo;
	
	sortArrays(chartBarValues, chartBarCategories, false);
	$('body').css('cursor', 'default');
	
	createChartBarSingle(subtitle, chartBarCategories, chartBarValues, 'Incentives');
};

cbfuncRsgeo = function(data) {
	var arrayYearIncentive = new Array(YEARS.length);
	for (var i = 0; i < YEARS.length; i++) {
		var size = INCENTIVE_NAMES[0].length;
		arrayYearIncentive[i] = new Array(size);
		for (var j = 0; j < size; j++) {
			arrayYearIncentive[i][j] = 0;
		}
	}
	
	$(data.results.bindings).each(function(key, val){
		var timeUri = val.time.value;
		var incentiveUri = val.incentive.value;
		var value = val.observation.value;
		
		var year = timeUri.substring(YEAR_PREFIX.length - 1, timeUri.length);// -1, since prefix starts with '<'
		var incentiveCode = incentiveUri.substring(INCENTIVE_PREFIX.length - 1, incentiveUri.length);
		
		var indexI = findIndex(YEARS, year);
		var indexJ = findIndex(INCENTIVE_NAMES[0], incentiveCode);
		arrayYearIncentive[indexI][indexJ] = value;
		
	});
//	for (var i = 0; i < data.length; i++) {
//		var timeUri = data[i].time.uri;
//		var incentiveUri = data[i].incentive.uri;
//		var value = data[i].observation;
//		
//		var year = timeUri.substring(YEAR_PREFIX.length - 1, timeUri.length);// -1, since prefix starts with '<'
//		var incentiveCode = incentiveUri.substring(INCENTIVE_PREFIX.length - 1, incentiveUri.length);
//		
//		var indexI = findIndex(YEARS, year);
//		var indexJ = findIndex(INCENTIVE_NAMES[0], incentiveCode);
//		arrayYearIncentive[indexI][indexJ] = value;
//	}

    $('body').css('cursor', 'default');
    var chartSubtitle = 'Area: ' + hashCodeToNames[rsgeoSelected];
    
    createChartBarMultiple(chartSubtitle, arrayYearIncentive, INCENTIVE_NAMES[1], YEARS);
	
//    loading = false;
};

cbfuncGeoSelectedVuk = function(data) {
    var arrayYearIncentive = new Array(YEARS.length);
    for (var i = 0; i < YEARS.length; i++) {
            var size = INCENTIVE_NAMES[0].length;
            arrayYearIncentive[i] = new Array(size);
            for (var j = 0; j < size; j++) {
                    arrayYearIncentive[i][j] = 0;
            }
    }
    
    var doubleArray = new Array();
    var prevTime = '';
    var prevIncentive = '';
    var singleArray = new Array();
    
    var dim1Array = new Array(); // series 
    var dim2Array = new Array(); // categories
    var firstPass = true;

    $(data.results.bindings).each(function(key, val){
            var timeUri = val.dim1.value;
            var incentiveUri = val.dim2.value;
            var value = val.observation.value;

            var year = timeUri.substring(YEAR_PREFIX.length - 1, timeUri.length);// -1, since prefix starts with '<'
            var incentiveCode = incentiveUri.substring(INCENTIVE_PREFIX.length - 1, incentiveUri.length);
            
            year += 'v';
            incentiveCode += 'v';
            
            if (timeUri != prevTime){
                doubleArray.push(singleArray);
                singleArray = new Array();
                dim1Array.push(year);
                firstPass = false;
            }
            if (incentiveUri != prevIncentive){
                singleArray.push(value);
                if (firstPass){
                    dim2Array.push(incentiveCode);
                }
            }
            singleArray.push(value);
            prevTime = timeUri;
            prevIncentive = incentiveUri;

//            var indexI = findIndex(YEARS, year);
//            var indexJ = findIndex(INCENTIVE_NAMES[0], incentiveCode);
//            arrayYearIncentive[indexI][indexJ] = value;

    });
    doubleArray.push(singleArray);
    
    $('body').css('cursor', 'default');
    var chartSubtitle = 'Area: ' + hashCodeToNames[rsgeoSelected];
    
    createChartBarMultiple(chartSubtitle+'v', doubleArray, dim2Array, dim1Array);
};

cbfuncIncentive = function(data) {
//												var requiredCodeLength = 0;
	var arrayNames = geoLayerNames;
	var selectedGeoLevelCodes = geoLevels[visibleGeoLevel];
	
//												if (visibleLayer === 'Areas') {
//													requiredCodeLength = CODE_LENGTH_AREA;
//													arrayNames = areaNames;
//												} else if (visibleLayer === 'Regions') {
//													requiredCodeLength = CODE_LENGTH_REGION;
//													arrayNames = regionNames;
//												} else if (visibleLayer === 'Country') {
//													requiredCodeLength = CODE_LENGTH_COUNTRY;
//													arrayNames = countryNames;
//												}
	
	var arrayYearRsgeo = new Array(YEARS.length);//create twodim. array and initialize with 0
	for (var i = 0; i < YEARS.length; i++) {
		arrayYearRsgeo[i] = new Array(arrayNames.length);
		for (var j = 0; j < arrayNames.length; j++) {
			arrayYearRsgeo[i][j] = 0;
		}
	}
	
	$(data.results.bindings).each(function(key, val){
		var timeUri = val.time.value;
		var rsgeoUri = val.rsgeo.value;
		var value = val.observation.value;
		
		var year = timeUri.substring(YEAR_PREFIX.length - 1, timeUri.length);// -1, since prefix starts with '<'
		var rsgeoCode = rsgeoUri.substring(CODE_PREFIX.length, rsgeoUri.length);

//													if (rsgeoCode.length === requiredCodeLength) {
//														var indexI = findIndex(YEARS, year);
//														var name = hashCodeToNames[rsgeoCode];
//														var indexJ = findIndex(arrayNames, name);
//														arrayYearRsgeo[indexI][indexJ] = value;
//													}
		
		
		if ($.inArray(rsgeoCode, selectedGeoLevelCodes) ) {
			var indexI = findIndex(YEARS, year);
			var name = hashCodeToNames[rsgeoCode];
			var indexJ = findIndex(arrayNames, name);
			arrayYearRsgeo[indexI][indexJ] = value;
		}
		
	});

    $('body').css('cursor', 'default');
    
    var displayIncentiveName = hashIncentiveNames[$('#incentive').val()];
    createChartBarMultiple('Incentive: ' + displayIncentiveName, arrayYearRsgeo, arrayNames, YEARS);
	
};

cbfuncYear = function(data) {
//													var requiredCodeLength = 0;
	var arrayNames = geoLayerNames;
	var selectedGeoLevelCodes = geoLevels[visibleGeoLevel];
	
//													if (visibleLayer === 'Areas') {
//														requiredCodeLength = CODE_LENGTH_AREA;
//														arrayNames = areaNames;
//													} else if (visibleLayer === 'Regions') {
//														requiredCodeLength = CODE_LENGTH_REGION;
//														arrayNames = regionNames;
//													} else if (visibleLayer === 'Country') {
//														requiredCodeLength = CODE_LENGTH_COUNTRY;
//														arrayNames = countryNames;
//													}
	
	var arrayIncentiveRsgeo = new Array(INCENTIVE_NAMES[0].length);//create twodim. array and initialize with 0
	for (var i = 0; i < INCENTIVE_NAMES[0].length; i++) {
		arrayIncentiveRsgeo[i] = new Array(arrayNames.length);
		for (var j = 0; j < arrayNames.length; j++) {
			arrayIncentiveRsgeo[i][j] = 0;
		}
	}
	
	$(data.results.bindings).each(function(key, val){
		var incentiveUri = val.incentive.value;
		var rsgeoUri = val.rsgeo.value;
		var value = val.observation.value;
		
		var incentiveCode = incentiveUri.substring(INCENTIVE_PREFIX.length - 1, incentiveUri.length);// -1, since prefix starts with '<'
		var rsgeoCode = rsgeoUri.substring(CODE_PREFIX.length, rsgeoUri.length);

//													if (rsgeoCode.length === requiredCodeLength) {
//														var indexI = findIndex(INCENTIVE_NAMES[0], incentiveCode);
//														var name = hashCodeToNames[rsgeoCode];
//														var indexJ = findIndex(arrayNames, name);
//														arrayIncentiveRsgeo[indexI][indexJ] = value;
//													}
		
		if ($.inArray(rsgeoCode, selectedGeoLevelCodes) ) {
			var indexI = findIndex(INCENTIVE_NAMES[0], incentiveCode);
			var name = hashCodeToNames[rsgeoCode];
			var indexJ = findIndex(arrayNames, name);
			arrayIncentiveRsgeo[indexI][indexJ] = value;
		}
		
	});

    $('body').css('cursor', 'default');
    
    var displayYear = $('#year').val();
    createChartBarMultiple('Year: ' + displayYear, arrayIncentiveRsgeo, arrayNames, INCENTIVE_NAMES[1]);
	
};

cbfuncGetGeoCodes = function(data) {
	var geoLevelArray = [];
	
	$(data.results.bindings).each(function(key, val){
		var rsgeoUri  = val.rsgeo.value;
		var code = rsgeoUri.substring(CODE_PREFIX.length, rsgeoUri.length);
		geoLevelArray.push(code);

	});

	if (geoLevelArray.length > 0) {
		geoLevels.push(geoLevelArray);
	}
	
};

    domReadyVuk();


}