# ML_Manage
This is a web-based management system that provides a user-friendly interface for users to manage a machine learning system. 

It is developed based on <b>Python Django</b> (as the web framework) and <b>Bootstrap</b> (for front-end design). The background machine learning framework is <b>MXNet</b>.

By using it, you can:
* Train learning models by inputting the parameters and uploading the training data on the web page
* Look up the existing models
* Edit the models
* Delete the models

## Installation:
### 1. For Windows

(1) Install Python

(2) Install Django and numpy

    pip install django
    pip install numpy

(3) Build MXNet

Go to <a href="http://mxnet.io/get_started/setup.html#build-mxnet-on-windows">this page</a>. Follow the instructions in the "Installing Pre-built Packages on Windows" section.

(4) Install the Python Package of MXNet

Open the README.txt file in the compressed file downloaded in step (3), and follow corresponding instructions.

(5) Run it

Open CMD and change the current directory to the project directory. Type the following command:

    cd mysite
    python manage.py runserver 0.0.0.0:8000
    
Then, open a browser. Type `localhost:8000/manage` and press Enter.

### 2. For Ubuntu

(1) Install Python (if not installed)

(2) Install Django and numpy

    sudo pip install django
    sudo pip install numpy

(3) Build MXNet
Go to <a href="http://mxnet.io/get_started/setup.html#quick-installation-on-ubuntu">this page</a>. Follow the instructions in the "Quick Installation on Ubuntu" section.

(4) Install the Python Package of MXNet
Go to <a href="http://mxnet.io/get_started/setup.html#install-the-mxnet-package-for-python">this page</a>. Follow the instructions in the "Install the MXNet Package for Python" section.

(5) Run it

Open Terminal and change the current directory to the project directory. Type the following command:

    cd mysite
    python manage.py runserver 0.0.0.0:8000
    
Then, open a browser. Type `localhost:8000/manage` and press Enter.
