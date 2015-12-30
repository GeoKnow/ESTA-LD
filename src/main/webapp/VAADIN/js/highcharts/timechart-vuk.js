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
var timeChartFirstTime = undefined;
var timeChartLastTime = undefined;

function calculateAggreg() {
    
}

var lastNewData = {
    results: {
        bindings: []
    }
};
var lastMinIndex = -1;
var lastMaxIndex = -1;

function onTimeWindowChange(event) {
    var s = new Date().getTime();
    var minInt = event.target.xAxis[0].min;
    var maxInt = event.target.xAxis[0].max;
    // TODO figure out a better way to do this, breaks on dense charts when the user hits roght or left boundary
    if (javaAggregatedColoring && Math.abs((maxInt - minInt) - (timeChartMax - timeChartMin)) > 10) {
        clearAggregatedColoring();
    }
    timeChartMin =  minInt;
    timeChartMax = maxInt;
    if (geoForMapAllTimesData.active && geoForMapAllTimesData.dataAllTimes.results.bindings.length > 0) {
        var newData = {
            results: {
                bindings: []
            }
        };
        var selectedGeoLevelCodes = geoLevels[visibleGeoLevel];
        var timeSpan = maxInt - minInt;
        var lastItemTime = geoForMapAllTimesData.dataAllTimes.results.bindings[geoForMapAllTimesData.dataAllTimes.results.bindings.length - 1].parsedTime.millis;
        var previousTime = -1;
        var beforeTime = -1;
        var allBindings = geoForMapAllTimesData.dataAllTimes.results.bindings;
        var weird = (minInt > lastMaxIndex || maxInt < lastMinIndex);
        if (!geoForMapAllTimesData.firstPass && !weird) {
            newData = lastNewData;
            var newBindings = newData.results.bindings;
            $(newBindings).each(function(index, item) {
                var rsgeo = item.rsgeo.value;
                var sumToAdd = 0;
                var sumToSubtract = 0;
                var sumCurrent = parseFloat(item.observation.value);
                var i = item.minIndex-1;
                // go left of min and add any new values to the sum
                while (i>0 && allBindings[i].parsedTime.millis >= minInt && allBindings[i].rsgeo.value===rsgeo) {
                    var curBinding = allBindings[i];
                    item.minIndex = i;
                    if (curBinding.observation.value && !isNaN(curBinding.observation.value))
                        sumToAdd += parseFloat(curBinding.observation.value);
                    i--;
                }
                // go right of min and deduct values that dropped out
                i = item.minIndex;
                while (i<allBindings.length && allBindings[i].parsedTime.millis<minInt && allBindings[i].rsgeo.value===rsgeo) {
                    var curBinding = allBindings[i];
                    if (curBinding.observation.value && !isNaN(curBinding.observation.value))
                        sumToSubtract += parseFloat(curBinding.observation.value);
                    i++;
                }
                if (i<allBindings.length && allBindings[i].rsgeo.value === rsgeo)
                    item.minIndex = i;
                else 
                    item.minIndex = i-1;
                // go left of max and subtract values that dropped out
                i = item.maxIndex;
                while (i>0 && allBindings[i].parsedTime.millis>maxInt && allBindings[i].rsgeo.value===rsgeo) {
                    var curBinding = allBindings[i];
                    item.maxIndex = i;
                    if (curBinding.observation.value && !isNaN(curBinding.observation.value))
                        sumToSubtract += parseFloat(curBinding.observation.value);
                    i--;
                }
                if (i>0 && allBindings[i].rsgeo.value === rsgeo)
                    item.maxIndex = i;
                else
                    item.maxIndex = i+1;
                // go right of max and add values that got in
                i = item.maxIndex+1;
                while (i<allBindings.length && allBindings[i].parsedTime.millis<=maxInt && allBindings[i].rsgeo.value===rsgeo) {
                    var curBinding = allBindings[i];
                    item.maxIndex = i;
                    if (curBinding.observation.value && !isNaN(curBinding.observation.value))
                        sumToAdd += parseFloat(curBinding.observation.value);
                    i++;
                }
                sumCurrent += sumToAdd;
                sumCurrent -= sumToSubtract;
                item.observation.value = sumCurrent.toString();
            });
        } else $(geoForMapAllTimesData.dataAllTimes.results.bindings).each(function (index, item) {
//            var currentDate = Date.UTC(item.parsedTime.year, item.parsedTime.month);
            var curTimeInMillis = item.parsedTime.millis;
            if (timeChartFirstTime === undefined && timeChartLastTime === undefined) {
                timeChartFirstTime = timeChartLastTime = curTimeInMillis;
            } else {
                if (curTimeInMillis < timeChartFirstTime) timeChartFirstTime = curTimeInMillis;
                if (curTimeInMillis > timeChartLastTime) timeChartLastTime = curTimeInMillis;
            }
            geoForMapAllTimesData.firstPass = false;
            var currentDate = item.parsedTime.millis;
            if (currentDate !== beforeTime) {
                previousTime = beforeTime;
                beforeTime = currentDate;
            }
            if (currentDate >= minInt && currentDate <= maxInt) {
                // add item if it doesn't exist
                var itemToIncrease = getItemToIncrease(newData.results.bindings, item);
                if (itemToIncrease === null) {
                    var itemAdd = $.extend(true, {}, item);
                    itemAdd.minIndex = index;
                    itemAdd.maxIndex = index;
                    itemAdd.rstime.value = "Aggregated";
                    if (!itemAdd.observation.value)
                        itemAdd.observation.value = 0;
                    newData.results.bindings.push(itemAdd);
                } else { // add value if it does exist
                    var sum = parseFloat(itemToIncrease.observation.value);
                    if (item.observation.value && !isNaN(item.observation.value))
                        sum += parseFloat(item.observation.value);
                    itemToIncrease.observation.value = sum.toString();
                    itemToIncrease.maxIndex = index;
                }
            }
        });
        lastNewData = newData;
        lastMinIndex = minInt;
        lastMaxIndex = maxInt;
        if (geoForMapAllTimesData.cbFunction)
            geoForMapAllTimesData.cbFunction(newData, true);
    }
    var e = new Date().getTime();
    redrawCount++;
    console.log('Redraw called: ' + redrawCount);
    console.log('Execution time: ' + (e - s));
}

