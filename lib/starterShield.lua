----------------------------------------------
-- Starter Shield Module
--
-- Provides a module for each resource on the starter shield
-- in a cord-based concurrency model
-- and mapping to lower level abstraction provided
-- by storm.io @ toolchains/storm_elua/src/platform/storm/libstorm.c
----------------------------------------------

require("storm") -- libraries for interfacing with the board and kernel
require("cord") -- scheduler / fiber library
----------------------------------------------
-- Shield module for starter shield
----------------------------------------------
local shield = {}

----------------------------------------------
-- LED module
-- provide basic LED functions
----------------------------------------------
local LED = {}

LED.pins = {["blue"]="D2",["green"]="D3",["red"]="D4",["red2"]="D5"}

LED.start = function()
-- configure LED pins for output
   storm.io.set_mode(storm.io.OUTPUT, storm.io.D2, 
		     storm.io.D3, 
		     storm.io.D4,
		     storm.io.D5)
end

LED.stop = function()
-- configure pins to a low power state
end

-- LED color functions
-- These should rarely be used as an active LED burns a lot of power
LED.on = function(color)
   storm.io.set(1,storm.io[LED.pins[color]])
end
LED.off = function(color)
   storm.io.set(0,storm.io[LED.pins[color]])
end

-- Flash an LED pin for a period of time
--    unspecified duration is default of 10 ms
--    this is dull for green, but bright for read and blue
--    assumes cord.enter_loop() is in effect to schedule filaments
LED.flash=function(color,duration)
   local pin = LED.pins[color] or LED.pins["red2"]
   duration = duration or 10
   storm.io.set(1,storm.io[pin])
   storm.os.invokeLater(duration*storm.os.MILLISECOND,
			function() 
			   storm.io.set(0,storm.io[pin]) 
			end)
end

----------------------------------------------
-- Buzz module
-- provide basic buzzer functions
----------------------------------------------
local Buzz = {}

Buzz.run = nil
Buzz.go = function(delay)
   delay = delay or 0
   -- configure buzzer pin for output
   storm.io.set_mode(storm.io.OUTPUT, storm.io.D6)
   Buzz.run = true
   -- create buzzer filament and run till stopped externally
   -- this demonstrates the await pattern in which
   -- the filiment is suspended until an asynchronous call 
   -- completes
   cord.new(function()
	       while Buzz.run do
		  storm.io.set(1,storm.io.D6)
		  storm.io.set(0,storm.io.D6)	       
		  if (delay == 0) then cord.yield()
		  else cord.await(storm.os.invokeLater, 
				  delay*storm.os.MILLISECOND)
		  end
	       end
	    end)
end

Buzz.stop = function()
   print ("Buzz.stop")
   Buzz.run = false		-- stop Buzz.go partner
-- configure pins to a low power state
end

----------------------------------------------
-- Button module
-- provide basic button functions
----------------------------------------------
local Button = {}

Button.pins = {"D9","D10","D11"}

Button.start = function() 
   -- set buttons as inputs
   storm.io.set_mode(storm.io.INPUT,   
		     storm.io.D9, storm.io.D10, storm.io.D11)
   -- enable internal resistor pullups (none on board)
   storm.io.set_pull(storm.io.PULL_UP, 
		     storm.io.D9, storm.io.D10, storm.io.D11)
end

-- Get the current state of the button
-- can be used when poling buttons
Button.pressed = function(button) 
   return 1-storm.io.get(storm.io[Button.pins[button]]) 
end

-------------------
-- Button events
-- each registers a call back on a particular transition of a button
-- valid transitions are:
--   FALLING - when a button is pressed
--   RISING - when it is released
--   CHANGE - either case
-- Only one transition can be in effect for a button
-- must be used with cord.enter_loop
-- none of these are debounced.
-------------------
Button.whenever = function(button, transition, action)
   -- register call back to fire when button is pressed
   local pin = Button.pins[button]
   storm.io.watch_all(storm.io[transition], storm.io[pin], action)
end

Button.when = function(button, transition, action)
   -- register call back to fire when button is pressed
   local pin = Button.pins[button]
   storm.io.watch_single(storm.io[transition], storm.io[pin], action)
end

Button.wait = function(button)
-- Wait on a button press
--   suspend execution of the filament
--   resume and return when transition occurs
-- DEC: this doesn't quite work.  Return to it
   local pin = Button.pins[button]
   cord.new(function()
	       cord.await(storm.io.watch_single,
			  storm.io.FALLING, 
			  storm.io[pin])
	    end)
end

----------------------------------------------
shield.LED = LED
shield.Buzz = Buzz
shield.Button = Button
return shield


