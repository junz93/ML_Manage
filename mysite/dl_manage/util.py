import csv
import numpy as np
import mxnet as mx

def create(data_file, d_in, d_out):
    d_in = int(d_in)
    d_out = int(d_out)
    data = np.loadtxt(data_file)
    data_file = data_file[data_file.find('/')+1:data_file.rfind('.')]
    tmp_list = data.tolist()
    np.random.shuffle(tmp_list)
    data = np.array(tmp_list)
    num = data.shape[0]
    tmp = data[:,:d_in]
    minVals = tmp.min(0)
    maxVals = tmp.max(0)
    ranges = maxVals-minVals
    k=0
    for each in ranges:
        if each == 0:
            ranges[k] = 1
        k = k+1
    norm = tmp-np.tile(minVals,(num,1))
    norm = norm/np.tile(ranges,(num,1))
    norm_file = "norm/"+data_file+'_normData.txt'
    np.savetxt(norm_file,np.vstack((minVals,ranges)))
    labels = data[:,d_in:]
    trainData = norm[0:num*2/3+1].tolist()
    trainLabel = labels[0:num*2/3+1].tolist()
    testData = norm[num*2/3+1:].tolist()
    testLabel = labels[num*2/3+1:].tolist()
    csv_prefix = "csv/"+data_file
    with open(csv_prefix+'_train_data.csv','w') as f:
        f_csv = csv.writer(f)
        for each in trainData:
            f_csv.writerow(tuple(each))

    with open(csv_prefix+'_train_label.csv','w') as f:
        f_csv = csv.writer(f)
        for each in trainLabel:
            f_csv.writerow(tuple(each))

    with open(csv_prefix+'_test_data.csv','w') as f:
        f_csv = csv.writer(f)
        for each in testData:
            f_csv.writerow(tuple(each))

    with open(csv_prefix+'_test_label.csv','w') as f:
        f_csv = csv.writer(f)
        for each in testLabel:
            f_csv.writerow(tuple(each))


def get_error(model_id, prefix, num_epochs, d_in, d_out):
    label_test = mx.io.CSVIter(data_csv="csv/"+prefix+"_test_label.csv", data_shape=(int(d_out),), batch_size=1)
    label_test.reset()
    data_test = mx.io.CSVIter(data_csv="csv/"+prefix+"_test_data.csv", data_shape=(int(d_in),), batch_size=1)
    model = mx.model.FeedForward.load(prefix+"_"+str(model_id), int(num_epochs))
    result = model.predict(data_test)
    result=result.tolist()
    sum=0
    i=0
    for each in result:
        label_test.next()
        labels=label_test.getdata().asnumpy()[0]
        for label,item in zip(labels,each):
            sum+=abs((item-float(label))/float(label))
            i=i+1
    error_rate=sum/i
    return error_rate

