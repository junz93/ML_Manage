import csv
import numpy as np
import mxnet as mx
import logging
import argparse
import json
import pickle

parser = argparse.ArgumentParser(description='train an image classifer on mnist')
parser.add_argument('--data-name', type=str, help='none')
parser.add_argument('--d-in', type=int, help='none')
parser.add_argument('--d-out', type=int, help='none')
parser.add_argument('--num-layer', type=int, help='none')
parser.add_argument('--num-unit', type=str, help='none')
parser.add_argument('--loss', type=float, help='none')
parser.add_argument('--gpus', type=str,
                    help='the gpus will be used, e.g "0,1,2,3"')
parser.add_argument('--num-examples', type=int, default=1000,
                    help='the number of training examples')
parser.add_argument('--batch-size', type=int, default=10000,
                    help='the batch size')
parser.add_argument('--lr', type=float, default=.1,
                    help='the initial learning rate')
parser.add_argument('--model-prefix', type=str,
                    help='the prefix of the model to load/save')
parser.add_argument('--num-epochs', type=int, default=1000,
                    help='the number of training epochs')
parser.add_argument('--load-epoch', type=int,
                    help="load the model on an epoch using the model-prefix")
parser.add_argument('--kv-store', type=str, default='local',
                    help='the kvstore type')
parser.add_argument('--lr-factor', type=float, default=1,
                    help='times the lr with a factor for every lr-factor-epoch epoch')
parser.add_argument('--lr-factor-epoch', type=float, default=1,
                    help='the number of epoch to factor the lr, could be .5')

args = parser.parse_args()

data_name = args.data_name
data_train = mx.io.CSVIter(data_csv='data/'+data_name+'_train_data.csv', data_shape=(args.d_in,),label_csv='data/'+data_name+'_train_label.csv',label_shape=(args.d_out,),batch_size=args.batch_size)

data_test = mx.io.CSVIter(data_csv='data/'+data_name+'_test_data.csv', data_shape=(args.d_in,),label_csv='data/'+data_name+'_test_label.csv', label_shape=(args.d_out,),batch_size=args.batch_size)

args.num_unit = args.num_unit.split('k')
fc={}
act={}
data = mx.symbol.Variable('data')
act[0] = data
for x in range(1,args.num_layer-1):
    fc[x] = mx.symbol.FullyConnected(data = act[x-1], name="fc%s" % str(x), num_hidden=int(args.num_unit[x]))
    act[x] = mx.symbol.Activation(data = fc[x], name="relu%s" % str(x), act_type="relu")
fc[args.num_layer-1]  = mx.symbol.FullyConnected(data = act[args.num_layer-2], name="fc%s" % str(args.num_layer-1), num_hidden=int(args.num_unit[args.num_layer-1]))
net = mx.symbol.LinearRegressionOutput(fc[args.num_layer-1], name='softmax')

kv = mx.kvstore.create(args.kv_store)

head = '%(asctime)-15s Node[' + str(kv.rank) + '] %(message)s'
logging.basicConfig(level=logging.DEBUG, format=head)
logging.info('start with arguments %s', args)

devs = mx.cpu() if args.gpus is None else [
    mx.gpu(int(i)) for i in args.gpus.split(',')]

if 'local' in kv.type and (
        args.gpus is None or len(args.gpus.split(',')) is 1):
    kv = None

model=mx.model.FeedForward(
symbol=net,
num_epoch=args.num_epochs,
learning_rate=args.lr,
momentum=0.9)

num_iter, train_error = model.fit(loss=args.loss, X=data_train, eval_metric=mx.metric.MRE(), kvstore=kv)
test_error = model.score(data_test, mx.metric.MRE())
symbol = net.tojson()
arg_params = model.arg_params
aux_params = model.aux_params

tmp1 = {'num_iter' : num_iter, 'train_error' : train_error, 'test_error' : test_error, 'symbol' : symbol}
with open('tmp_result1.json', 'w') as file:
    json.dump(tmp1, file)
with open('tmp_result2.txt', 'wb') as file:
    pickle.dump(arg_params, file)
with open('tmp_result3.txt', 'wb') as file:
    pickle.dump(aux_params, file)

