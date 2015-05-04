
require "cord"

-- set buttons as inputs
storm.io.set_mode(storm.io.INPUT,   storm.io.D9, storm.io.D10, storm.io.D11)
-- enable internal resistor pullups (none on board)
storm.io.set_pull(storm.io.PULL_UP, storm.io.D9, storm.io.D10, storm.io.D11)
-- set leds as outputs
storm.io.set_mode(storm.io.OUTPUT,  storm.io.D2, storm.io.D3,  storm.io.D4)

-- this is a hack, poll button state every 50ms
storm.os.invokePeriodically(50*storm.os.MILLISECOND, function ()
    -- get button states
    local p1, p2, p3 = storm.io.get(storm.io.D9, storm.io.D10, storm.io.D11)
    -- write em to the LEDs (but invert them)
    storm.io.set(1-p1, storm.io.D2)
    storm.io.set(1-p2, storm.io.D3)
    storm.io.set(1-p3, storm.io.D4)
end)

cord.enter_loop()
