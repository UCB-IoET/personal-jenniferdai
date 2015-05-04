require "cord"

storm.os.invokePeriodically(1*storm.os.SECOND, function()
	print ("hello world")
	end)

cord.enter_loop()
