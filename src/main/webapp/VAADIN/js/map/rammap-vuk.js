
var map;//leaflet map object


//FIND STYLE (COLOR) BASED ON THE OBSERVATION VALUE
var WHITE_COLOR = 'rgb(255,255,255)';
var colorGradeValues = [0, 1000000, 2000000, 3000000, 4000000, 5000000, 6000000, 7000000, 8000000];
var COLORS = ['rgb(247,251,255)', 'rgb(222,235,247)', 'rgb(198,219,239)', 'rgb(158,202,225)', 'rgb(107,174,214)',
            'rgb(66,146,198)', 'rgb(33,113,181)', 'rgb(8,81,156)', 'rgb(8,48,107)'];

//recalculate color grade values based on the min and max observation values
function recalculateColorGradeValuesOld(minObservationValue, maxObservationValue) {
	var range = maxObservationValue - minObservationValue;
	var step = range / colorGradeValues.length;
        var divisor = 100000;
	var roundStep = Math.round(step / divisor) * divisor;
	while (roundStep === 0 && divisor > 1) {
                divisor /= 10;
		roundStep = Math.round(step / divisor) * divisor;
	}
        if (roundStep === 0) {
            roundStep = 1;
        }
	
	//find the largest multiple of roundStep which is lower than minObservationValue
	var firstRangeValue = 0;
	while (firstRangeValue + roundStep < minObservationValue) {
		firstRangeValue += roundStep;
	}
	
	//create new colorGradeValues elements
	for (var i = 0; i < colorGradeValues.length; i++) {
		colorGradeValues[i] = firstRangeValue + i * roundStep;
	}
}

function recalculateColorGradeValues(minObservationValue, maxObservationValue) {
	var range = maxObservationValue - minObservationValue;
	var step = range / colorGradeValues.length;
        var divisor = 100000;
	var roundStep = Math.floor(step / divisor) * divisor;
	while (roundStep === 0 && divisor > 1) {
                divisor /= 10;
		roundStep = Math.floor(step / divisor) * divisor;
	}
        if (roundStep === 0) {
            roundStep = 1;
        } else {
            var subDivisor = divisor / 10;
            var subStep = Math.floor(step-roundStep);
            roundStep += Math.floor(subStep / subDivisor) * subDivisor;
        }
	
	//find the largest multiple of roundStep which is lower than minObservationValue
	var firstRangeValue = 0;
	while (firstRangeValue + roundStep < minObservationValue) {
		firstRangeValue += roundStep;
	}
	
	//create new colorGradeValues elements
	for (var i = 0; i < colorGradeValues.length; i++) {
		colorGradeValues[i] = firstRangeValue + i * roundStep;
	}
}

function recalculateColorGradeValuesQuarters(minObservationValue, maxObservationValue) {
    var range = maxObservationValue - minObservationValue;
    var step = range / colorGradeValues.length;
    var divisor = 100000;
    var roundStep = Math.floor(step / divisor) * divisor;
    var i=0;
    while (roundStep === 0 && divisor > 1) {
        i++;
        if (i % 3 === 0) divisor /= 2.5;
        else divisor /= 2;
        roundStep = Math.round(step / divisor) * divisor;
    }
    if (roundStep === 0) {
        roundStep = 1;
    }
	
    //find the largest multiple of roundStep which is lower than minObservationValue
    var firstRangeValue = 0;
    while (firstRangeValue + roundStep < minObservationValue) {
        firstRangeValue += roundStep;
    }

    //create new colorGradeValues elements
    for (var i = 0; i < colorGradeValues.length; i++) {
        colorGradeValues[i] = firstRangeValue + i * roundStep;
    }
}

function recalculateColorGradeValuesSimple(minObservationValue, maxObservationValue) {
    var range = maxObservationValue - minObservationValue;
    var step = Math.floor(range / colorGradeValues.length);
    if (step === 0 ) step = 1;
    //find the largest multiple of roundStep which is lower than minObservationValue
    var firstRangeValue = 0;
    while (firstRangeValue + step < minObservationValue) {
        firstRangeValue += step;
    }

    //create new colorGradeValues elements
    for (var i = 0; i < colorGradeValues.length; i++) {
        colorGradeValues[i] = firstRangeValue + i * step;
    }
}

