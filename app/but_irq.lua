
require "cord"

-- set buttons as inputs
storm.io.set_mode(storm.io.INPUT,   storm.io.D9, storm.io.D10, storm.io.D11)
-- enable internal resistor pullups (none on board)
storm.io.set_pull(storm.io.PULL_UP, storm.io.D9, storm.io.D10, storm.io.D11)
-- set leds as outputs
storm.io.set_mode(storm.io.OUTPUT,  storm.io.D2, storm.io.D3,  storm.io.D4)


-- button one toggles blue
local b1irq = storm.io.watch_all(storm.io.FALLING, storm.io.D9, function()
    storm.io.set(storm.io.TOGGLE, storm.io["D2"])
end)


-- button two toggles green, but should only occur once
function toggle_green()
    -- io.TOGGLE is new as of kernel 1.3, does what you expect
    storm.io.set(storm.io.TOGGLE, storm.io.D3)
    -- another new feature is to get the pin drive value, so this
    -- would do the same. note the getD. get() still only works for
    -- input mode as it reads from the schmidt trigger which is only
    -- enabled when in input mode.
    -- storm.io.set(1-storm.io.getd(storm.io.D3),storm.io.D3)
end
-- you also get io.RISING and io.CHANGE (both edges)
storm.io.watch_single(storm.io.FALLING, storm.io.D10, toggle_green)


-- button three cancels button one irq, and reregisters button 2
-- note this is "test grade" as registering multiple callbacks for
-- the same pin is frowned upon (as it leaks memory). Pressing 3
-- without pressing two does just that.
storm.io.watch_all(storm.io.FALLING, storm.io.D11, function()
    storm.io.watch_single(storm.io.FALLING, storm.io.D10, toggle_green)
    if b1irq ~= nil then storm.io.cancel_watch(b1irq)
        b1irq = nil
    end
end)

cord.enter_loop()
