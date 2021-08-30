# Event Collector
Event Collector is RESTful app in charge of collecting log events from different sources

tools

`java`
`maven`


building app: 

in root of app type: 
`mvn clean package`
maven will download all needed dependencies and build binary


resulting application binary will be created in single file `target/EventCollector-1.0.0-SNAPSHOT-jar-with-dependencies.jar`

application can be run with: 
`java -jar target/EventCollector-1.0.0-SNAPSHOT-jar-with-dependencies.jar`

Should application need to be configured, one can make `properties` folder in place of running application and store inside 1 file: 

`event-collector.config` - application configuration properties

file is already created and part of application's binary, so it is not needed to configure app to run it. It will run with default settings as set in config files part of application.


project structure: 

`src/main/java` - source folder containing all needed sources. 

`src/main/webapp` - static html contents so application can present itself, starting point for configuring application using browser etc. 

`src/test/java` - all test classes

`target` - target folder with compiled application 

## feeding data into application 

 
### JSON events

Example how to feed JSON server event into application: 

```
curl -v --header "Content-Type: application/json; charset=UTF-8"   --request POST   --data ' 
{
"id":"someId",
"type":"some",
"host":"abc.com",
"state":"FINISHED",
"timestamp": 1636395139091
}
' http://localhost:9980/event-collector/api/server-event
```

Example how to feed JSON input event into application: 

```
curl -v --header "Content-Type: application/json; charset=UTF-8"   --request POST   --data ' 
{
"id":"someId",
"state":"STARTED",
"timestamp": 1636395139086
}
' http://localhost:9980/event-collector/api/event
```


 
 
   
