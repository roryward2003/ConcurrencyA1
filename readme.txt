# Q1

## Running
In your command line run the following:
 - cd src
 - javac q1.java
 - java q1 w h t n    // where w = width, h = height, t = num of threads, n = num of snowmen

## Description
This code randomly generates and draws snowmen on a BufferedImage, without overlaps.
It is set up to allow for multiple threads to work on this task simultaneously.

# Q2

## Running
In your command line run the following:
 - cd src
 - javac q2.java
 - java q2 k j s      // where k = adder sleep time (ms), j = remover sleep time (ms), s = simulation length (s)

## Description
This code runs 3 threads to simulate a snakes and ladders game, where one thread is the player,
another thread adds snakes and ladders to the board at intervals of k ms, and the other thread
removes snakes and ladders from the board at intervals of j ms.

The player makes a move every 20-50 ms and sleeps for 100 ms upon winning. This simulation will
run for s seconds