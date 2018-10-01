# -*- coding: utf-8 -*-
# @Author: Administrator
# @Date:   2018-04-02 18:43:31
# @Last Modified by:   a1png
# @Last Modified time: 2018-04-21 22:13:03
import os
import filecmp
from subprocess import check_output

check_output("javac -classpath ../ vc.java")
with open('parser.test', 'w') as w: 
    for f in os.listdir('Parser'):
        if f.endswith('vc'):
            w.write(f+'\n')
            check_output("java -classpath ../ VC.vc %s" % f)
            # s = filecmp.cmp('Parser/%s'%f.replace('vc', 'sol'), 'Parser/%s'%(f+'u'))
            # command = "diff Parser/%s Parser/%s"% (f.replace('vc', 'sol'), (f+'u'))
            
            #s = check_output(command)
            print((f.replace('vc', 'sol'), f+'u'), open('Parser/' + f.replace('vc', 'sol')).read() == open('Parser/' + f+'u').read())
            