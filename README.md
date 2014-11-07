Python Development Environment Configuration
=================

[TOC]

Introduction
--------------

This Tutorial shows how [__Python__](https://www.python.org/) should be configured so the different scripts and plugins found in _Wallapop_  work correctly.

Changes
----------

7/11/2014 - First version of the document

Python
--------

 [__Python__](https://www.python.org/) is a scripting programming language, very light and fast for prototyping. It's currently being used in several scripts to automatize different processes such as downloading the different languages of the _Wallapop Android app_ from [_Webtranslateit_](www.webtranslateit.com)

###  Download

The minimum python version supported is 2.7. The recommendation is to install the latest stable python version, being today [__Python 3.4.2__](https://www.python.org/downloads/release/python-342/) with release date of 10/13/2014

> Obvious note: Be sure to download the correct installation file (So, do not download the source codes of python)

###  Installation

#### Windows

First
: Install python following the installer indications. 

Second
: Check that __Python home__ is set in the system environment variable _Path_. Open __cmd__ and write

```
C:\ > path
```

Something like this response should be printed by __cmd__

```
C:\ > 
C:\Program Files\StupidGame; F:\gsutil;C:\Python34;C:\ProgramData\chocolatey\bin;
```

Look for the Python folder. If found, skip to section [PIP](#pip).

To modify the Global Path follow this [tutorial](http://www.computerhope.com/issues/ch000549.htm)

#### Linux

Usually Linux comes with python already installed. Check if update is required. Otherwise skip to [pip section](#pip)

#### Mac

First
: Install python following the installer

Second
: : Check that __Python home__ is set in PATH. Try to run python from terminal

```
> python --version
Python 3.4.0
```

If python not found, __Python home__ must be added to the environment variable PATH. There are different ways to add the folder to PATH. I suggest to do it in your own way. However, if you don't know how to do it, follow this method obtained from this [link](http://stackoverflow.com/questions/5545715/how-do-i-add-usr-local-git-bin-to-the-path-on-mac-osx).

Add the following line to __~/.bash_profile__:

```
export PATH=/usr/local/git/bin:$PATH
```

> __TIP__: Everybody should know how to install Python. Put your shit together man.


<a name="pip"> PIP
-----------------------

[Pip](https://pypi.python.org/pypi/pip) is a packet manager for Python, similar to apt-get for Linux. Pip will simplify enormously the installation of dependencies. 

###Installation

Download [this file (get-pip.py)](https://bootstrap.pypa.io/get-pip.py) and execute:


```
c:>python get-pip.py
```

### Install python libraries

Thanks to pip installing any python libraries is as easy as doing this:

```
c:> pip install library-name
```


Python Libraries
-------------------
As in 7/11/2014 the following libraries are required to execute wallapop scripts:


> __Library name__:
> : requests


