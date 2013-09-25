qepoc
=====

# What's this? #
It is a query engine proof of concept. We want to have three different minimal implementations of a query engine configuration in order to make a good decision.

## What are the requirements? ##

We want to mimick a request for the main upstream interface of a cable modem with the fake MAC address 00AB123456. Each of the POCs should perform the same requests to the mock-service and get the same results.

### Gimme the recipe! ###
Ok! Beware, this is a realistic example, but the parameters are all fake and wouldn't work in the real world.

Input Parameters: 

* 00AB123456
* mainUpstreamIfc

Result:

* 'L1/0/1/1'

Needed request sequence:

1. /modem/00AB123456/ip → 10.30.22.12
2. /scopes/10.30.22.12 → mbsXZY001-01-pc
3. /scopes/mbsXZY001-01-pc/cmts → mbsXZY001
4. /cmts/mbsXZY001/modem/00AB123456/cmId → 203342
5. /cmts/mbsXZY001/ip → 62.2.11.22
6. /snmp/62.2.11.22/1.3.6.2.1.2.3.4.20334 → L1/0/1/1

The outcome of the request should be the string L1/0/1/1.

## Project structure ##

*   qepoc-mock: A Mock for the requests. 
 
    Compile: 
	```
	cd /path/to/qepoc-mock
    mvn clean package
    ```
    Run:
    ```
    java -jar target/qepoc-mock-1.0-SNAPSHOT.jar
    >starting up server...
	>… ready, listening at port 4321.
	>Say 'Bye.' to disconnect.
	>Say 'Shut.' to shutdown.
    ```
    Use:
    ```
    telnet localhost 4321
    Trying ::1...
	Connected to localhost.
	Escape character is '^]'.
	Welcome, young padavan. Send me your request!
	/modem/00AB123456/ip
	10.30.22.12
	```
	or use the java client!
	```
	String result = Client.query("/modem/00AB123456/ip");
	```
	
	Disconnect but let the server run:
	```
	telnet localhost 4321
	Bye.
	
	Bye.
	Connection closed by foreign host.
	```
    Terminate:
    ```
    telnet localhost 4321
    Shut.
    Bye.
	Connection closed by foreign host.
	```