//discover the range that the value belongs to (see colorGradeValues)
function getRangeIndex(value) {
	for (var i = colorGradeValues.length - 1; i >= 0; i--) {
		if (value > colorGradeValues[i]) {
			return i;
		}
	}
	return -1;
}

//get the color based on the range that the value belongs to
function getColorFromValue(value) {
	var index = getRangeIndex(value);
	if (index > -1) {
		return COLORS[index];
	}
	return WHITE_COLOR;// 0 = No data
}

function getStyleHash(feature, layer) {
	var color = getColorFromHash(feature.properties.URI);
//	if (visibleLayer === 'Municipalities') {
//		color = getColorFromHash(feature.properties.MAT_BR_OPS);
//	}
	return getStyle(color);
}

function getColorFromHash(uri) {
	var value = hashCodeToObservationValues[uri];
	if (value == null) {
		return WHITE_COLOR;
	}
	return getColorFromValue(value);
}

function getStyle(color) {
	return {
	    weight: 2,
	    color: '#999',
	    opacity: 1,
	    fillColor: color,
	    fillOpacity: 0.8
	};
}
		
var geojson;

function highlightFeature(e) {
  var layer = e.target;

  layer.setStyle({
      weight: 5,
      color: '#666',
      dashArray: '',
      fillOpacity: 0.7
  });
  console.log(layer);
  var regionURI = layer.feature.properties.URI;
  var lastSlash = regionURI.lastIndexOf("/");
  if (lastSlash === regionURI.length-1) {
      lastSlash = regionURI.substring(0, lastSlash-1).lastIndexOf("/");
  }
  var lastHash = regionURI.lastIndexOf("#");
  if (lastHash === regionURI.length-1) {
      lastHash = regionURI.substring(0, lastHash-1).lastIndexOf("/");
  }
  var startIndex = 0;
  if (lastSlash > startIndex) startIndex = lastSlash+1;
  if (lastHash > startIndex) startIndex = lastHash+1;
  var information = "Region: " + regionURI.substring(startIndex) + "<br>Value: " + addThousandsSeparators(hashCodeToObservationValues[regionURI]);
  $('#esta-map-popup p').html(information);
  $('#esta-map-popup').show();

  if (!L.Browser.ie && !L.Browser.opera) {
      layer.bringToFront();
  }
  
  info.update(layer.feature.properties);
}

function resetHighlight(e) {
    var layer = e.target;
  geojson.resetStyle(e.target);
  $('#esta-map-popup').hide();
  
  info.update();
}

function zoomToFeature(e) {
  map.fitBounds(e.target.getBounds());
}


		
//ADD INFO WINDOW TO MAP
var info;


//add thousands separators to the value number
function addThousandsSeparators(value) {
    if (typeof value === 'undefined') return 'undefined';
	if (value != null) {
		var parts = value.toString().split('.');
	    parts[0] = parts[0].replace(/\B(?=(\d{3})+(?!\d))/g, ',');
	    return parts.join('.');
//		return value.split('').reverse().join('')
//	    .replace(/(\d{3}\B)/g, '$1,')
//	    .split('').reverse().join('');
	}
	return null;
}


