require("storm") -- libraries for interfacing with the board and kernel
require("cord") -- scheduler / fiber library

----------------------------------------------
-- LED class
--   basic LED functions associated with a shield pin
--   assume cord.enter_loop() is active, as per stormsh
----------------------------------------------
local LED = {}

function LED:new(ledpin)
   assert(ledpin and storm.io[ledpin], "invalid pin spec")
   obj = {pin = ledpin}		-- initialize the new object
   setmetatable(obj, self)	-- associate class methods
   self.__index = self
   storm.io.set_mode(storm.io.OUTPUT, storm.io[ledpin])
   return obj
end

function LED:pin()
   return self.pin
end

function LED:on()
   storm.io.set(1,storm.io[self.pin])
end

function LED:off()
   storm.io.set(0,storm.io[self.pin])
end

-- Flash an LED pin repeatedly for a period of time
--    default duration is 10 ms
--    default repetition is 1
--    assumes cord.enter_loop() is in effect to schedule filaments
function LED:flash(times,duration)
   local pin = self.pin
   times = times or 1
   duration = duration or 10
   cord.new(function()
	       local i
	       for i = 1,times do
		  if i ~= 1 then		-- wait before flashing back on
		     cord.await(storm.os.invokeLater,50*storm.os.MILLISECOND)
		  end
		  storm.io.set(1,storm.io[pin]) -- turn it on
		  cord.await(storm.os.invokeLater,duration*storm.os.MILLISECOND)
		  storm.io.set(0,storm.io[pin]) -- turn it back off
	       end
	    end)
end

return LED

