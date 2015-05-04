require("storm") -- libraries for interfacing with the board and kernel
require("cord") -- scheduler / fiber library
shield = require("starter") -- interfaces for resources on starter shield

print ("Button loop test ")

shield.Button.start()		-- enable LEDs
shield.LED.start()
leds = {"blue","green","red"}

function armButton()
   print("Wait on button", 2)
   shield.Button.wait(button)
   print("Got it")
end

-- Upon pushing button 1, wait till button two
print("push button 1")
shield.Button.when(1, "FALLING", armButton)

cord.enter_loop() -- start event/sleep loop



