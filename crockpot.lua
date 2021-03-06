require "cord"
require "math"
require "svcd"
require "storm"
sh = require "stormsh" 
TEMP = require "extTempSensor" 

----------------initialize global variables--------------------------

boolean = 0
temperature = 0

storm.io.set_mode(storm.io.OUTPUT, storm.io.D4)
print("hello")

function rc_on()
	storm.io.set(1, storm.io.D4)
end

function rc_off()
	storm.io.set(0, storm.io.D4)
end
-----------------service---------------------------------------------------

SVCD.init("ricecooker", function()
    print("starting")
    SVCD.add_service(0x3003)

    SVCD.add_attribute(0x3003, 0x4005, function(pay, srcip, srcport)
        local ps = storm.array.fromstr(pay)
        if ps:get(1) ~= nil then
            boolean = ps:get(1)
            print("boolean received", boolean)
	    if boolean == 1 then
		rc_on()
	    else
		rc_off()
	    end
            print("got a request to switch the cooker")
        end
    end)

    --attribute 2, notifies app the current cooker temperature every 3 seconds---
    SVCD.add_attribute(0x3003, 0x4006, function(pay, srcip, srcport)
        
    end)
end)

------------------temp sensor---------------------------
irTemp = TEMP:new()
shellip = "2001:470:66:3f9::2"
sendsock = storm.net.udpsocket(5409, function() end)
cord.new(function()
	cord.await(storm.os.invokeLater, storm.os.SECOND*5)
	irTemp:init()
	while true do
		local temp = irTemp:getIRTemp()
		print(temp)
		SVCD.notify(0x3003, 0x4006, temp)
		storm.net.sendto(sendsock, storm.mp.pack(temp), shellip, 5080)
		cord.await(storm.os.invokeLater, storm.os.MILLISECOND*500)
	end
end)
sh.start()
cord.enter_loop()
