import time
import urllib2
import sys
import msgpack
import socket
from smap.archiver.client import RepublishClient
from smap import driver, util


class riceCookerApp(driver.SmapDriver):

    def setup(self, opts):
        self.rate = float(opts.get('rate', 1))
        self.add_timeseries('/temp', 'unit', data_type='double')
        self.set_metadata('/temp', {
                          'Metadata/Description' : 'Application for rice cooker',
                          })

        self.archiverurl = opts.get('archiverurl','http://shell.storm.pm:8079')                       
        self.subscription = opts.get('subscription','Metadata/SourceName = "Rice Cooker Sensor"')
        self.r = RepublishClient(self.archiverurl, self.cb, restrict=self.subscription)               

    def cb(self, points):
        print points
        #curr_time = int(time.time())
        #self.add('/temp', curr_time)

    def start(self):
        self.r.connect()
        util.periodicSequentialCall(self.dummyData).start(1)

    def dummyData(self):
        print "hello"
        curr_time = int (time.time())
        self.add("/temp", curr_time, float(2))

    def stop(self):
     print "Quit"
     self.stopping = True
