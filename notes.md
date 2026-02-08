# Reactive Redis with Java

## Redis

- fast in-memory NoSQL database
- multi-purpose data structure server
- use cases

1. caching
2. pub-sub
3. message queue
4. streaming
5. geospatial

- free & open source
- redis.io -> open source
- redislabs.com -> commercial cloud service

- redis-cli ---> redis-server (6379)
- redis-cli uses imperative commands

```
> ping
pong
```

- Redis: Key-Value
  - Simple key-value store
  - Redis is simple - keys and values are simple strings
  - any binary sequence can be the key (prefer short, readable key)
  - 2 ^ 32 keys (>4 billion keys)
  - 512 MB max size

```sh
# set <key> <value> -  set the key:value pair
set a b
# get <key>
get a
get c # nil
```

- Redis encourages developers to follow their standards for key name, e.g. `/user/1/name` or preferred way `user:1:name`
- access keys

```sh
keys <regex-pattern>
# access all keys; can affect the performance
keys *
# access keys that match the following pattern
keys user*
# scan - newer function; better option
scan 0 # list all keys in a paginated way (10 by 10); use the number it displays to fetch the next page
# scan <page-no> MATCH <pattern> - scans the page no <page-no> by a given pattern
scan 0 MATCH user*
# limit - scan <page-no> MATCH <pattern> count <number>

scan 0 MATCH user* count 3
```

- delete keys

```sh
# delete an entry by key
# del <key>
> del user:1:name
(integer) 1 # num of deleted rows
# if we try to delete a non-existent entry, it will return 0
# del <key-1> <key-12> <key-3> - deletes all given keys (entries by keys)
# it doesn't accept regex pattern
# truncate database
flushdb
```

#### Expiring keys

```sh
# it will automatically expire after 10 seconds
set a b ex 10

# access the TTL of a key
# ttl <key>
ttl a

# extend the expiry
# expire <key> <new-expiry-in-seconds>
extend a 60

# expire at Unix timestamp
set a b exat 1766010100

# expiry in milliseconds
set b c px 3000  # 3000 milliseconds

# if you have a TTL set on a key and you change the value associated with that key, the expiry will be gone
set a b ex 60
ttl a -> 60
set a c
ttl a -> -1

# how to keep TTL?
set a b ex 60
ttl a -> 60
set a c keepttl
ttl a -> 55
```

### Set Options - XX/NX

- `set a b` - key = a, value = b
- `xx` - if it's present, do this
- `nx` - if not present, do this

- `flushdb` - clear everything

- `set a b xx` - set if present (update)
- `set a b nx` - set if not present

### Exists

- `keys *` - shows all keys
- `exists <key>` - indicates if the key exists

- `set user:1:session token ex 10` - set key:value entry that is going to expire in 10s

### INCR/DECR

- `incr a` -> increments the value associated with the given key (by default Redis stores everything as a string, so it is applicable only to integer values)
- `decr a` -> increments the value associated with the given key

- if the key is not present, it will first create it, set the value to zero and then increment/decrement it

- how to increment a float value?
- `incrbyfloat <key> <float_value>` - increments the float value associated with the given key by the specified float value (can be negative to decrement)

### Cheat sheet reference:

https://cheatography.com/tasjaevan/cheat-sheets/redis/#google_vignette
https://redis.io/docs/latest/commands/

### Redis notification about the key expiry

- Redis can notify the client that the key has expired (library for channel notifs)

### Hash

- There is no table/collection concept in Redis
- Redis encourages developers to follow their own standards for key names
- Set/Get are for simple key value pairs
- Hash

  - to store a group of fields that belong to an object
  - we can access/update invidiual field of an object

- `hset <key> <field1> <value1> <field2> <value2> <field3> <value3>` -> `hset user:1 name sam age 10 city atlanta`
- we cannot use `get` with hash object
- `type <hash-type-object-key>` -> hash
- `hget key field` -> `hget user:1 age` = 10
- `hgetall key` -> returns the whole object from hash (all field-value pairs)
- we cannot expire just one field from hash, only the whole hash object

- `hkeys` - returns all keys from a hash object
- `hvalues` - returns all keys from a hash object
- `hexists <key> field` - does the key exist in the hash
- `hgetall key` - returns all key and value entries from a hash object
- `hdel <key> field` - removes a field from the hash object
- `del <key>` - deletes a hash object too

### List

- an ordered collection of items (string)
- similar to Java List (LinkedList)
- it can be used as Queue/Stack
- Redis List can be used as a Message Queue

