# Rate Limit Service

## Requirements:
[Java 18](https://docs.oracle.com/en/java/javase/18/install/installation-jdk-linux-platforms.html#GUID-4907E1A6-7B4B-4E98-9DA5-BF2A4D01AA57)

[maven 3.8.5](https://phoenixnap.com/kb/install-maven-on-ubuntu)

## Installation:
To install program from sources, run 'mvn install' in root folder of the project

## Run
use './Run.sh', file is attached to the app with threshold = 10 and timelimit = 1min

OR './Run.sh <x> <y>' where x,y are int, long for threshold and time in millis respectively.

OR type  'java -jar RateLimitService <x> <y>' where x,y are the inputs.

## Sample Test Run :
You can use the the './sample_run.sh' command to execute a test that is similar to the example in the
PDF file of the exercise requirements, note: the service. ie this java program has to run in advance.

## Architecture
The app acts as a web-service on port 8080 exposing a single HTTP Rest API '/report/'

The main inner service is a gateway to all other services of the app, it is the only one that 
knows about the other services, but still works totally asynchronously and non-blocking.

The main service will register  & chain a chain of tasks in an async manner,
and collapse while the logic is now delegated to the other services denoted below.

The app also uses 2 inner services, where each one is designated for a different task.

ValidationService acts as a validation layer in the form of validating input, parsing, then returning appropriate data
in the right form.

RateLimitService - a service that performs the BL as per the requirements. RateLimitService is a module that uses it's
own executor (thread pool), thus performing tasks in an async manner.

# Tests
There's only sample test attached to this exercise in the ./sample_run.sh file.

Other test can and should include:

- Testing inner functions / method for each class
- Testing Wrapping layers / Services with mock values
- Testing Full process (End To End) correctness with no mocks
- Benchmarking and validating concurrency - it's correctness and prformance

## Possible Changes / Enhancements
The current implementation assumes sequential order is important and dictated by timestamp,
thus the RateLimitService only holds 1 thread in its pool .We can scale this threadpool to contain
more working threads, and possibly reach better performance, but risk in accepting tasks that are valid, but managed to
race another valid task that was processed slower by the service. i.e URL request A arrived before request B did, i.e 
A.timestamp < B.timestamp yet service computed B faster and allowed it to visit, raising the count of visits. by that time, A is done but threshold
was reached thus A would be blocked.