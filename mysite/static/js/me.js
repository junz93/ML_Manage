$(document).ready(function(){
    // validation for form 1 before submission
    $("#Part1 form :input").blur(function(){
        var $parent = $(this).parent();
        if($(this).is("[type='file']")){
            $parent.removeClass("has-error");
            $(this).prev().text("");
            if($("#model_id").val() === "0"){
                $(this).addClass("required");
            }
            else{
                $(this).removeClass("required");
            }
        }
        
        if($(this).hasClass("required")){
            if($(this).val() == ""){
                $parent.addClass("has-error");
                $(this).prev().text("不能为空！");
            }
            else{
                $parent.removeClass("has-error").addClass("has-success");
                $(this).prev().text("");
            }
        }

        if($(this).hasClass("number")){
            if($(this).val() != ""){
                if(isNaN($(this).val())){
                    $parent.addClass("has-error");
                    $(this).prev().text("请输入数字！");            
                }
                else{
                    $parent.removeClass("has-error").addClass("has-success");
                    $(this).prev().text("");
                }
            }
        }

        if($(this).hasClass('numlist')){
            if($(this).val() != ""){
                var num_list = $(this).val().trim().split(" ");
                for(var i = 0; i < num_list.length; i++){
                    if(isNaN(num_list[i])){
                        $parent.addClass("has-error");
                        $(this).prev().text("请输入正确格式！");
                        break;
                    }
                }
                if(!isNaN(num_list[i])){
                    $parent.removeClass("has-error").addClass("has-success");
                    $(this).prev().text("");
                }
            }
        }
    });

    // validation for form 1 when submitting
    $("#Part1 form").submit(function(){
        $(this).find(':input').trigger("blur");
        var numError = $(this).find(".has-error").length;
        if(numError){
            return false;
        }
    });

    // load the type2 according to the selected type1
    $("#typeSel1").change(function(event, type2){
        var $sel2 = $("#typeSel2");
        $sel2.empty();
        $sel2.append("<option value=''></option>");
        if(event.target.value === "")
            return;

        $.get("/manage/", {"type1": event.target.value}, function(data){
            $.each(data, function(index, val){
                $sel2.append("<option>" + val + "</option>");
            });

            if(typeof type2 != "undefined"){
                $sel2.val(type2);
                $sel2.trigger("blur");
            }
        }, "json");
    });

    // load the models of the corresponding type2
    $("a.loadmodels").click(function(){
        var typeid = $(this).attr("data-id");
        var $panel_body = $(this).closest('.panel-heading').next().children();
        if($panel_body.html() != ""){
            return;
        }

        $.get("/manage/loadmodels/", {"type_id": typeid}, function(data){
            if(data.length == 0){
                $panel_body.html("None");
                return;
            }

            var $list = $("<ul class='list-unstyled category'></ul>");
            $.each(data, function(index, val){
                var $item = $("<li>" + val.name +"</li>");
                $item.attr('data-id', val.id);
                $item.click(function(){
                    var model_id = $(this).attr("data-id");
                    $.get("/manage/fillmodel/", {"model_id": model_id, "query_type": "lite"}, function(data){
                        $("#mId").val(data.id);
                        $("#mName").val(data.model_name);
                        $("#mIntro").val(data.description);
                    });
                });
                $item.appendTo($list);
            });
            $list.appendTo($panel_body);
        });
    });

    // switch the type of data(text/file) when invoking a model
    $("#Part2 [name='dataRadio']").click(function(){
        if($("#Part2 [name='dataRadio']:first").prop("checked")){
            $("#data").replaceWith("<input type='text' class='form-control' id='data'>");
        }
        else{
            $("#data").replaceWith("<input type='file' class='form-control' id='data'>");
        }
    });

    // modify an existing model
    $("#modify").click(function(){
        if($("#mId").val() == 0)
            return;

        var model_id = $("#mId").val();
        $.get('/manage/fillmodel/', {"model_id": model_id, "query_type": "full"}, function(data){
            $("#model_id").val(data.id);
            $("#typeSel1").val(data.type1);
            $("#typeSel1").triggerHandler('change', data.type2);
            $("#model_name").val(data.model_name);
            $("#num_layer").val(data.num_layer);
            $("#num_unit").val(data.num_unit);
            $("#lr").val(data.lr);
            $("#d_in").val(data.d_in);
            $("#d_out").val(data.d_out);
            $("#comment").val(data.comment);                
            $("#myTab a:first").tab("show");
            $("#Part1 form :input").trigger("blur");            
        });        
    });

    $("#invoke").click(function(){	
	var model_id = $("#mId").val();
	var topic = $("#topic").val();
	$.get('/manage/load/', {"model_id": model_id, "tpoic": topic}, function(data){
	  window.open(location.protocol + "//" + location.host + "/manage/plot/");
 	});    
   });

    $("#plot").click(function(){	
      window.open(location.protocol + "//" + location.host + "/manage/plot/");	    
   });

    // delete a model
    $("#delete").click(function(){
        if($("#mId").val() == 0){
            return false;
        }
    });
});
