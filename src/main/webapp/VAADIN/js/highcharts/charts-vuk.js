
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


function createChartBarMultiple(chartSubtitle, arrayMultiple, chartCategories, seriesNames ) {
	
	chartBarMultiple = new Highcharts.Chart(chartBarMultipleOptions);
    chartBarMultiple.setTitle({text: ''}, {text: chartSubtitle});
    chartBarMultiple.xAxis[0].setCategories(chartCategories, false);
    var numberOfSeries = seriesNames.length;
    for (var i = numberOfSeries - 1; i >= 0; i--) {
    	var array = new Array();
    	for (var j = 0; j < arrayMultiple[i].length; j++) {
			array.push(parseFloat(arrayMultiple[i][j]));
		}
    	chartBarMultiple.addSeries({                        
		    name: seriesNames[i],
		    legendIndex: i + 1,
		    data: array,
		    visible: (numberOfSeries > 5) ? (i === 0) : true   //if there are too many series initially show only the first one
		}, false);
	}
    
    chartBarMultiple.redraw();
    $('#highchartsbarmultiple').show();
    currentChart = chartBarMultiple;
}

function createChartBarSingle(chartSubtitle, chartCategories, chartValues, seriesName) {
	var chartBarSingle = new Highcharts.Chart(chartBarSingleOptions);
	chartBarSingle.setTitle({text: seriesName}, {text: chartSubtitle});
	chartBarSingle.xAxis[0].setCategories(chartCategories, false);
	chartBarSingle.series[0].setData(chartValues, true);
	chartBarSingle.series[0].name=seriesName;
	chartBarSingle.redraw();
        $('#highchartsbarmultiple').show();
        currentChart = chartBarSingle;
//        $('#highchartsbarmultiple').show('slow', function() {
            // Animation complete.
//        });
	
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
	    	min: 0,
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
//            exporting: {
//                buttons: {
//                    stackButton: {
//                        text: 'Stack', 
////                        symbol: 'square', 
//                        onclick: function () {
//                            if (chartBarMultiple.options.plotOptions.column.stacking)
//                                delete chartBarMultiple.options.plotOptions.column.stacking;
//                            else {
//                                var plotOptionsObject = {
//                                    column: {
//                                        stacking: 'normal'
//                                    }
//                                };
//                                $.extend(true, chartBarMultiple.options.plotOptions, plotOptionsObject);
//                            }
//                            
//                            chartBarMultiple.xAxis[0].update({}, false);
//                            chartBarMultiple.yAxis[0].update({}, false);
//                            $(chartBarMultiple.series).each(function (k,v) {
//                                v.update({}, false);
//                            });
//            
//                            chartBarMultiple.redraw();
//                        }
//                    }, 
//                    invertButton: {
//                        text: 'Invert', 
//                        onclick: function () {
//                            if (chartBarMultiple.inverted)
//                                chartBarMultiple.inverted = false;
//                            else
//                                chartBarMultiple.inverted = true;
//                            
//                            chartBarMultiple.xAxis[0].update({}, false);
//                            chartBarMultiple.yAxis[0].update({}, false);
//                            $(chartBarMultiple.series).each(function (k,v) {
//                                v.update({}, false);
//                            });
//            
//                            chartBarMultiple.redraw();
//                        }
//                    }
//                }
//            }, 
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
	        min: 0,
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
