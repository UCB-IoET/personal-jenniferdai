import time
import urllib2
import sys
import msgpack
import socket
from smap import driver, util


class ledApp(driver.SmapDriver):

    def setup(self, opts):
        self.rate = float(opts.get('rate', 1))
        self.add_timeseries('/temp', 'unit', data_type='double')
        self.set_metadata('/rice_app', {
                          'Metadata/Description' : 'Application for rice cooker',
                          })

        self.archiverurl = opts.get('archiverurl','http://shell.storm.pm:8079')            
        self.subscription = opts.get('subscription','Metadata/SourceName = "Rice Cooker Sensor"')
        self.r = RepublishClient(self.archiverurl, self.cb, restrict=self.subscription)    

    def cb(self, points):
        print points
        curr_time = time.time()
        self.add('/temp',curr_time, index)

    def start(self):
        self.r.connect()

    def stop(self):
     print "Quit"
     self.stopping = True
