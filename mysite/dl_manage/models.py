# -*- coding: utf-8 -*-

from __future__ import unicode_literals

from django.db import models


# Create your models here.


class Category(models.Model):
    type_1 = models.CharField(max_length=30)
    type_2 = models.CharField(max_length=30)

    def __str__(self):
        return self.type_1 + "." + self.type_2


class Data(models.Model):
    data_file = models.FileField(upload_to="data/")
    dimension_in = models.IntegerField()
    dimension_out = models.IntegerField()

    def __str__(self):
        return str(self.data_file)


class LearningModel(models.Model):
    model_name = models.CharField(max_length=50)
    type = models.ForeignKey(Category, on_delete=models.CASCADE)
    data = models.ForeignKey(Data, null=True, on_delete=models.SET_NULL)
    dimension_in = models.IntegerField()
    dimension_out = models.IntegerField()
    data_name = models.CharField(max_length=255)
    num_layer = models.IntegerField(default=2)
    num_unit = models.CharField(max_length=255)
    lr = models.FloatField()
    num_iter = models.IntegerField()
    num_batch = models.IntegerField()
    arg_params = models.BinaryField(null=True)
    aux_params = models.BinaryField(null=True)
    symbol = models.TextField(null=True)
    norm_para = models.BinaryField(null=True)
    train_error = models.FloatField(null=True)
    test_error = models.FloatField(null=True)
    time = models.FloatField(default=0.0)
    comment = models.TextField(blank=True)


    def get_intro(self):
        return u"模型ID：%s\n模型名称：%s\n一级分类：%s\n二级分类：%s\n特征个数：%s\n结果个数：%s\n网络层数：%s\n各层神经元个数：%s\n" \
               u"学习率：%s\n迭代次数：%s\nBatch：%s\n训练时间：%s\n训练误差：%s\n测试误差：%s\n备注：\n%s\n" \
               % (self.id, self.model_name, self.type.type_1, self.type.type_2, self.dimension_in, self.dimension_out,
                  self.num_layer, self.num_unit, self.lr, self.num_iter, self.num_batch, self.time, self.train_error, self.test_error, self.comment)


    def __str__(self):
        return str(self.type) + "." + self.model_name
