# Tupperware

Tupperware is a silly little social media application, written in java.
It supports friendship, posts of various privacy levels, private messaging, group chats, and the ability to manipulate the flow of time.
There is a very cool-looking textual user interface courtesy of [Lanterna](https://github.com/mabe02/lanterna).

I wrote it for a databases class, which had a lot of very strange data requirements, for example, that email addresses be no more than 20 characters long.
I may remove these at some point in the future.

## Running it

There should be a postgres server running on localhost:5432.
It should be a user and a database named `tupperware`, and the user should have no password.

```bash
$ make lib              # build lanterna, the gui library
$ make                  # build the main body of code
$ make setup            # set up the database schema
$ make run              # run that bad boy
```

Move with the arrow keys, submit with the enter key. Once logged in, hit the
help button to learn to use the window management system.

## Problems

There is currently no convenient way of running tupperware as a server.
Fixing this is pretty high up on my list of priorities!

Also the build system is makefiles + javac. Sue me.
