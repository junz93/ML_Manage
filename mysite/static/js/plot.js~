$(document).ready(function(){
  Chart.defaults.global.animation = false;
  Chart.defaults.global.showTooltips = false;
  var ctx = document.getElementById("myChart").getContext("2d");
  var myData = {
    labels: ["1s", "2s", "3s", "4s", "5s", "6s","7s", "8s", "9s","10s", "11s", "12s"],
    datasets: [
        {
            label: "My dataset",
            fillColor: "rgba(220,220,220,0.2)",
            strokeColor: "rgba(220,220,220,1)",
            pointColor: "rgba(220,220,220,1)",
            pointStrokeColor: "#fff",
            pointHighlightFill: "#fff",
            pointHighlightStroke: "rgba(220,220,220,1)",
            data: [0,0,0,0,0,0,0,0,0,0,0,0]
        }       
    ]
  };
  setInterval(function() {
    $.get(window.location.pathname,function(data){
       $.each(data, function(index, val){
                myData.datasets[0].data[index] = parseInt(val)
            });
    });
    new Chart(ctx).Line(myData);
  }, 1000);
 
  $("#stop").click(function(){
    var i = window.location.pathname.lastIndexOf('/');
    var model_id = window.location.pathname.substr(i+1);
    $.get('/manage/stop/' + model_id, function(data){
      $.each(data, function(index, val){
        if(val == "true")
	{
	  alert("结束调用成功！");
          window.close();
	}
	else
	{
	  alert("未调用！");
          window.close();
	}
      });
    });
  });
});