function exampleShit(containerName, granularity) {
    $.getJSON('https://www.highcharts.com/samples/data/jsonp.php?filename=aapl-ohlcv.json&callback=?', function (data) {

        // split the data set into ohlc and volume
        var ohlc = [],
            volume = [],
            dataLength = data.length,
            // set the allowed units for data grouping
            groupingUnits = [[
                'week',                         // unit name
                [1]                             // allowed multiples
            ], [
                'month',
                [1, 2, 3, 4, 6]
            ]],

            i = 0;

        for (i; i < dataLength; i += 1) {
            ohlc.push([
                data[i][0], // the date
                data[i][1], // open
                data[i][2], // high
                data[i][3], // low
                data[i][4] // close
            ]);

            volume.push([
                data[i][0], // the date
                data[i][5] // the volume
            ]);
        }


        // create the chart
        chart = new Highcharts.StockChart({
            
            chart: {
	    	renderTo : containerName,
	        alignTicks: false,
                zoomType: 'x'
//                ,events: {
//                    redraw: onTimeWindowChange, 
//                    selection: function(event) {
//                        console.log('Selection changed');
//                        console.log(event.xAxis[0]);
//                        console.log(event.yAxis[0]);
//                    }
//                }
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
	         }
	    },

            rangeSelector: {
                selected: 1, 
                inputEnabled: false
            },

            title: {
                text: 'AAPL Historical'
            },
            subtitle: {
              text: 'subtitleText'
            },

            yAxis: [{
                labels: {
                    align: 'right',
                    x: -3
                },
                title: {
                    text: 'OHLC'
                },
                height: '60%',
                lineWidth: 2
            }, {
                labels: {
                    align: 'right',
                    x: -3
                },
                title: {
                    text: 'Volume'
                },
                top: '65%',
                height: '35%',
                offset: 0,
                lineWidth: 2
            }],

            series: [{
                type: 'candlestick',
                name: 'AAPL',
                data: ohlc,
                dataGrouping: {
                    units: groupingUnits
                }
            }, {
                type: 'column',
                name: 'Volume',
                data: volume,
                yAxis: 1,
                dataGrouping: {
                    units: groupingUnits
                }
            }]
        });
        
	chart.redraw();
        currentChart = chart;
    });
}