- `get` does not work with lists

- `rpush key value1 value2 value3` - pushes to the end of the list -> `rpush users sam mike jake`
- `lpush key value` - pushes to the beginning of the list -> `lpush users sam mike jake`

- len
  - `llen key` -> list length
  - `hlen key` -> hash length
- `lrange users 0 1` - returns from 0th to 1st index (inclusive)
- `lrange users 0 -1` - returns the complete content of a list
- `rpop users [count]` - removes from the end of the list; count is optional, it defaults to 1
- `lpop users [count]` - removes from the beginning of the list

- NOTE: Redis does not keep keys that do not have any value associated

### Set

- an unordered collection of unique items (string)
- similar to Java Set
- use cases:

  - maintain currently logged in users
  - maintain blacklisted IP address/users
  - Set intersection

- `sadd <set-name> <values...>` - `sadd users 1 2 3 4 5`
- `scard <set-name>` - gets the length (cardinality) of that set
- `smembers <set-name>` - list all set members (it does not maintain the order of insertion)
- `sismember <set-name> <value>` - checks if the value is in the set
- `srem <set-name> <value>` - removes an element from the set
- `spop <set-name>` - randomly removes an element from the set

- Set operations

  - `sinter <set-name-1> <set-name-2> <set-name-3>...` -> returns the intersection of all given sets
  - `sunion <set-name-1> <set-name-2> <set-name-3>...` -> returns the union of all given sets
  - `sdiff <set-name-1> <set-name-2> <set-name-3>...` -> returns the diff between the first set and all successive sets
  - `sinterstore <dest-set-name> <set-name-1> <set-name-2>....` -> stores the intersection of all given sets in a separate set

- List and Set can hold 2^32 items

### Sorted Set

- it contains a score for every item and it sorts based on that score (it is called rank)
- score can be an integer or the floating point value
- an ordered collection of unique items (string)
- similar to Java Sorted Set
- if two items have the same score, it will sort them alphabetically by their value
- Use cases:

  - Priority Queue
  - Top rated movie/product
  - frequently visited pages

- `zadd <sorted-set-name> <rank-1> <value-1> <rank-2> <value-2> <rank-3> <value-3>...` - creates a sorted set and adds values to it
- `zcard <sorted-set-name>` - gets the length (cardinality) of the sorted set
- `zincrby <sorted-set-name> <rank> <value>` - increments a rank of the given entry (we can have duplicates, so we need both rank and value to determine the entry)
- `zrange <sorted-set-name> <start> <stop>` - shows all items from the sorted set in the given range
  - `zrange products 0 -1` -> shows all items
  - `zrange <sorted-set-name> <start> <stop> withscores` - shows all items from the sorted set in the given range with their scores
  - `zrange products -1 -1` - gives us the item with the highest rank
  - `zrange products 0 0 rev` - gives us the first item (with the smallest rank) in the reverse order (the same thing as above)
- `zrank <sorted-set> <value>` - gives us the index of the given value (values are unique) - when sorted; it first sorts the sorted set and then it returns the index of the given value
- `zrevrank <sorted-set> <value>` - gives us the index of the given value (values are unique) - when sorted in descending order; it first sorts the sorted set in descending order and then it returns the index of the given value
- `zscore <sorted-set> <value>` - returns the score of the given value
- `zpopmax <sorted-set>` - pops the value with the max rank (score)

### Redis Transaction

- Redis is single threaded; it has a few other threads for other purposes, but it uses a single thread for the event loop
- when clients send multiple requests at the same time; these requests are queued; so Redis does not need to worry about locking and synchronization
- commands are then atomic by design
- transaction in Redis: Hey Redis, you are going to receive a series of commands to execute them together. If all of them are successful, commit them, otherwise undo all these commands
- `multi` - goes into transaction mode; all upcoming commands will be queued
- `exec` - executes all commands in the transaction
- `watch <key-1> <key-2>...` - watch these keys, if they have been modified in the meantime, by another transaction, it will revert this transaction
- once `exec` is run, watches on those variables will be removed
- `discard` - rollback (revert the transaction)

```
watch user:1:balance user:2:balance

multi
....
....
exec
```

### Saving data on disk

- Redis periodically flushes data to disk, but not for each and every command
- useful to load a snapshot into Redis; when Redis is shutdown, it will try to save the state to disk
- `bgsave` - it will use some additional threads to save the Redis state to disk
