function getItemToIncrease(targetArray, item){
    var ret = null;
    $(targetArray).each(function(i, v){
        if (v.rsgeo.value === item.rsgeo.value) {
            ret = v;
            return false;
        }
    });
    return ret;
}

var redrawCount = 0;

var timeChartMin = 0;
var timeChartMax = 0;

function calculateAggreg() {
    
}

/*
 * Creates Highstock chart
 * @param {String} containerName 
 * @param {Array} chartData - Array of [<milliseconds since Jan 1st 1970>, value]
 * @return {String} titleText
 * @return {String} subtitleText
 * @return {String} seriesName
 * @return {String} chartType - 'line' or 'column'
 * @return {String} granularity - for label formatting - 'Y' or 'M' or 'D'
 */
function createTimeChart(containerName, chartData, titleText, subtitleText, seriesName, chartType, granularity) {
	var labelFormatM = '%Y';
	var labelFormatW = '%Y';
	var labelFormatD = '%Y';
	if (granularity === 'M') {
		labelFormatM = '%Y-%m';
		labelFormatW = '%Y-%m';
		labelFormatD = '%Y-%m';
	} else if (granularity === 'D') {
		labelFormatM = '%Y-%m';
		labelFormatW = '%Y<br/>%m-%d';
		labelFormatD = '%Y<br/>%m-%d';
	}
	
	chart = new Highcharts.StockChart({
	    chart: {
	    	renderTo : containerName,
	        alignTicks: false,
                zoomType: 'x',
                events: {
                    redraw: function(event) {
                        var s = new Date().getTime();
                        var minInt = event.target.xAxis[0].min;
                        var maxInt = event.target.xAxis[0].max;
                        if (geoForMapAllTimesData.active){
                            var newData = {
                                results: {
                                    bindings: []
                                }
                            };
                            minObservationValueAggregated = 0;
                            maxObservationValueAggregated = 0;
                            var selectedGeoLevelCodes = geoLevels[visibleGeoLevel];
                            var timeSpan = maxInt - minInt;
                            var lastItemTime = geoForMapAllTimesData.dataAllTimes.results.bindings[geoForMapAllTimesData.dataAllTimes.results.bindings.length-1].parsedTime.millis;
                            var previousTime = -1;
                            var beforeTime = -1
                            $(geoForMapAllTimesData.dataAllTimes.results.bindings).each(function(index, item) {
                                var currentDate = Date.UTC(item.parsedTime.year, item.parsedTime.month);
                                if (currentDate !== beforeTime) {
                                    previousTime = beforeTime;
                                    beforeTime = currentDate;
                                }
                                if (currentDate >= minInt && currentDate <= maxInt) {
                                    // add item if it doesn't exist
                                    var itemToIncrease = getItemToIncrease(newData.results.bindings, item);
                                    if (itemToIncrease === null) {
                                        var itemAdd = $.extend(true, {}, item);
                                        itemAdd.rstime.value = "Aggregated";
                                        if (!itemAdd.observation.value) itemAdd.observation.value = 0;
                                        newData.results.bindings.push(itemAdd);
                                    } else { // add value if it does exist
                                        var sum = parseFloat(itemToIncrease.observation.value);
                                        if (item.observation.value && !isNaN(item.observation.value))
                                            sum += parseFloat(item.observation.value);
                                        itemToIncrease.observation.value = sum.toString();
                                    }
                                }
                                
                                ///////////////////////////////////////
                                // and now calculation of min and max
                                ///////////////////////////////////////
                                if (geoLevels.length > visibleGeoLevel) {
                                    var rsgeoUri = item.rsgeo.value;
//                                    var code = rsgeoUri.substring(CODE_PREFIX.length, rsgeoUri.length);
                                    if (!rsgeoUri) return true;

                                    // if this item/geo is not on the map go to next iteration
                                    if (selectedGeoLevelCodes.indexOf(rsgeoUri) < 0) return true; //!!! this shouldn't happen before all geo levels are added

                                    // update min/,ax
                                    // create new current and calc local min and max
                                
                                    var currentGeo = item.rsgeo.value;
                                    var currentSum = parseFloat(item.observation.value);
                                    if (currentSum === undefined || currentSum === null)
                                        currentSum = 0;
                                    var currentTime = item.parsedTime.millis;
                                    
                                    // if the previus time was in the the time span looking from behind
                                    // do not update min and max since this window is not possible
                                    if (lastItemTime - previousTime < timeSpan) 
                                        return true;
                                    
                                    $(geoForMapAllTimesData.dataAllTimes.results.bindings.slice(index+1)).each(function (j, timeItem) {
                                        // break if we are at the next geo
                                        if (timeItem.rsgeo.value !== currentGeo) return false;
                                        // break if the time is out of the window
                                        if (timeItem.parsedTime.millis - currentTime > timeSpan) return false;
                                        // if it is in the window increase the sum
                                        if (timeItem.observation.value){
                                            if (!isNaN(timeItem.observation.value))
                                                currentSum += parseFloat(timeItem.observation.value);
                                        }
                                    });
                                    // now I have the sum for this element, so update min and max
                                    if (maxObservationValueAggregated === 0 || currentSum > maxObservationValueAggregated)
                                        maxObservationValueAggregated = currentSum;
                                    if (minObservationValueAggregated === 0 || currentSum < minObservationValueAggregated)
                                        minObservationValueAggregated = currentSum;
                                }
                                
                            });
                            console.log('Min: ' + minObservationValueAggregated);
                            console.log('Max: ' + maxObservationValueAggregated);
                            if (geoForMapAllTimesData.cbFunction) geoForMapAllTimesData.cbFunction(newData, true);
                        }
                        var e = new Date().getTime();
                        redrawCount++;
//                        console.log('Redraw called: ' + redrawCount);
//                        console.log('Execution time: ' + (e - s));
                    }, 
                    selection: function(event) {
//                        console.log('Selection changed');
//                        console.log(event.xAxis[0]);
//                        console.log(event.yAxis[0]);
                    }
                }
	    },
	    tooltip: {
	        formatter: function() {
	            var date = new Date(this.x);
	            var year = date.getFullYear();
	            var month = '';
	            var day = '';
	            if (granularity === 'M') {
	            	month = ('0' + (date.getMonth() + 1)).slice(-2) + '/';
	            } else if (granularity === 'D') {
	            	day = ('0' + date.getDate()).slice(-2) + '/' ;
	            	month = ('0' + (date.getMonth() + 1)).slice(-2) + '/';
	            }
	            
	            var value = addThousandsSeparators(this.y);
	            return day + month + year + '<br/>' + '<span style="color:'+this.points[0].series.color+'">'+ this.points[0].series.name +'</span>: '+ value;
	         },
	    },
	    xAxis: {
	    	type: 'datetime',
                minRange: 7516800000, 
                events: {
//                    setExtremes: function(event){
//                        var s = new Date().getTime();
//                        var minInt = event.min;
//                        var maxInt = event.max;
//                        if (geoForMapAllTimesData.active){
//                            var newData = {
//                                results: {
//                                    bindings: []
//                                }
//                            };
//                            minObservationValueAggregated = 0;
//                            maxObservationValueAggregated = 0;
//                            var selectedGeoLevelCodes = geoLevels[visibleGeoLevel];
//                            var timeSpan = maxInt - minInt;
//                            $(geoForMapAllTimesData.dataAllTimes.results.bindings).each(function(index, item) {
//                                var currentDate = Date.UTC(item.parsedTime.year, item.parsedTime.month);
//                                if (currentDate >= minInt && currentDate <= maxInt) {
//                                    // add item if it doesn't exist
//                                    var itemToIncrease = getItemToIncrease(newData.results.bindings, item);
//                                    if (itemToIncrease === null) {
//                                        var itemAdd = $.extend(true, {}, item);
//                                        itemAdd.rstime.value = "Aggregated";
//                                        newData.results.bindings.push(itemAdd);
//                                    } else { // add value if it does exist
//                                        var sum = parseFloat(itemToIncrease.observation.value);
//                                        sum += parseFloat(item.observation.value);
//                                        itemToIncrease.observation.value = sum.toString();
//                                    }
//                                }
//                                
//                                ///////////////////////////////////////
//                                // and now calculation of min and max
//                                ///////////////////////////////////////
//                                if (geoLevels.length > visibleGeoLevel) {
//                                    var rsgeoUri = item.rsgeo.value;
//                                    var code = rsgeoUri.substring(CODE_PREFIX.length, rsgeoUri.length);
//                                    if (!code) return true;
//
//                                    // if this item/geo is not on the map go to next iteration
//                                    if (selectedGeoLevelCodes.indexOf(code) < 0) return true; //!!! this shouldn't happen before all geo levels are added
//
//                                    // update min/,ax
//                                    // create new current and calc local min and max
//                                
//                                    var currentGeo = item.rsgeo.value;
//                                    var currentSum = parseFloat(item.observation.value);
//                                    var currentTime = item.parsedTime.millis;
//                                    $(geoForMapAllTimesData.dataAllTimes.results.bindings.slice(index+1)).each(function (j, timeItem) {
//                                        // break if we are at the next geo
//                                        if (timeItem.rsgeo.value !== currentGeo) return false;
//                                        // break if the time is out of the window
//                                        if (timeItem.parsedTime.millis - currentTime > timeSpan) return false;
//                                        // if it is in the window increase the sum
//                                        if (!isNaN(timeItem.observation.value))
//                                            currentSum += parseFloat(timeItem.observation.value);
//                                    });
//                                    // now I have the sum for this element, so update min and max
//                                    if (maxObservationValueAggregated === 0 || currentSum > maxObservationValueAggregated)
//                                        maxObservationValueAggregated = currentSum;
//                                    if (minObservationValueAggregated === 0 || currentSum < minObservationValueAggregated)
//                                        minObservationValueAggregated = currentSum;
//                                }
//                                
//                            });
//                            console.log('Min: ' + minObservationValueAggregated);
//                            console.log('Max: ' + maxObservationValueAggregated);
//                            geoForMapAllTimesData.newData = newData;
//                            if (geoForMapAllTimesData.cbFunction) geoForMapAllTimesData.cbFunction(newData, true);
//                        }
//                        var e = new Date().getTime();
//                        redrawCount++;
////                        console.log('Redraw called: ' + redrawCount);
////                        console.log('Execution time: ' + (e - s));
//                    }, 
                    afterSetExtremes: function(event) {
                        //console.log('Extremes set: ' + event.min + '\t' + event.max);
                    }
                }, 
	    	dateTimeLabelFormats: {
	    		day : labelFormatD,
	    		week: labelFormatW,
	    		month: labelFormatM,
	            year: '%Y'
	            
//		            	second: '%Y-%m-%d<br/>%H:%M:%S',
//						minute: '%Y-%m-%d<br/>%H:%M',
//						hour: '%Y-%m-%d<br/>%H:%M',
//						day: '%Y<br/>%m-%d',
//						week: '%Y<br/>%m-%d',
//						month: '%Y-%m',
//						year: '%Y'
					
					
            }
	    },
	    rangeSelector: {
			inputEnabled: false,
	        selected: 5
	    },

	    title: {
	        text: titleText
	    },
	    subtitle: {
	    	text: subtitleText
	    },
	    series: [{
	        type: chartType,
	        name: seriesName,
	        data: chartData,
	        dataGrouping: {
				units: [[
				     	'day',
				    	[1]
					    ], [
						'week', // unit name
						[1] // allowed multiples
						], [
						'month',
						[1, 3, 6]
				]]
	        }
	    }]
	});
	
	chart.redraw();
	
	$('#' + containerName).show('slow', function() {
	    // Animation complete.
	});
}

function timechartInitVuk(){
    
}
