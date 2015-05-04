require("storm") -- libraries for interfacing with the board and kernel
require("cord") -- scheduler / fiber library

----------------------------------------------
-- TEMP class
--   basic TEMP functions associated with a shield pin
--   assume cord.enter_loop() is active, as per stormsh
----------------------------------------------
local TEMP = {}

function TEMP:new(temppin)
   assert(temppin and storm.io[temppin], "invalid pin spec")
   obj = {pin = temppin}		-- initialize the new object
   setmetatable(obj, self)	-- associate class methods
   self.__index = self
   storm.io.set_mode(storm.io.INPUT, storm.io[temppin])
   return obj
end

function TEMP:pin()
   return self.pin
end

function TEMP:readTemp()
   temp_constant = 1 --XXX: Figure this out by testing...
   return storm.io.get(storm.io[self.pin]) * temp_constant
end

return TEMP
