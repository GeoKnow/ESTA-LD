
/* global chartActivityControl, chartTypes */

var chartBarSingleOptions;

var chartBarMultipleOptions;
var chartBarMultiple;

var currentChart = {
    redraw: function(){ console.warn('Called redraw on chart before it got creaed!'); }
};

function hideCharts() {
//	$('#highchartsbarsingle').hide();
//	$('#highchartsbarmultiple').hide();
}

function seriesShowHide(numberOfSeries, visible) {
    return function() {
        if (chartBarMultiple.series.length <= numberOfSeries) return;
        var index = this.chart.series.indexOf(this);
        if (index < 0) { console.err("Didn't find the shown/hidden series"); return; }
        chartBarMultiple.series[numberOfSeries + index].setVisible(visible);
    }
}

function createChartBarMultiple(chartSubtitle, arrayMultiple, chartCategories, seriesNames, compareMeasure) {
    var numberOfSeries = seriesNames.length;
    if (!compareMeasure) {
        chartActivityControl.setChartType(chartTypes.multi);
        chartActivityControl.numberOfSeries(numberOfSeries);
        chartBarMultiple = new Highcharts.Chart(chartBarMultipleOptions);
        chartBarMultiple.setTitle({text: ''}, {text: chartSubtitle});
        chartBarMultiple.xAxis[0].setCategories(chartCategories, false);
        for (var i = numberOfSeries - 1; i >= 0; i--) {
            var array = new Array();
            for (var j = 0; j < arrayMultiple[i].length; j++) {
                array.push(parseFloat(arrayMultiple[i][j]));
            }
            chartBarMultiple.addSeries({                        
                name: seriesNames[i],
                legendIndex: i + 1,
                data: array,
                // TODO these get lost after swapping, fix it
                events: {
                    hide: seriesShowHide(numberOfSeries, false), 
                    show: seriesShowHide(numberOfSeries, true)
                },
                visible: (numberOfSeries > 5) ? (i === 0) : true   //if there are too many series initially show only the first one
            }, false);
        }

        chartBarMultiple.redraw();
        $('#highchartsbarmultiple').show();
        currentChart = chartBarMultiple;   
    } else {
        var measPretty = measurePrettyName(compareMeasure);
        // modify the axes if compare wasn't already active
        if (currentChart.yAxis.length < 2) {
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
            for (var i = numberOfSeries - 1; i >= 0; i--) {
                var array = new Array();
                for (var j = 0; j < arrayMultiple[i].length; j++) {
                    array.push(parseFloat(arrayMultiple[i][j]));
                }
                chartBarMultiple.addSeries({                        
                    name: seriesNames[i],
//                    legendIndex: i + 1,
                    data: array,
                    yAxis: 1,
                    showInLegend: false,
                    color: chartBarMultiple.series[numberOfSeries-1-i].color,
                    visible: chartBarMultiple.series[numberOfSeries-1-i].visible,
                    type: chartBarMultiple.series[numberOfSeries-1-i].type
                }, false);
            }
        } else { // there already are comparison series, remove them
            for (var upIndex = 2*numberOfSeries - 1; upIndex >= numberOfSeries; upIndex--) {
                var array = new Array();
                var i2 = upIndex - numberOfSeries;
                for (var j = 0; j < arrayMultiple[i2].length; j++) {
                    array.push(parseFloat(arrayMultiple[i2][j]));
                }   
                chartBarMultiple.series[numberOfSeries-1-i2].update({
                    data: array
                }, false);
            }
        }
        chartBarMultiple.redraw();
        $('#highchartsbarmultiple').show();
    }
}

