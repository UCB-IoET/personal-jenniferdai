require("storm") -- libraries for interfacing with the board and kernel
require("cord") -- scheduler / fiber library
shield = require("starter") -- interfaces for resources on starter shield

print ("Button test ")

shield.Button.start()		-- enable LEDs

function buttons()
   print(shield.Button.pressed(1),shield.Button.pressed(2),shield.Button.pressed(3))
end

storm.os.invokePeriodically(1*storm.os.SECOND, buttons)
cord.enter_loop() -- start event/sleep loop



