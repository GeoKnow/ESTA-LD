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
	        alignTicks: false
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
