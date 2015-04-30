import os
import socket
import msgpack
import uuid
import time
import requests
import json

# This is what we are listening on for messages
UDP_IP ="::" #all IPs
UDP_PORT = 5080

# Note we are creating an INET6 (IPv6) socket
sock = socket.socket(socket.AF_INET6,
    socket.SOCK_DGRAM)
sock.bind((UDP_IP, UDP_PORT))
# These are the different timeseries paths that our middleware is publishing to
ts_paths = ['acc_x','acc_y','acc_z','mag_x','mag_y','mag_z','pir']

smap_dict = {'/temp':{'uuid': "6f6b3306-ef7f-11e4-bc83-0001c0158419", 'Metadata': {'SourceName': 'test data'}, 'Properties': {'UnitofTime': 'ms', 'UnitofMeasure': 'count'}}}

while True: # serve forever
    data, addr = sock.recvfrom(1024) # buffer size is 1024 bytes
    # if addr[0][-4:] not in addresses:
    #    continue
    msg = msgpack.unpackb(data)
    print "msg"
    print msg
    smap_dict['/temp']['Readings'] = [[int(time.time()*1000), float(msg)]]
    try:
        # We use the IPv4 address because requests sometimes defaults to ipv6 if you use DNS and 
        # the archiver doesn't support that. This is a hack
        x = requests.post('http://54.215.11.207:8079/add/xyz', data=json.dumps(smap_dict))
        #x = requests.post('http://pantry.cs.berkeley.edu:8079/add/xyz', data=json.dumps(smap))
        print x
    except Exception as e:
        print e
