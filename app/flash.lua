require("storm") -- libraries for interfacing with the board and kernel
require("cord") -- scheduler / fiber library
shield = require("starter") -- interfaces for resources on starter shield

print ("LED flash test ")

shield.LED.start()		-- enable LEDs
storm.os.invokePeriodically(1*storm.os.SECOND, 
			    function() shield.LED.flash("red2") end)
storm.os.invokePeriodically(2*storm.os.SECOND, 
			    function() shield.LED.flash("red") end)
storm.os.invokePeriodically(3*storm.os.SECOND, 
			    function() shield.LED.flash("green") end)
storm.os.invokePeriodically(5*storm.os.SECOND, 
			    function() shield.LED.flash("blue") end)
cord.enter_loop() -- start event/sleep loop



