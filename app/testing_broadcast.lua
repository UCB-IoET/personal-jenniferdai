require "cord"
-- sock = storm.net.udpsocket(1525, function(payload, from, port) end)
local svc_manifest = {id="what"}              
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

a_socket = storm.net.udpsocket(1525,
							function(payload, from, port)
							   print (string.format("from %s port %d",from,port))
							   local msg = storm.mp.unpack(payload)
							   msg = print_msg(msg)
							end)
local msg = storm.mp.pack(svc_manifest)
storm.os.invokePeriodically(5*storm.os.SECOND, function()
       		storm.net.sendto(a_socket, msg, "ff02::1", 1525)
	end)

local svc_invoke_off = {
     "setOvenState",
     {false},
}
local svc_invoke_on = {
     "setOvenState",
     {true},
}
local onMsg = storm.mp.pack(svc_invoke_on)
local offMsg = storm.mp.pack(svc_invoke_off)
function toggleState(args)
	print("i want to turn the oven on")
	if args then
	     storm.net.sendto(a_socket, onMsg, "ff02::1", 1526) 
        else
	     storm.net.sendto(a_socket, offMsg, "ff02::1", 1526) 
        end
end
sh = require "stormsh"
sh.start()
cord.enter_loop()

