## Application & Architecture Overview
<img src="architecture.jpg"/>
All components of the IoT Event Flow described below (sources, brokers, spark streaming, elasticsearch & map apps) are scheduled on DC/OS as marathon apps.

## IoT Event Flow
<img src="flow.png"/>
Sources emit events to Kafka brokers.  Real-time Analytic Tasks (RATs) use Spark Streaming to consume events from Kafka brokers, perform spatiotemporal analytics and sink results to one or more sinks.  The spatiotemporal-store uses Elasticsearch to efficiently index observations by space, time, and all the other attributes of the event.  The JavaScript Web app periodically queries to reflect the latest state of observations on a map.

### Sources
Sources emit events to Kafka brokers.  When producing sources are responsible for partioning the data as evenly as possible across the available brokers.  There are two available sources to choose from.  The event-source allows you to play back a file at constant velocity over regular intervals, e.g. send 100 events every 1 second.  The spatiotemporal-event-source plays back a file based on the temporal values supplied in the file, e.g. play back each unique timestamp at an interval of 3 seconds.

### Kafka Brokers
Kafka brokers provide topics that are published to by Sources and consumed from by Real-time Analytic Tasks (Spark Streaming).

### Real-time Analytic Tasks (Spark Streaming)
Real-time Analytic Tasks (RATs) use Spark Streaming to consume events from Kafka brokers, perform spatiotemporal analytics and sink results to one or more sinks.  The real-time analytics tasks combines the <a href="https://github.com/Esri/geometry-api-java">esri/geometry-java-api</a> to perform geospatial analytics in near real-time.

### spatiotemporal-store (Elasticsearch)
The spatiotemporal-store uses Elasticsearch to efficiently index observations by space, time, and all the other attributes of the event.

### JavaScript Web App
The JavaScript Web app periodically queries to reflect the latest state of observations on a map.  The JavaScript web app queries for geohash aggregations that are visualized as rectangles on the map or raw observations visualized as symbols on the map.
