# -*- coding: utf-8 -*-

from collections import deque
from django.shortcuts import render
from django.http import HttpResponse, HttpResponseRedirect, JsonResponse
from django.core.urlresolvers import reverse
from models import Category, Data, LearningModel
from subprocess import call, Popen
import os
import datetime
import json
import urllib2
import pickle
import numpy as np
import mxnet as mx


# Create your views here.

count = {}
count_list = {}
sub_process = {}
LearningModel_set = list()

def index(request):
    if request.is_ajax():
        type1 = request.GET["type1"]
        type2_set = [category.type_2 for category in
                     Category.objects.filter(type_1=type1).order_by("type_2")]
        return JsonResponse(type2_set, safe=False)

    if "add_type" in request.POST:
        type1 = request.POST["newType1"]
        type2 = request.POST["newType2"]
        category = Category(type_1=type1, type_2=type2)
        category.save()

    type1_set = [category["type_1"] for category in Category.objects.all().values("type_1").distinct().order_by("type_1")]
    type_set = [(type1, [category for category in Category.objects.filter(type_1=type1).order_by("type_2")])
                for type1 in type1_set]

    with open('dl_manage/topic_config.json') as data_file:
        data = json.load(data_file)
        sub_topic_set = data['topic_sub']
        pub_topic_set = data['topic_pub']
    return render(request, "dl_manage/index.html", {"type_set": type_set, "data_set": Data.objects.all(), "sub_topic_set": sub_topic_set, "pub_topic_set": pub_topic_set})


def add(request):
    model_id = request.POST["model_id"]
    type1 = request.POST["typeSel1"]
    type2 = request.POST["typeSel2"]
    category = Category.objects.get(type_1=type1, type_2=type2)
    model_name = request.POST["model_name"]
    num_network = int(request.POST["num_network"])
    net_set = [(request.POST["num_layer" + str(i + 1)],
                request.POST["num_unit" + str(i + 1)]) for i in xrange(num_network)]
    max_iter = request.POST["max_iter"]
    loss = request.POST["loss"]
    # learning rate
    if request.POST["lrRadio"] == "lr1":
        lr_set = [request.POST["lr"]]
    else:
        begin = float(request.POST["lr-begin"])
        end = float(request.POST["lr-end"])
        step = float(request.POST["lr-step"])
        num = int((end - begin) / step)
        lr_set = [begin + i * step for i in xrange(num + 1)]
    # number of a batch
    if request.POST["batchRadio"] == "batch1":
        num_batch_set = [request.POST["num_batch"]]
    else:
        begin = float(request.POST["batch-begin"])
        end = float(request.POST["batch-end"])
        step = float(request.POST["batch-step"])
        num = int((end - begin) / step)
        num_batch_set = [begin + i * step for i in xrange(num + 1)]

    d_in = request.POST["d_in"]
    d_out = request.POST["d_out"]
    comment = request.POST["comment"]
    if request.POST["fileRadio"] == "file1":
        data = Data.objects.get(id=request.POST["file_data"])
    else:
        file_data = request.FILES["file_data"]
        data = Data(data_file=file_data, dimension_in=d_in, dimension_out=d_out)
        data.save()

    norm_para = preProcess(data)

    global LearningModel_set
    LearningModel_set = []
    if model_id != "0":
        lmodel = LearningModel.objects.get(id=model_id)
        lmodel.model_name = model_name
        lmodel.type = category
        if lmodel.num_layer == int(net_set[0][0]) and lmodel.num_unit == net_set[0][1] \
                and lmodel.num_iter == int(max_iter) and lmodel.num_batch == int(num_batch_set[0]) \
                and lmodel.lr == float(lr_set[0]) and  lmodel.error_rate == float(loss) and lmodel.data.id == data.id:
            lmodel.save()
            return HttpResponseRedirect(reverse('dl_manage:index'))
        lmodel.data = data
        lmodel.num_layer, lmodel.num_unit = net_set[0]
        lmodel.lr = lr_set[0]
        lmodel.num_batch = num_batch_set[0]
        lmodel.comment = comment
        train(lmodel, loss, max_iter)
        LearningModel_set.append(lmodel)
    else:
        i = 1
        for num_layer, num_unit in net_set:
            for lr in lr_set:
                for num_batch in num_batch_set:
                    if i == 1:
                        name = model_name
                    else:
                        name = "%s (%d)" % (model_name, i)

                    lmodel = LearningModel(model_name=name, type=category, data=data, dimension_in=d_in, dimension_out=d_out, num_layer=num_layer, num_unit=num_unit,
                                           data_name=data.data_file.name, lr=lr, num_batch=num_batch, norm_para= norm_para, comment=comment)
                    train(lmodel, max_iter, loss)
                    LearningModel_set.append(lmodel)
                    i += 1
    data_path = data.data_file.name
    data_name = data_path[data_path.find('/')+1:data_path.rfind('.')]
    os.remove('data/'+data_name+'_train_data.csv')
    os.remove('data/'+data_name+'_train_label.csv')
    os.remove('data/'+data_name+'_test_data.csv')
    os.remove('data/'+data_name+'_test_label.csv')
    LearningModel_set.sort(key=lambda lmodel: lmodel.train_error)
    return render(request, "dl_manage/add.html", {"lmodel_set": LearningModel_set})


