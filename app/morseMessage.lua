require("storm") -- libraries for interfacing with the board and kernel
require("cord") -- scheduler / fiber library
shield = require("starter") -- interfaces for resources on starter shield
morse = require("morse") -- interfaces for resources on starter shield

print ("Morse messages")

shield.Button.start()
shield.Button.whenever(3, "RISING", function()
						  local t = morse.send("NOCTURNAL", storm.io.D6, 200)
						  shield.Button.when(1, "RISING", t.stop)
									end)

cord.enter_loop() -- start event/sleep loop



