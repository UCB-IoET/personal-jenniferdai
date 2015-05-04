require("storm") -- libraries for interfacing with the board and kernel
require("cord") -- scheduler / fiber library
shield = require("starter") -- interfaces for resources on starter shield

print ("blink test ")

function blinker(color)
   local state = 0
   return function ()
      if state  == 1 then 
	 print ("blink on", state)
	 shield.LED.on(color)
      else 
	 print ("blink off", state)
	 shield.LED.off(color)
      end
      state=1-state
   end
end

shield.LED.start()		-- enable LEDs
storm.os.invokePeriodically(1*storm.os.SECOND, blinker("red"))
cord.enter_loop() -- start event/sleep loop

