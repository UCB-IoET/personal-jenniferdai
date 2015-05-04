require("storm") -- libraries for interfacing with the board and kernel
require("cord") -- scheduler / fiber library
ADC = require ("adc") -- adc library

----------------------------------------------
-- TEMP class
--   basic TEMP functions associated with a shield pin
--   assume cord.enter_loop() is active, as per stormsh
----------------------------------------------
local TEMP = {}

function TEMP:new()
   s = ADC:new()
   s:init()
   obj = {sensor = s}		-- initialize the new object
   setmetatable(obj, self)	-- associate class methods
   self.__index = self
   return obj
end

function TEMP:readTemp()
    return self.sensor:get()
   -- temp_constant = 1 --XXX: Figure this out by testing...
   -- return storm.io.get(storm.io[self.pin]) * temp_constant
end

return TEMP
