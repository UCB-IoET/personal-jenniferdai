require("storm") -- libraries for interfacing with the board and kernel
require("cord") -- scheduler / fiber library
shield = require("starter") -- interfaces for resources on starter shield

print ("buzz and blink test ")

function beeper()
   local state = 0
   return function ()
      -- alternate buzz and quiet, shifting pitch each time it goes on
      if state % 2 == 1 then 
	 print ("beep on", state)
	 shield.Buzz.go((state/2)%4) 
      else 
	 print ("beep off", state)
	 shield.Buzz.stop()
      end
      state=state+1
   end
end

shield.LED.start()		-- enable LEDs
shield.Buzz.go()		-- enable buzzer
storm.os.invokePeriodically(2*storm.os.SECOND, beeper())
storm.os.invokePeriodically(1*storm.os.SECOND, 
			    function() shield.LED.flash("green", 20) end)
cord.enter_loop() -- start event/sleep loop