def save(request):
    global LearningModel_set
    if "model" in request.POST:
        index = request.POST.getlist("model")
        for i in index:
            LearningModel_set[int(i)].save()
    return HttpResponseRedirect(reverse('dl_manage:index'))


def delete(request):
    if request.GET["type"] == "model":
        model_id = request.GET["id"]
        lmodel = LearningModel.objects.get(id=model_id)
        result = lmodel.delete()
    elif request.GET["type"] == "data":
        data_id = request.GET["id"]
        data = Data.objects.get(id=data_id)
        data.data_file.delete()
        result = data.delete()
    return HttpResponse(result[0])


def loadmodels(request):
    type_id = request.GET["type_id"]
    lmodel_set = LearningModel.objects.filter(type__id=type_id).order_by("model_name")
    model_set = [{"id": lmodel.id, "name": lmodel.model_name} for lmodel in lmodel_set]
    return JsonResponse(model_set, safe=False)


def fillmodel(request):
    model_id = request.GET["model_id"]
    lmodel = LearningModel.objects.get(id=model_id)
    model_dict = dict()
    model_dict["id"] = lmodel.id
    model_dict["model_name"] = lmodel.model_name
    if request.GET["query_type"] == "lite":
        model_dict["intro"] = lmodel.get_intro()
    elif request.GET["query_type"] == "full":
        model_dict["type1"] = lmodel.type.type_1
        model_dict["type2"] = lmodel.type.type_2
        model_dict["num_layer"] = lmodel.num_layer
        model_dict["num_unit"] = lmodel.num_unit
        model_dict["lr"] = lmodel.lr
        model_dict["loss"] = lmodel.error_rate
        model_dict["num_iter"] = lmodel.num_iter
        model_dict["num_batch"] = lmodel.num_batch
        model_dict["d_in"] = lmodel.dimension_in
        model_dict["d_out"] = lmodel.dimension_out
        if lmodel.data:
            model_dict["file"] = lmodel.data.id
        else:
            model_dict["file"] = ""
        model_dict["comment"] = lmodel.comment
    return JsonResponse(model_dict)

def preProcess(data):
    data_path = data.data_file.name
    data_name = data_path[data_path.find('/')+1:data_path.rfind('.')]
    d_in = int(data.dimension_in)
    data = np.loadtxt(data_path)
    np.random.shuffle(data)
    num = data.shape[0]
    tmp = data[:,:d_in]
    minVals = tmp.min(0)
    maxVals = tmp.max(0)
    ranges = maxVals-minVals
    k = 0
    for each in ranges:
        if each == 0:
            ranges[k] = 1
        k = k + 1
    norm = tmp-np.tile(minVals,(num,1))
    norm = norm/np.tile(ranges,(num,1))
    norm_para = np.vstack((minVals,ranges))
    labels = data[:,d_in:]
    trainData = norm[0:num*3/4+1].tolist()
    trainLabel = labels[0:num*3/4+1].tolist()
    testData = norm[num*3/4+1:].tolist()
    testLabel = labels[num*3/4+1:].tolist()
    np.savetxt('data/'+data_name+'_train_data.csv', trainData, delimiter=",")
    np.savetxt('data/'+data_name+'_train_label.csv', trainLabel, delimiter=",")
    np.savetxt('data/'+data_name+'_test_data.csv', testData, delimiter=",")
    np.savetxt('data/'+data_name+'_test_label.csv', testLabel, delimiter=",")
    return pickle.dumps(norm_para)

