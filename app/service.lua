require "cord" -- scheduler / fiber library
Toaster = require "toaster"

toaster = Toaster:new("D6")
toaster:init()
ipaddr = storm.os.getipaddr()
ipaddrs = string.format("%02x%02x:%02x%02x:%02x%02x:%02x%02x::%02x%02x:%02x%02x:%02x%02x:%02x%02x",
			ipaddr[0],
			ipaddr[1],ipaddr[2],ipaddr[3],ipaddr[4],
			ipaddr[5],ipaddr[6],ipaddr[7],ipaddr[8],	
			ipaddr[9],ipaddr[10],ipaddr[11],ipaddr[12],
			ipaddr[13],ipaddr[14],ipaddr[15])

print("ip addr", ipaddrs)
print("node id", storm.os.nodeid())

service_announce = 1525
service_invoke = 1526

function pt(x) for k,v in pairs(x) do print("", "", k,v) end end

function print_msg(msg)
   if (type(msg) ~= "table") then
	  print("Malformed service [type=%s]", type(msg))
	  return {}
   end

   for k,v in pairs(msg) do
	  if (k ~= "id") then
		 if type(v) ~= "table" then
			print("", k, v)
		 else
			print("", k)
			pt(v)
		 end
	  else
		 print("", k, v)
	  end
   end

   return msg
end

a_socket = storm.net.udpsocket(service_announce,
							function(payload, from, port)
							   print (string.format("from %s port %d",from,port))
							   local msg = storm.mp.unpack(payload)
							   msg = print_msg(msg)
							end)

local svc_manifest = {
   id="FirestormTroopers",
   turnOn={},
   turnOff={},
   setTemperature={t="degrees in celcius"},
   getTemperature={},
}

local svc_msg = storm.mp.pack(svc_manifest)
storm.os.invokePeriodically(2*storm.os.SECOND,
							function()
							   print ("SENDING ANNOUNCEMENT")
							   storm.net.sendto(
								  a_socket,
								  svc_msg,
								  "ff02::1",
								  service_announce)
							end)

storm.io.set_mode(storm.io.OUTPUT, storm.io.GP0)


b_socket = storm.net.udpsocket(service_invoke,
							function(payload, from, port)
							   print (string.format("invoke from %s port %d",from,port))
							   local msg = storm.mp.unpack(payload)
							   if (type(msg) ~= "table") then
								  print ("Invalid format [type=%s]", type(msg))
								  return
							   end

							   cmd = msg[1]
							   args = msg[2]
							   print("cmd", cmd)
							   print("args", args)
							   if (cmd == nil) then
								  return
							   end

							   if (args == nil) then
								  args = {}
							   elseif (type(args) ~= "table") then
								  args = {args}
							   end

							   if (cmd == "turnOn") then
								  toaster:on()
							   elseif (cmd == "turnOff") then
								toaster:off()
							   elseif (cmd == "setTemperature") then
								toaster:setTemp(args)
							   elseif (cmd == "getTemperature") then
								toaster:getTemp(args)
							   end
							end)

-- enable a shell
sh = require "stormsh"
sh.start()
cord.enter_loop() -- start event/sleep loop
