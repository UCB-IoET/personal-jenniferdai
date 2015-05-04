require "cord"
sh = require "stormsh"

-- start a coroutine that provides a REPL
sh.start()

-- enter the main event loop. This puts the processor to sleep
-- in between events
cord.enter_loop()
