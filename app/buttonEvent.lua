require("storm") -- libraries for interfacing with the board and kernel
require("cord") -- scheduler / fiber library
shield = require("starter") -- interfaces for resources on starter shield

print ("Button Event test ")

shield.Button.start()		-- enable LEDs
shield.LED.start()

leds = {"blue","green","red"}

function buttonAction(button,mode)
   return function() 
      print("button", button, mode) 
   end
end

shield.Button.whenever(1, "FALLING", buttonAction(1,"down"))
shield.Button.whenever(2, "RISING",  buttonAction(2,"up"))
shield.Button.whenever(3, "CHANGE",  buttonAction(3,"change"))

cord.enter_loop() -- start event/sleep loop



