from django.contrib import admin
from .models import Category, Data, LearningModel

# Register your models here.
admin.site.register([Category, Data, LearningModel])
