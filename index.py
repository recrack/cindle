import re
from subprocess import Popen, PIPE
    
prjRoot = "/home/beonit/work/"

# http://localhost/codeview/index.py/get_file?prj=android-platform&filename=system/core/Android.mk&linnum=1
def get_file(req):
    info = req.form
    prj = info.get('prj', None)
    if prj is None:
        return "errno 0 : prj is none"
    filename = info.get('filename', None)
    if filename is None:
        return "errno 1"
    linnum = info.get('linnum', '0')
    fileHandle = file( prjRoot + prj + "/" + filename )
    out = ""
    for line in fileHandle:
        out += line
    return out

# http://localhost/codeview/index.py/get_file_list?prj=android-platform&path=frameworks/base/core
def get_file_list(req):
    import os
    info = req.form
    prj = info.get( 'prj', None )
    if prj is None:
        return "errno 0 : prj is none"
    path = info.get( 'path', None )
    if path is None:
        return "errno 1 : path is noen"
    out = ""
    os.chdir( prjRoot + prj + "/" + path )
    f = os.popen("find -type f -maxdepth 1")
    out = ""
    for line in f.readlines():
        out += line[2:]
    return out

# http://localhost/codeview/index.py/get_dir_list?prj=android-platform&path=frameworks/base/core
def get_dir_list(req):
    import os
    info = req.form
    prj = info.get( 'prj', None )
    if prj is None:
        return "errno 0 : prj is none"
    path = info.get( 'path', None )
    if path is None:
        return "errno 1 : path is noen"
    out = ""
    os.chdir( prjRoot + prj + "/" + path )
    f = os.popen("ls -d */")
    out = ""
    for line in f.readlines():
        out += line
    return out

# http://localhost/codeview/index.py/cscope?prj=android-platform&method=-1&query=shutdown
def cscope(req):
    import os
    info = req.form
    prj = info.get( 'prj', None )
    if prj is None:
        return "errno 0 : prj is none"
    method = info.get( 'method', None )
    if method is None:
        return "errno 1 : method is empty"
    query = info.get( 'query', None )
    if query is None:
        return "errno 2 : query is empty"

    dbfile = prjRoot + prj + "/cscope.out"
    proc = Popen(["cscope", "-dL", "-f", dbfile, method, query],
                 stdin=PIPE,
                 stdout=PIPE,
                 cwd=prjRoot+prj)
    out = ""
    for line in proc.stdout.readlines():
        out += line
    return out
