# Log Collector
Log Collector is RESTful app in charge of collecting log events from different sources, validating, normalizing and storing them 


tools

`java`
`maven`


building app: 

in root of app type: 
`mvn clean package`
maven will download all needed dependencies and build binary


resulting application binary will be created in single file `target/LogCollector-0.0.1-SNAPSHOT-jar-with-dependencies.jar`

application can be run with: 
`java -jar target/LogCollector-0.0.1-SNAPSHOT-jar-with-dependencies.jar`

Should application need to be configured, one can make `properties` folder in place of running application and store inside 3 files: 

`activity.csv` - contains mappings betveen activity codes and activity strings
`log-collector.config` - application configuration properties
`XmlInputEventSchema.xml` - contains definitions of valid XML event coming into application. 

all three files are already created and part of application's binary, so it is not needed to configure app to run it. It will run with default settings as set in config files part of application.


project structure: 

`src/main/java` - source folder containing all needed sources. 

`src/main/webapp` - static html contents so application can present itself, start point for configuring application using browser etc. 
`src/test/java` - all test classes

`target` - target folder with compiled application 

## feeding data into application 

### XML events

Example how to feed XML event into application: 

```
curl -v --header "Content-Type: application/xml; charset=UTF-8" --request POST --data \
'<?xml version="1.0" encoding="UTF-8"?>
<activity>
<userName>Williamson</userName>
<websiteName>xyz.com</websiteName>
<activityTypeCode>001</activityTypeCode>
<loggedInTime>2020-01-13</loggedInTime>
<number_of_views>10</number_of_views>
</activity>' http://localhost:9980/log-collector/api/event
```

### JSON events

Example how to feed JSON event into application: 

```
curl -v --header "Content-Type: application/json; charset=UTF-8"   --request POST   --data ' 
{
"activity":{
"userName":"Sam Json",
"websiteName":"abc.com",
"activityTypeDescription":"Viewed",
"signedInTime":"01/14/2020"
 }
}
' http://localhost:9980/log-collector/api/event
```

 
 
   
