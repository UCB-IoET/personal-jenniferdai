require("storm") -- libraries for interfacing with the board and kernel
require("cord") -- scheduler / fiber library
print ("Base  test ")

sh = require "stormsh"
LED = require("led")
Button = require("button")

-- blue LED plugged into LED board attached to base shield pin D2
-- button plugged into base shield pin D3
blue = LED:new("D2")
btn = Button:new("D3")

blue:flash(5)

count = 1
btn:whenever("RISING", function() 
		print("BTN", btn:pressed(), count) 
		blue:flash(count % 4)
		count = count+1
		       end)

-- start a shell so you can play more

-- start a coroutine that provides a REPL
sh.start()

-- enter the main event loop. This puts the processor to sleep
-- in between events
cord.enter_loop()
