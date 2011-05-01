# guessbitch

Guess Bitch is a multiplayer guessing game reminiscent of the popular "Guess Who?" board game (with similar mechanics).

## License

Copyright (C) 2011 Dave Paola

Distributed under the University of Illinois / NCSA Open Source License

# Architecture Notes

## Pieces:
* API - responsible for coordinating and storing games between two players.  games have a unique alphanumeric identifier (e.g. www.guessbitch.com/Tg7rx4yt or something)
* clients - clients access the API and basically just reflect the state returned by the API calls.  actions done by the user on the client will merely perform API calls and repopulate themselves based on the response
website (the only one, at first)
 * iphone/ipad app
 * android app
 * database - the API stores all persistent data in a backend database.  either relational rdbms (mysql) or nosql (redis)


# The Game State

Games have two players.  Players have game boards.  Game boards contain N slots (N=12 initially).  Slots have two states: exposed or flipped.  When exposed, they’ll have unique photos, when flipped they’ll have a uniform color or logo.

At any given time, the server must maintain the following information about the current state of the game (reflected in the database schema):

* The state of each player’s board and corresponding slots
* How many questions each player has asked (and what questions they’ve asked?)
* The duration of the game
* Which player we’re waiting for (to ask a question)

The clients will poll the API every N seconds to retrive the game state.  Player A asks a question, which will be sent to the API.  Then, on the next game state retrieval by Player B, the question will be displayed.  Player B will select the slots that correspond to the question, then click on a “finished” button of some kind.  This will send that player’s new state to the API, which will then be reflected the next time Player A polls the API for the game state.  The game proceeds thus until one of the players has no remaining slots.  

Keeping the clients stateless means that any errors or exceptions can merely be discarded and the next state retrieval will repopulate everything and the game can continue.  Any bugs or errors in the calculation of the state must then be present in the API and not in the clients.  In other words, keep the code dumb and the data smart!

# Database Schema

I think a schemaless, key-value store might be best for this sort of program.  The keys will therefore be the alphanumeric identifer and the values will be an encapsulated snapshot of the game state.  This can be JSON, something like the following:

	{
		‘timestamp’:’2011-04-28-05:55:36PM’,
		‘players’: [‘5hfdssh’, ‘u5oi6j45’],
		‘waiting_on’: ‘5hfdssh’,
		‘boards’: {
	‘5hfdssh’: [
		{‘kanye’: ‘flipped’},
		{‘obama’: ‘exposed’},
		…
	],
	‘u5oi6j45’: [
		{‘bill_gates’: ‘flipped’},
		{‘steve_jobs’: ‘flipped’},
		...
	]
	},
		‘num_questions’: {
			‘5hfdssh’:5,
			‘u5oi6j45’:6
		}
	}

Note that players have unique alphanumeric identifiers internally.  These can be calculated upon joining a game (handshake), possibly by hashing a given username with the IP address.  The clients are then made aware of their identifiers and provide them on subsequent API calls or polls.

When a game is finished, a “winner” key can be added to this JSON structure corresponding to the unique identifier for that player.  Every game action will happen as follows (on the server):
Retrieve game state from database
Perform specified action and recalculate game state
Save game state back into database
Respond to API action call

This design will allow both sides to operate asynchronously and handle failures gracefully.  Clients can have timeouts and the server can die and come back without any interruption in gameplay, since the data is stored as a stateless object.  For example, if a client tries to go out of turn, the API will discover that that client’s unique identifer isn’t the “waiting_on” ID and reject that action.  This prevents cheating on the server side rather than the client side.   This will also allow the system to be scalable -- for example, if I need to spin up 10 more servers to handle the load, the API servers can be dumb -- all they have to do is have access to the database and everything will work.  10 servers or 1000 servers, it won’t matter.  It’s all in the database.

PROBLEM: think about concurrency issues here.  If Player A’s API call gets routed to server A and player B’s request gets routed to server B, they access the same database.  if an action is performed, the gamestate is calculated on both requests, one of them will beat the other and one of the player’s actions will be lost.  Basically it’s a race condition.  The “waiting_on” attribute might act as a lock -- don’t recalculate anything for this player if it isn’t the same as the “waiting_on” character.  There’s still a race condition there, though.  If I’m checking, and during that time, the correct player modifies the database, the server’s response will still throw away that users action, even though it will now be the correct player.  

SOLUTION: use a message queue PER GAME.  actions are queued up, and if they’re incorrect, they can just be discarded.  the server’s functionality will be completely independent of whether or not there are clients waiting.  or rather, anything that MODIFIES the database (actions) are queued up; polls by the client can just query the database and return the state.  done.