function onEachFeature(feature, layer) {
//DEFINE POPUP CONTENT
	var popupContent = '';
	
	if (feature.properties && feature.properties.NAME) {
		popupContent += '<p>Area: ' + feature.properties.NAME + '</p>';
	}

	if (feature.properties && feature.properties.NSTJ_CODE) {
		popupContent += '<p>NSTJ code: ' + feature.properties.NSTJ_CODE + '</p>';
	}
        
        if (feature.properties && feature.properties.URI) {
		popupContent += '<p>URI: ' + feature.properties.URI + '</p>';
	}

	if (feature.properties && feature.properties.AREA) {
		popupContent += '<p>Area: ' + addThousandsSeparators(feature.properties.AREA) + '</p>';
	}

	if (feature.properties && feature.properties.PERIMETER) {
		popupContent += '<p>Perimeter: ' + addThousandsSeparators(feature.properties.PERIMETER) + '</p>';
	}
	
	if (feature.properties && isObservationValueDefined(feature.properties.URI)) {
		popupContent += '<p>Value: ' + addThousandsSeparators(hashCodeToObservationValues[feature.properties.URI]) + '</p>';
	}

	if (feature.properties && !isObservationValueDefined(feature.properties.URI)) {
		popupContent += '<p>Value: No data</p>';
	}
	
	
																						//	if (visibleLayer === 'Country') {
																						//		if (feature.properties && feature.properties.NAME) {
																						//			popupContent += '<p>Area: ' + feature.properties.NAME + '</p>';
																						//		}
																						//	
																						//		if (feature.properties && feature.properties.NSTJ_CODE) {
																						//			popupContent += '<p>NSTJ code: ' + feature.properties.NSTJ_CODE + '</p>';
																						//		}
																						//	
																						//		if (feature.properties && feature.properties.AREA) {
																						//			popupContent += '<p>Area: ' + addThousandsSeparators(feature.properties.AREA) + '</p>';
																						//		}
																						//	
																						//		if (feature.properties && feature.properties.PERIMETER) {
																						//			popupContent += '<p>Perimeter: ' + addThousandsSeparators(feature.properties.PERIMETER) + '</p>';
																						//		}
																						//		
																						//		if (feature.properties && isObservationValueDefined(feature.properties.NSTJ_CODE)) {
																						//			popupContent += '<p>Value: ' + addThousandsSeparators(hashCodeToObservationValues[feature.properties.URI]) + '</p>';
																						//		}
																						//	
																						//		if (feature.properties && !isObservationValueDefined(feature.properties.NSTJ_CODE)) {
																						//			popupContent += '<p>Value: No data</p>';
																						//		}
																						//		
																						//	} else if (visibleLayer === 'Areas') {
																						//		if (feature.properties && feature.properties.NAME) {
																						//			popupContent += '<p>Area: ' + feature.properties.NAME + '</p>';
																						//		}
																						//	
																						////		if (feature.properties && feature.properties.BROJ_OPSTI) {
																						////			popupContent += '<p>No. of municipalities: ' + feature.properties.BROJ_OPSTI + '</p>';
																						////		}
																						////	
																						////		if (feature.properties && feature.properties.SIFRA_OKRU) {
																						////			popupContent += '<p>Area code: ' + feature.properties.SIFRA_OKRU + '</p>';
																						////		}
																						////	
																						//		if (feature.properties && feature.properties.NSTJ_CODE) {
																						//			popupContent += '<p>NSTJ code: ' + feature.properties.NSTJ_CODE + '</p>';
																						//		}
																						//	
																						//		if (feature.properties && feature.properties.AREA) {
																						//			popupContent += '<p>Area: ' + addThousandsSeparators(feature.properties.AREA) + '</p>';
																						//		}
																						//	
																						//		if (feature.properties && feature.properties.PERIMETER) {
																						//			popupContent += '<p>Perimeter: ' + addThousandsSeparators(feature.properties.PERIMETER) + '</p>';
																						//		}
																						//		
																						//		if (feature.properties && isObservationValueDefined(feature.properties.NSTJ_CODE)) {
																						//			popupContent += '<p>Value: ' + addThousandsSeparators(hashCodeToObservationValues[feature.properties.URI]) + '</p>';
																						//		}
																						//	
																						//		if (feature.properties && !isObservationValueDefined(feature.properties.NSTJ_CODE)) {
																						//			popupContent += '<p>Value: No data</p>';
																						//		}
																						//		
																						//	} else if (visibleLayer === 'Regions') {
																						//		if (feature.properties && feature.properties.NAME) {
																						//			popupContent += '<p>Region: ' + feature.properties.NAME + '</p>';
																						//		}
																						//
																						//		if (feature.properties && feature.properties.NSTJ_CODE) {
																						//			popupContent += '<p>NSTJ code: ' + feature.properties.NSTJ_CODE + '</p>';
																						//		}
																						//		
																						//		if (feature.properties && isObservationValueDefined(feature.properties.NSTJ_CODE)) {
																						//			popupContent += '<p>Value: ' + addThousandsSeparators(hashCodeToObservationValues[feature.properties.URI]) + '</p>';
																						//		}
																						//
																						//		if (feature.properties && !isObservationValueDefined(feature.properties.NSTJ_CODE)) {
																						//			popupContent += '<p>Value: No data</p>';
																						//		}
																						//		
																						//	} else if (visibleLayer === 'Municipalities') {
																						//		if (feature.properties && feature.properties.OPSTINA) {
																						//			popupContent += '<p>Municipality: ' + feature.properties.OPSTINA + '</p>';
																						//		}
																						//
																						//		if (feature.properties && feature.properties.NSTJ_CODE_OKRUG) {
																						//			popupContent += '<p>NSTJ code: ' + feature.properties.NSTJ_CODE_OKRUG + '</p>';
																						//		}
																						//
																						//		if (feature.properties && feature.properties.OKRUG) {
																						//			popupContent += '<p>Area: ' + feature.properties.OKRUG + '</p>';
																						//		}
																						//
																						//		if (feature.properties && feature.properties.REGION) {
																						//			popupContent += '<p>Region: ' + feature.properties.REGION + '</p>';
																						//		}
																						//
																						//		if (feature.properties && feature.properties.NSTJ_CODE_REGION) {
																						//			popupContent += '<p>NSTJ region code: ' + feature.properties.NSTJ_CODE_REGION + '</p>';
																						//		}
																						//		
																						//		if (feature.properties && feature.properties.MAT_BR_OPS) {
																						//			popupContent += '<p>Municipality id no.: ' + feature.properties.MAT_BR_OPS + '</p>';
																						//		}
																						//
																						//		if (feature.properties && feature.properties.SIF_OKRUGA) {
																						//			popupContent += '<p>Area code: ' + feature.properties.SIF_OKRUGA + '</p>';
																						//		}
																						//
																						//		if (feature.properties && feature.properties.AREA) {
																						//			popupContent += '<p>Area: ' + addThousandsSeparators(feature.properties.AREA) + '</p>';
																						//		}
																						//
																						//		if (feature.properties && feature.properties.PERIMETER) {
																						//			popupContent += '<p>Perimeter: ' + addThousandsSeparators(feature.properties.PERIMETER) + '</p>';
																						//		}
																						//		
																						//		if (feature.properties && isObservationValueDefined(feature.properties.MAT_BR_OPS)) {
																						//			popupContent += '<p>Value: ' + addThousandsSeparators(hashCodeToObservationValues[feature.properties.MAT_BR_OPS]) + '</p>';
																						//		}
																						//
																						//		if (feature.properties && !isObservationValueDefined(feature.properties.MAT_BR_OPS)) {
																						//			popupContent += '<p>Value: No data</p>';
																						//		}
																						//		
																						//	}

	loadHashCodeToNames(feature.properties);
	
	layer.bindPopup(popupContent);
	
//DEFINE LAYER ONMOUSEOVER, ONMOUSEOUT
	layer.on({
      mouseover: highlightFeature,
      mouseout: resetHighlight,
      //click: zoomToFeature
      click:getSelectedRsgeoData
  });
}




		
//ADD LEGEND TO MAP
var legend;





