----------------------------------------------
-- Morse Code module
----------------------------------------------

require("storm") -- libraries for interfacing with the board and kernel
require("cord") -- scheduler / fiber library
----------------------------------------------
-- Morse code module
----------------------------------------------
local morse = {}

Alphabet = {
   ["A"] = {false,true},
   ["B"] = {true,false,false,false},
   ["C"] = {true,false,true,false},
   ["D"] = {true,false,false},
   ["E"] = {false},
   ["F"] = {false,false,true,false},
   ["G"] = {true,true,false},
   ["H"] = {false,false,false,false},
   ["I"] = {false,false},
   ["J"] = {false,true,true,true},
   ["K"] = {true,false,true},
   ["L"] = {false,true,false,false},
   ["M"] = {true,true},
   ["N"] = {true,false},
   ["O"] = {true,true,true},
   ["P"] = {false,true,true,false},
   ["Q"] = {true,true,false,true},
   ["R"] = {false,true,false},
   ["S"] = {false,false,false},
   ["T"] = {true},
   ['U'] = {false,false,true},
   ['V'] = {false,false,false,true},
   ['W'] = {false,true,true},
   ['X'] = {true,false,false,true},
   ['Y'] = {true,false,true,true},
   ['Z'] = {true,true,false,false},
   [" "] = {nil},
}

morse.send = function(message, pin, delay)
   local res = {}
   local stop = false
   res.stop = function()
   	  stop = true
   end

   storm.io.set_mode(storm.io.OUTPUT, pin)

   cord.new(
	  function()
		 for i = 1, #message do
			local c = message:sub(i,i)
			for j=1, #Alphabet[c] do
			   if j ~= 1 then
					 storm.io.set(0, pin)
					 if (stop) then
					 	return
					 end
					 cord.await(storm.os.invokeLater,
								delay*storm.os.MILLISECOND)
			   end
			   if (stop) then
			   	  return
			   end
			   entry = Alphabet[c][j]
			   if entry == nil then
				  value = 0
				  length = 4 -- Will pause 3 more later
			   elseif entry then
				  value = 1
				  length = 3
			   else
				  value = 1
				  length = 1
			   end
			   storm.io.set(value, pin)
			   cord.await(storm.os.invokeLater,
						  length*delay*storm.os.MILLISECOND)
			end
			storm.io.set(0, pin)
			if (stop) then
			   return
			end
			cord.await(storm.os.invokeLater,
					   3*delay*storm.os.MILLISECOND)
		 end
	  end)
   return res
end

return morse
