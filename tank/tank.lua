--[[
   echo client as server
   currently set up so you should start one or another functionality at the
   stormshell

--]]

require "cord" -- scheduler / fiber library

ipaddr = storm.os.getipaddr()
ipaddrs = string.format("%02x%02x:%02x%02x:%02x%02x:%02x%02x::%02x%02x:%02x%02x:%02x%02x:%02x%02x",
			ipaddr[0],
			ipaddr[1],ipaddr[2],ipaddr[3],ipaddr[4],
			ipaddr[5],ipaddr[6],ipaddr[7],ipaddr[8],	
			ipaddr[9],ipaddr[10],ipaddr[11],ipaddr[12],
			ipaddr[13],ipaddr[14],ipaddr[15])

print("ip addr", ipaddrs)
print("node id", storm.os.nodeid())

tank_port = 42069

relay = storm.io.D2
buzzer = storm.io.D3

storm.io.set_mode(storm.io.OUTPUT, relay, buzzer)

-- create echo server as handler
server = function()
   ssock = storm.net.udpsocket(tank_port, 
	             function(payload, from, port)
					print (string.format("from %s port %d",from,port))
					up = storm.mp.unpack(payload)
					print (string.format("action: %s", up.action))
					if up.action == "go" then
					   go()
					   storm.net.sendto(ssock, payload, from, port)
					elseif up.action == "stop" then
					   stop()
					   storm.net.sendto(ssock, payload, from, port)
					elseif up.action == "beep" then
					   beep()
					   storm.net.sendto(ssock, payload, from, port)
					else
					   local reply = {}
					   reply.action = "NAK"
					   local packed = storm.mp.pack(reply)
					   storm.net.sendto(ssock, packed, from, port)
					end
				 end)
end

go = function()
   storm.io.set(1, relay)
end

stop = function()
   storm.io.set(0, relay)
end

beep = function()
   cord.new(function()
			   storm.io.set(1, buzzer)
			   cord.await(storm.os.invokeLater,
						  500*storm.os.MILLISECOND)
			   storm.io.set(0, buzzer)
			end)
end

server()			-- every node runs the echo server

-- enable a shell
sh = require "stormsh"
sh.start()
cord.enter_loop() -- start event/sleep loop