function redrawMap(data, minObservationValue, maxObservationValue, updateStyle) {
//    console.log('redrawMap data:');
//    console.log(data);
    var start = new Date().getTime();
    if (updateStyle && javaAggregatedColoring)
        recalculateColorGradeValues(minObservationValueAggregated, maxObservationValueAggregated);
    else
        recalculateColorGradeValues(minObservationValue, maxObservationValue);
    var wp1 = new Date().getTime();
//    console.log('Recalc color grades: ' + (wp1-start));
    
    map.removeControl(legend);
    legend.addTo(map);
    var wp2 = new Date().getTime();
//    console.log('Remove and add legend: ' + (wp2-wp1));

//    map.removeLayer(geojson);
    if (updateStyle) {
        geojson.setStyle(getStyleHash);
    }
    else {
        geojson.clearLayers();
        // TODO: should check if data contains layers and add only if there are some
        geojson.addData(data);
    } 

//    geojson = L.geoJson(data, {
//        style: getStyleHash,
//        onEachFeature: onEachFeature,
//    }).addTo(map);
    var wp3 = new Date().getTime();
//    console.log('Remove and add geojson: ' + (wp3-wp2));
}

function rammapInitVuk() {
    if (map !== undefined) map.remove();
    map = L.map('map').setView([43.7, 21.5], 7);//leaflet map object

//ADD LAYER TO MAP
//for (var i = 0; i < geoData.length; i++) {
//	var geojsonTemp = L.geoJson(geoData[i], {//preload names
//		style: getStyleHash,
//		onEachFeature: onEachFeature,
//	});
//}

geojson = L.geoJson(countryData, {//areas
	style: getStyleHash,
	onEachFeature: onEachFeature,
}).addTo(map);

//LEAFLET MAP
L.tileLayer('http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
}).addTo(map);