def train(lmodel, max_iter, loss):
    data_path = lmodel.data.data_file.name
    data_name = data_path[data_path.find('/')+1:data_path.rfind('.')]
    num_unit_tmp = str(lmodel.num_unit).split()
    num_unit = ''
    for each in num_unit_tmp:
        num_unit = num_unit + each + ' '
    num_unit = num_unit.strip().replace(' ','k')
    starttime = datetime.datetime.now()
    call(["~/mxnet/tools/launch.py -n 2 -H hosts python train.py --kv-store dist_sync"+\
    " --data-name "+str(data_name)+" --d-in "+str(lmodel.dimension_in)+" --d-out "+ str(lmodel.dimension_out)+" --"+\
    "num-layer "+str(lmodel.num_layer)+" --num-unit "+str(num_unit)+" --lr "+str(lmodel.lr)+" --batch-size "+str(lmodel.num_batch)+" --"+\
    "num-epochs "+str(max_iter)+" --loss "+str(loss)], shell = True)
    endtime = datetime.datetime.now()
    lmodel.time = (endtime - starttime).seconds
    with open('tmp_result1.json') as file:
        tmp_result = json.load(file)
        lmodel.num_iter = tmp_result['num_iter']
        lmodel.train_error = tmp_result['train_error']
        lmodel.test_error = tmp_result['test_error']
        lmodel.symbol = tmp_result['symbol']
    with open('tmp_result2.txt', mode='rb') as file:
        lmodel.arg_params = file.read()
    with open('tmp_result3.txt', mode='rb') as file:
        lmodel.aux_params = file.read()
    if os.path.isfile('tmp_result1.json'):
        os.remove('tmp_result1.json')
    if os.path.isfile('tmp_result2.txt'):
        os.remove('tmp_result2.txt')
    if os.path.isfile('tmp_result3.txt'):
        os.remove('tmp_result3.txt')


def predict(request, model_id, inputStr):
    lmodel = LearningModel.objects.get(id = model_id)
    arg_params = pickle.loads(lmodel.arg_params)
    aux_params = pickle.loads(lmodel.aux_params)
    net = mx.symbol.load_json(lmodel.symbol)
    model = mx.model.FeedForward(symbol = net,arg_params = arg_params, aux_params = aux_params)
    inputFloat = [float(item) for item in inputStr.split(';')]
    norm_para = pickle.loads(lmodel.norm_para)
    norm = inputFloat-norm_para[0]
    norm = norm/norm_para[1]
    norm = mx.nd.array([norm.tolist()])
    iter = mx.io.NDArrayIter(data=norm)
    label = model.predict(X = iter).tolist()
    result = ''
    for each in label[0]:
        result = result + str(each) + ' '
    result = result.strip()
    global count
    if str(model_id) in count.keys():
        count[str(model_id)] = count[str(model_id)] + 1
    return HttpResponse(result)

"""
def localPredict(request):
    inputStr = request.GET["inputStr"]
    model_id = request.GET["model_id"]
    lmodel = LearningModel.objects.get(id = model_id)
    arg_params = lmodel.arg_params
    aux_params = lmodel.aux_params
    net = lmodel.symbol
    model = mx.model.FeedForward(symbol = net)
    model.set_params(arg_params, aux_params)
    inputFloat = [float(item) for item in inputStr.split('/')]
    tmp = pickle.loads(lmodel.norm_para)
    norm = inputFloat-tmp[0]
    norm = norm/tmp[1]
    norm = mx.nd.array([norm.tolist()])
    iter = mx.io.NDArrayIter(data=norm)
    label = model.predict(X = iter).tolist()
    result = ''
    for each in label[0]:
        result = result + str(each) + ' '
    result = result.strip()
    return HttpResponse(result)
"""

def invoke(request):
    global sub_process
    global count
    global count_list
    model_id = request.GET["model_id"]
    topic_sub = request.GET["topic_sub"]
    topic_pub = request.GET["topic_pub"]
    if str(model_id) in sub_process.keys():
       return JsonResponse(["false"], safe=False)
    sub = Popen(["java", "com.common.subscribe.singleSubscribe", str(model_id), str(topic_sub), str(topic_pub)])
    sub_process[str(model_id)] = sub
    count[str(model_id)] = 0
    count_list[str(model_id)] = deque([0 for i in range(12)])
    return JsonResponse(["true"], safe=False)


def plot(request, model_id):
    global count
    global count_list
    if str(model_id) not in count.keys():
        count[str(model_id)] = 0
    if str(model_id) not in count_list.keys():
        count_list[str(model_id)] = deque([0 for i in range(12)])
    if request.is_ajax():
        tmp = count[str(model_id)]
        count[str(model_id)] = 0
        count_list[str(model_id)].pop()
        count_list[str(model_id)].appendleft(tmp)
        data = [each for each in count_list[str(model_id)]]
        return JsonResponse(data, safe=False)
    else:
        count[str(model_id)] = 0
    return render(request, "dl_manage/graph.html")


def stop(request, model_id):
    global sub_process
    global count
    global count_list
    count.pop(str(model_id), None)
    count_list.pop(str(model_id), None)
    if str(model_id) in sub_process.keys():
        sub_process[str(model_id)].terminate()
        sub_process.pop(str(model_id), None)
        return JsonResponse(["true"], safe=False)
    else:
        return JsonResponse(["false"], safe=False)