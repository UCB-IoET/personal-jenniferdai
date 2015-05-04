require "cord"

return {
    start = function()
        cord.new(function()
           io.write("\n\27[34;1mstormsh> \27[0m")
            while true do
                local txt = cord.await(storm.os.read_stdin)
                storm.os.stormshell(txt)
            end
        end)
    end
}