//ADD INFO WINDOW TO MAP
info = L.control();
info.onAdd = function (map) {
  this._div = L.DomUtil.create('div', 'info'); // create a div with a class 'info'
  this.update();
  return this._div;
};

//update the info control based on feature properties passed
info.update = function (props) {
	var displayYear = $('#year').val();//year displayed in the info and charts
	var displayIncentiveName = hashIncentiveNames[$('#incentive').val()];//incentive aim displayed in the info and charts
	
	if (displayYear != null && displayIncentiveName != null) {
		if (props != null) {
			var areaRegionName = props.NAME;
			var uri = props.URI;
														//			if (visibleLayer === 'Municipalities') {
														//				nstjCode = props.MAT_BR_OPS;
														//				areaRegionName = 'Municipalities: ' + props.OPSTINA;
														//			}
			
			var valueFromHash = hashCodeToObservationValues[uri];
			var value = 'No data';
			if (valueFromHash != null) {
				value = addThousandsSeparators(valueFromHash);
			}
		    this._div.innerHTML = '<h4>Regional development incentives</h4>' +
	//			'<h4>Year: ' + displayYear + ', Incentive aim: ' + displayIncentiveName + '</h4>' +
//				'<h4>Year: ' + displayYear + ', Incentive aim: ' + 
				'<h4>Year: <a href="javascript:void(0)" onclick="runSparqlYear();">' + displayYear + '</a> ' +
				'Incentive aim: <a href="javascript:void(0)" onclick="runSparqlIncentive();">' + displayIncentiveName + '</a></h4>' +
		        '<b>' + areaRegionName + '</b><br />' + 
		        'URI: ' + uri + '<br />' + 
		        'Value: ' + value;
		} else {
			var valueRS = 'No data';
			if (hashCodeToObservationValues['http://elpo.stat.gov.rs/lod2/RS-DIC/geo/RS'] != null) {
				valueRS = addThousandsSeparators(hashCodeToObservationValues['http://elpo.stat.gov.rs/lod2/RS-DIC/geo/RS']);
			}
			
			this._div.innerHTML = '<h4>Regional development incentives</h4>' +
								'<h4>Year: <a href="javascript:void(0)" onclick="runSparqlYear();">' + displayYear + '</a> ' +
								'Incentive aim: <a href="javascript:void(0)" onclick="runSparqlIncentive();">' + displayIncentiveName + '</a></h4>' +
								'<b>' + 'Area: Serbia </b><br />' + 
							    'NSTJ code: RS <br />' +  
							    'Value: ' + valueRS;
		}
	}
};

info.addTo(map);

//ADD LEGEND TO MAP
legend = L.control({position: 'bottomright'});

legend.onAdd = function (map) {

    var div = L.DomUtil.create('div', 'info legend');

    // loop through our density intervals and generate a label with a colored square for each interval
    for (var i = 0; i < colorGradeValues.length; i++) {
        div.innerHTML +=
            '<i style="background:' + getColorFromValue(colorGradeValues[i] + 1) + '"></i> ' +
            addThousandsSeparators(colorGradeValues[i]) + (colorGradeValues[i + 1] ? '&ndash;' +
    		addThousandsSeparators(colorGradeValues[i + 1]) + '<br>' : '+');
    }

    return div;
};

legend.addTo(map);


}
