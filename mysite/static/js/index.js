function validate_form(){
    var $parent = $(this).closest(".form-group");

    if($(this).hasClass("required")){
        if($(this).val() == ""){
            $parent.removeClass("has-success").addClass("has-error");
            $parent.children(".control-label").text("不能为空！");
            return false;
        }
        else{
            $parent.removeClass("has-error").addClass("has-success");
            $parent.children(".control-label").text("");
        }
    }

    if($(this).hasClass("number")){
        if(isNaN($(this).val())){
            $parent.removeClass("has-success").addClass("has-error");
            $parent.children(".control-label").text("请输入数字！");
            return false;
        }
        else{
            $parent.removeClass("has-error").addClass("has-success");
            $parent.children(".control-label").text("");
        }
    }

    if($(this).hasClass('numlist')){
        if($(this).val() != ""){
            var num_list = $(this).val().trim().split(" ");
            for(var i = 0; i < num_list.length; i++){
                if(isNaN(num_list[i])){
                    $parent.removeClass("has-success").addClass("has-error");
                    $parent.children(".control-label").text("请输入正确格式！");
                    return false;
                }
            }
            $parent.removeClass("has-error").addClass("has-success");
            $parent.children(".control-label").text("");
        }
    }
    
    return true;
}

$(document).ready(function(){

    // validation for form before submission
    $("form :input").blur(validate_form);

    // validation for form when submitting
    $("form").submit(function(){
        $(this).find(':input').trigger("blur");
        var numError = $(this).find(".has-error").length;
        if(numError){
            return false;
        }
        $("#add").prop("disabled", true).attr("value", "训练中……");
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
            }
        }, "json");
    });

    var $network = $("#num_network");
    var $layer = $("#num_layer1").closest(".form-group");
    var $unit = $layer.next();
    var $pos = $unit.next();
    $("#newNetwork").click(function(){
        var i = +$network.val() + 1;
        $network.val(i);
        $layer = $layer.clone(true).removeClass("has-error")
            .children("label:first").attr("for", "num_layer"+i).text("网络层数 "+i)
            .siblings(".control-label").attr("for", "num_layer"+i).text("")
            .next("input").attr({"id": "num_layer"+i, "name": "num_layer"+i})
            .parent().insertBefore($pos);
        $unit = $unit.clone(true).removeClass("has-error")
            .children("label:first").attr("for", "num_unit"+i).text("各层神经元个数 "+i)
            .siblings(".control-label").attr("for", "num_unit"+i).text("")
            .next("input").attr({"id": "num_unit"+i, "name": "num_unit"+i})
            .parent().insertBefore($pos);
        $(this).parent().remove();
    });

    // load the models of the corresponding type
    $("div[id^='type1_'] a").click(function(){
        var type_id = $(this).attr("data-id");
        var $panel_body = $(this).closest(".panel-heading").next().children(".panel-body");
        if($panel_body.html() != ""){
            return;
        }

        $.get("/manage/loadmodels/", {"type_id": type_id}, function(data){
            if(data.length == 0){
                $panel_body.html("None");
                return;
            }

            var $list = $("<ul class='list-unstyled category'></ul>");
            $.each(data, function(index, val){
                var $item = $("<li>" + val.name +"</li>");
                $item.attr('data-id', val.id);
                // load the model clicked
                $item.click(function(){
                    $("div[id^='type1_'] li.active").removeClass("active");
                    $(this).addClass("active");
                    var model_id = $(this).attr("data-id");
                    $.get("/manage/fillmodel/", {"model_id": model_id, "query_type": "lite"}, function(data){
                        $("#mId").val(data.id);
                        $("#mName").val(data.model_name);
                        $("#mIntro").val(data.intro);
                    });
                });
                $item.appendTo($list);
            });
            $list.appendTo($panel_body);
        });
    });

    // single or batching for learning rate
    var $lr1 = $("#lr");
    var $lr2 = $("<div id='lr' class='row'></div>");
    $lr2.append("<div class='col-md-4'><label class='sr-only' for='lr-begin'>初值</label>\
        <input type='text' class='form-control required number' name='lr-begin' id='lr-begin' placeholder='初值'></div>");
    $lr2.append("<div class='col-md-4'><label class='sr-only' for='lr-end'>终值</label>\
        <input type='text' class='form-control required number' name='lr-end' id='lr-end' placeholder='终值'></div>");
    $lr2.append("<div class='col-md-4'><label class='sr-only' for='lr-step'>步长</label>\
        <input type='text' class='form-control required number' name='lr-step' id='lr-step' placeholder='步长'></div>");
    $lr2.find(" :input").blur(validate_form);
    $("[name='lrRadio']").click(function(){
        $("#lr").detach();
        var $parent = $(this).closest(".form-group");
        if($("[name='lrRadio']:first").prop("checked")){
            $parent.append($lr1);
        }
        else{
            $parent.append($lr2);
        }
    });

    // single or batching for the number of a batch
    var $batch1 = $("#num_batch");
    var $batch2 = $("<div id='num_batch' class='row'></div>");
    $batch2.append("<div class='col-md-4'><label class='sr-only' for='batch-begin'>初值</label>\
        <input type='text' class='form-control required number' name='batch-begin' id='batch-begin' placeholder='初值'></div>");
    $batch2.append("<div class='col-md-4'><label class='sr-only' for='batch-end'>终值</label>\
        <input type='text' class='form-control required number' name='batch-end' id='batch-end' placeholder='终值'></div>");
    $batch2.append("<div class='col-md-4'><label class='sr-only' for='batch-step'>步长</label>\
        <input type='text' class='form-control required number' name='batch-step' id='batch-step' placeholder='步长'></div>");
    $batch2.find(" :input").blur(validate_form);
    $("[name='batchRadio']").click(function(){
        $("#num_batch").detach();
        var $parent = $(this).closest(".form-group");
        if($("[name='batchRadio']:first").prop("checked")){
            $parent.append($batch1);
        }
        else{
            $parent.append($batch2);
        }
    });

    // switch the type of file(select/upload) when training a model
    var $file1 = $("#file_data");
    var $file2 = $("<input type='file' id='file_data' name='file_data' class='form-control required'>");
    $file2.blur(validate_form);
    $("[name='fileRadio']").click(function(){
        $("#file_data").detach();
        var $parent = $(this).closest(".form-group");
        if($("[name='fileRadio']:first").prop("checked")){
            $parent.append($file1);
        }
        else{
            $parent.append($file2);
        }
    });

    // switch the type of TypeName1 when creating a new type
    var $tn1 = $("#newType1");
    var $tn2 = $("<input type='text' class='form-control required' name='newType1' id='newType1'>");
    $tn2.blur(validate_form);
    $("[name='typeRadio']").click(function(){
        $("#newType1").detach();
        var $parent = $(this).closest(".form-group");
        if($("[name='typeRadio']:first").prop("checked")){
            $parent.append($tn1);
        }
        else{
            $parent.append($tn2);
        }
    });

    $(".data-list li").click(function(){
        $(".data-list li.active").removeClass();
        $(this).addClass("active");
        var data = $(this).attr("data-id").split("_");
        $("#data_id").val(data[0]);
        $("#data_name").val($(this).text());
        $("#data_in").val(data[1]);
        $("#data_out").val(data[2]);
    });

    // modify an existing model
    $("#modify").click(function(){
        var model_id = $("#mId").val();
        if(model_id == "0")
            return;

        $.get('/manage/fillmodel/', {"model_id": model_id, "query_type": "full"}, function(data){
            $("[name='lrRadio']:first").prop("checked", true).trigger('click');
            $("[name='batchRadio']:first").prop("checked", true).trigger('click');
            $("[name='fileRadio']:first").prop("checked", true).trigger('click');
            $("#model_id").val(data.id);
            $("#typeSel1").val(data.type1).trigger('change', data.type2);
            $("#model_name").val(data.model_name);
            $("#num_network").val("1");
            $("#num_layer1").val(data.num_layer);
            $("#num_unit1").val(data.num_unit);
            $("#lr").val(data.lr);
            $("#loss").val(data.loss);
            $("#max_iter").val(data.num_iter);
            $("#num_batch").val(data.num_batch);
            $("#d_in").val(data.d_in);
            $("#d_out").val(data.d_out);
            $("#file_data").val(data.file);
            $("#comment").val(data.comment);
            $("#myTab a:first").tab("show");
        });        
    });

    // invoke a model
   $("#invoke").click(function(){
	var model_id = $("#mId").val();
	var topic_sub = $("#sub").val();
	var topic_pub = $("#pub").val();
	if(model_id == "0"){
            alert("请选择要调用的模型！");
            return;
        }
	if(topic_sub == "" || topic_sub == null){
            alert("请选择订阅数据主题！");
            return;
        }
	if(topic_pub == "" || topic_pub == null){
            alert("请选择发布数据主题！");
            return;
        }	
	$.get('/manage/invoke/', {"model_id": model_id, "topic_sub": topic_sub, "topic_pub": topic_pub}, function(data){
	    $.each(data, function(index, val){
                if(val == "true")
	    	{
	    	    alert("调用成功！");
		    window.open(location.protocol + "//" + location.host + "/manage/plot/" + model_id);	
	    	}
		else
		{
		    alert("已调用！");
		    window.open(location.protocol + "//" + location.host + "/manage/plot/" + model_id);	
		}

            });   
 	});    
   });
	
    
    // delete a model
    $("#delete_model").click(function(){
        var model_id = $("#mId").val();
        if(model_id == "0"){
            alert("请选择要删除的模型！");
            return;
        }

        if(confirm("确认删除？")){
            $.get('/manage/delete/', {"type": "model", "id": model_id}, function(data){
                if(data != "0"){
                    alert("删除成功！");
                    $("div[id^='type1_'] li[data-id="+model_id+"]").remove();
                    $("#mId").val("0");
                    $("#mName, #mIntro").val("");
                }
                else
                    alert("删除失败！");
            });
        }
    });

    // delete a data
    $("#delete_data").click(function(){
        var data_id = $("#data_id").val();
        if(data_id == "0"){
            alert("请选择要删除的数据文件！");
            return;
        }

        if(confirm("确认删除？")){
            $.get('/manage/delete/', {"type": "data", "id": data_id}, function(data){
                if(data != "0"){
                    alert("删除成功！");
                    $("div.data-list li[data-id^="+data_id+"_]").remove();
                    $("#data_id").val("0")
                    $("#data_name, #data_in, #data_out").val("");
                }
                else
                    alert("删除失败！");
            });
        }
    });
});
