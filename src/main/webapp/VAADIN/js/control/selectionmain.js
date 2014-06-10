$(document).ready(function() {
	//Ajax activity indicator bound to ajax start/stop document events
	$(document).ajaxStart(function(){
		$('#ajaxBusy').show();
	}).ajaxStop(function(){
		$('#ajaxBusy').hide();
	});
	
	//onclick function for launch
	$('#launch').click(function() {
		sessionStorage.setItem('endpoint',$('#endpoint').val());
		sessionStorage.setItem('graph',$('#graphlist').val());
		sessionStorage.setItem('analysistype',$('#analysistype').val());
	});
	
	$('#endpoint').blur(function() {
		execSparqlGraphs();
	});
	
	
});


var cbFuncGraph = function(data) {
	$("#error").css('visibility', 'hidden');//remove error message
	$("#launch").removeAttr("disabled");//enable launch button
	$("#launch").attr('src', 'resources/images/launch_e.png');
	$('#graphlist').empty();
	$(data.results.bindings).each(function(key, val){
		var selectedString = '';
		var graph = val.g.value;
		if (graph.indexOf('elpo.stat') > -1 || graph.indexOf('stat.apr') > -1) {// add only elpo.stat and stat.apr graphs
			var itemval = '<option value="' + graph + '" ' + selectedString + '">' + graph + '</option>';
		    $('#graphlist').append(itemval);
		}
	});
};