var currentMainSeries = null;

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
function createTimeChart(containerName, chartData, titleText, subtitleText, seriesName, chartType, granularity, compareMeasure) {
    
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
    
    var newSeries = {
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
    };
    
    if (compareMeasure) {
        newSeries.yAxis = 2;
        newSeries.color = "#FFBC75";
        var measPretty = measurePrettyName(compareMeasure);
        newSeries.name = measPretty;
        // if there already is another axis
        if (currentChart.yAxis.length > 2) {
            currentChart.yAxis[2].update({
                title: {
                    text: measPretty
                }
            });
            currentChart.series[2].remove();
            currentChart.addSeries(newSeries);
        } else {
            currentChart.addAxis({
                labels: {
                    align: 'right',
                    x: -3
                },
                title: {
                    text: measPretty
                },
                top: "0%",
                height: "47%",
                offset: 0,
                lineWidth: 2
            });
            currentChart.yAxis[0].update({
                labels: {
                    align: 'right',
                    x: -3
                },
                title: {
                    text: measurePrettyName(javaSelectedMeasure)
                },
                top: "53%",
                height: "47%",
                offset: 0,
                opposite: false,
                lineWidth: 2
            });
            newSeries.yAxis = 2;
            currentChart.addSeries(newSeries);
        }
    } else {
        timeChartMin = 0;
        timeChartMax = 0;
        timeChartFirstTime = undefined;
        timeChartLastTime = undefined;
        minObservationValueAggregated = 0;
        maxObservationValueAggregated = 0;
        chartActivityControl.setChartType(chartTypes.time);
        clearAggregatedColoring();
        var options = {
	    chart: {
	    	renderTo : containerName,
	        alignTicks: false,
                zoomType: 'x',
                events: {
                    redraw: onTimeWindowChange, 
                    selection: function(event) {
//                        console.log('Selection changed');
//                        console.log(event.xAxis[0]);
//                        console.log(event.yAxis[0]);
                    }
                }
	    },
//	    tooltip: {
//	        formatter: function() {
//	            var date = new Date(this.x);
//	            var year = date.getFullYear();
//	            var month = '';
//	            var day = '';
//	            if (granularity === 'M') {
//	            	month = ('0' + (date.getMonth() + 1)).slice(-2) + '/';
//	            } else if (granularity === 'D') {
//	            	day = ('0' + date.getDate()).slice(-2) + '/' ;
//	            	month = ('0' + (date.getMonth() + 1)).slice(-2) + '/';
//	            }
//	            
//	            var value = addThousandsSeparators(this.y);
//	            return day + month + year + '<br/>' + '<span style="color:'+this.points[0].series.color+'">'+ this.points[0].series.name +'</span>: '+ value;
//	         }
//	    },
	    xAxis: {
	    	type: 'datetime',
//                minRange: 7516800000, 
                minRange: 259200000, 
                events: {
                    afterSetExtremes: function(event) {
                        //console.log('Extremes set: ' + event.min + '\t' + event.max);
                    }
                } 
//	    	dateTimeLabelFormats: {
//	    		day : labelFormatD,
//	    		week: labelFormatW,
//	    		month: labelFormatM,
//	            year: '%Y'
//	            
////		            	second: '%Y-%m-%d<br/>%H:%M:%S',
////						minute: '%Y-%m-%d<br/>%H:%M',
////						hour: '%Y-%m-%d<br/>%H:%M',
////						day: '%Y<br/>%m-%d',
////						week: '%Y<br/>%m-%d',
////						month: '%Y-%m',
////						year: '%Y'
//					
//					
//            }
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
	    series: [newSeries]
	};
        currentMainSeries = newSeries;
        chart = new Highcharts.StockChart(options);
	chart.redraw();
        currentChart = chart;
	
	$('#' + containerName).show('slow', function() {
	    // Animation complete.
	});
    } 
}

function timechartInitVuk(){
    
}