function createChartBarSingle(chartSubtitle, chartCategories, chartValues, seriesName, compareMeasure) {
    if (!compareMeasure) {
        chartActivityControl.setChartType(chartTypes.single);
        var chartBarSingle = new Highcharts.Chart(chartBarSingleOptions);
	chartBarSingle.setTitle({text: seriesName}, {text: chartSubtitle});
	chartBarSingle.xAxis[0].setCategories(chartCategories, false);
	chartBarSingle.series[0].setData(chartValues, true);
	chartBarSingle.series[0].name=seriesName;
        chartBarSingle.series[0].update({}, false);
	chartBarSingle.redraw();
        $('#highchartsbarmultiple').show();
        currentChart = chartBarSingle;
//        $('#highchartsbarmultiple').show('slow', function() {
            // Animation complete.
//        });
    } else {
        var measPretty = measurePrettyName(compareMeasure);
        var newSeries = {
            data: chartValues,
            name: measPretty,
            type: currentChart.series[0].type,
            yAxis: 1,
            color: "#FFBC75",
            showInLegend: false
        };
        if (currentChart.yAxis.length == 1) {
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
            currentChart.series[0].update({
                name: measurePrettyName(javaSelectedMeasure),
                showInLegend: false
            });
            currentChart.addSeries(newSeries);
        } else if (currentChart.yAxis.length == 2) {
            currentChart.yAxis[1].update({
                title: {
                    text: measPretty
                }
            });
            currentChart.series[1].remove();
            currentChart.addSeries(newSeries);
        }
        currentChart.redraw();
    }
}

function chartsInitVuk(){
    chartBarSingleOptions = {
	    chart: {
	    	renderTo: 'highchartsbarmultiple',
	    	type: 'column',
            margin: [50, 70, 70, 130]
	    },
	    title: {
	    	margin: 30,
	    	text: 'Regional development incentives'
	    },
	    subtitle: {
	    	text: ''
	    },
	    xAxis: {
	    	categories: chartBarSpaceCategories,
	    	title: {
	    		text: null
	        }
	    },
	    yAxis: {
//	    	min: 0,
//	        title: {
//	            text: 'Population (millions)',
//	            align: 'high'
//	        },
	        labels: {
	        	overflow: 'justify'
	        },
	        title: {
	        	text: null
	        }
	    },
//	    tooltip: {
//	        valueSuffix: ' millions'
//	    },
	    plotOptions: {
	    	bar: {
	    		dataLabels: {
	        			enabled: false
	    		}
	    	}
	    },
//		        legend: {
//		            layout: 'vertical',
//		            align: 'right',
//		            verticalAlign: 'top',
//		            x: -40,
//		            y: 100,
//		            floating: true,
//		            borderWidth: 1,
//		            backgroundColor: '#FFFFFF',
//		            shadow: true
//		        },
	    credits: {
	    	enabled: false
	    },
	    series: [{
	    	showInLegend: false,
	    	name: 'Incentives',
	    	data: chartBarSpaceValues
//		        }, {
//		            name: 'Year 2008',
//		            data: [973, 914, 4054, 732, 34]
	    }]
	};

chartBarMultipleOptions = {
	    chart: {
	    	renderTo: 'highchartsbarmultiple',
	        type: 'column',
            margin: [50, 70, 70, 130]
	    },
	    title: {
	    	margin: 30,
	        text: 'Regional development incentives'
	    },
	    subtitle: {
	        text: 'Area: RS'
	    },
	    xAxis: {
	        categories: INCENTIVE_NAMES[1],
	        title: {
	            text: null
	        }
	    },
	    yAxis: {
//	        min: 0,
//	        title: {
//	            text: 'Population (millions)',
//	            align: 'high'
//	        },
	        labels: {
	            overflow: 'justify'
	        },
	        title: {
	        	text: null
	        }
	    },
//	    tooltip: {
//	        valueSuffix: ' millions'
//	    },
	    plotOptions: {
//	        bar: {
//	            dataLabels: {
//	                enabled: false
//	            }
//	        }
                column: {
                    grouping: true
                }
	    },
        legend: {
            layout: 'vertical',
            align: 'right',
            verticalAlign: 'top',
            x: -40,
            y: 100,
            floating: true,
            borderWidth: 1,
            backgroundColor: '#FFFFFF',
            shadow: true
        },
	    credits: {
	        enabled: false
	    }
	    
	};


}
