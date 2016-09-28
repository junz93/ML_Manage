from django.conf.urls import url
from . import views

app_name = "dl_manage"
urlpatterns = [
    url(r'^$', views.index, name="index"),
    url(r'^add/$', views.add, name="add"),
    url(r'^save/$', views.save, name="save"),
    url(r'^delete/$', views.delete, name="delete"),
    url(r'^loadmodels/$', views.loadmodels, name="loadmodels"),
    url(r'^fillmodel/$', views.fillmodel, name="fillmodel"),
    url(r'^id/(?P<model_id>[0-9]+)/data/(?P<inputStr>.+)$', views.predict, name="predict"),
    url(r'^plot/(?P<model_id>[0-9]+)$', views.plot, name="plot"),
    url(r'^stop/(?P<model_id>[0-9]+)$', views.stop, name="stop"),
    url(r'^invoke/$', views.invoke, name="invoke")
]
