-- This script serializes and then unserializes a table

-- create our data
local t = {}
t.a = "abc"
t.b = 123
t.c = {1,2,3}
t.d = {}
t.d["a"] = 7
t.d["b"] = 8
t.d["c"] = 9
print("packing", t)

-- call storm.mp.pack with the target as the argument. The target
-- can be a table, array, string, number or stormarray
p = storm.mp.pack(t)
print("packed:", p)

-- unpack a serialized target
up = storm.mp.unpack(p)
print("should be abc", up.a)
print("should be 123", up.b)
print("should be {1,2,3}", up.c)
for idx,val in ipairs(up.c) do print("index",idx,"value",val) end
print("should be table", t.d)
for key,val in pairs(up.d) do print("key",key,"value",val) end
