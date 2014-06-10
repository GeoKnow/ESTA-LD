
var chartBarSingleOptions = {
	    chart: {
	    	renderTo: 'highchartsbarsingle',
	    	type: 'bar',
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

var chartBarMultipleOptions = {
	    chart: {
	    	renderTo: 'highchartsbarmultiple',
	        type: 'bar',
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


function hideCharts() {
	$('#highchartsbarsingle').hide();
	$('#highchartsbarmultiple').hide();
}


function createChartBarMultiple(chartSubtitle, arrayMultiple, chartCategories, seriesNames ) {
	
	var chartBarMultiple = new Highcharts.Chart(chartBarMultipleOptions);
    chartBarMultiple.setTitle({text: 'Regional development incentives'}, {text: chartSubtitle});
    chartBarMultiple.xAxis[0].setCategories(chartCategories, false);
    var numberOfSeries = seriesNames.length;
    for (var i = numberOfSeries - 1; i >= 0; i--) {
    	var array = new Array();
    	for (var j = 0; j < arrayMultiple[i].length; j++) {
			array.push(parseInt(arrayMultiple[i][j]));
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
    
}

function createChartBarSingle(chartSubtitle, chartCategories, chartValues, seriesName) {
	var chartBarSingle = new Highcharts.Chart(chartBarSingleOptions);
	chartBarSingle.setTitle({text: 'Regional development incentives'}, {text: chartSubtitle});
	chartBarSingle.xAxis[0].setCategories(chartCategories, false);
	chartBarSingle.series[0].setData(chartValues, true);
	chartBarSingle.series[0].name=seriesName;
	chartBarSingle.redraw();
    
    //show
//    if (!loading && visibleLayer !== 'Municipalities') {
    if (chartValues.length > 0) {
		
		$('#highchartsbarsingle').show('slow', function() {
		    // Animation complete.
		});
	    
	}
}

