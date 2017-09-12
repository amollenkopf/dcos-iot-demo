# Run the demo
Schedule a real-time analytic task & a source that emits events.<br>
We will now configure a Source to emit data into the Kafka brokers.  A real-time analytic task using Spark Streaming will then consume the data and write the results to the spatiotemporal-store.  The spatiotemporal-store uses Elasticsearch to efficiently index observations by space, time, and all the other attributes of the event.  The JavaScript map app periodically queries to reflect the latest state of observations on a map.
<img src="../0-overview/flow.png"/>

## Visualize taxi movement behavior (map-webapp)
<br><b>Step 22:</b> 
The 'taxi-stream' task is writing events it recieves to Elasticsearch data nodes.  The 'map-webapp' that was deployed previously continuously queries Elasticsearch to visualize the latest taxi movement information on a map.  The map application can be accessed on the public agent node at /map/index.html, e.g. https://adamdcos04.westus.cloudapp.azure.com/map/index.html.<br>
<img src="22.gif"/><br>

<br><b>Step 23:</b> The 'map-webapp' has the ability to enable 'Replay' of the spatiotemporal observations.  To enable this flip the dial to on and use the time slider on the bottom left corner to specify the time window you want to replay with.  Stepping forward on the replay we can see the counts (labels on the goehash aggregations) increasing:<br>
<img src="23.gif"/><br>

<br><b>Step 24:</b> The 'map-webapp' also supports the ability to generate a client-side heatmap based on content being queried from Elasticsearch.  To enable this flip the 'Heatmap' dial to on and interact with the replay bar to see how taxi movement density changes with time using heatmap as the visualization technique.<br>
<img src="24.gif"/><br>

<br><b>Step 25:</b> Disabling the 'Replay' dial we can switch back into 'live' mode visualizing near real-time taxi movement as it occurs.<br>
<img src="25.png"/><br>

<br><b>Step 26:</b> Disabling the 'Heatmap' dial we can switch back into a geohash aggregation visualization of the near real-time taxi movement.<br>
<img src="26.png"/><br>

<br><br><b>Congratulations:</b> You have successfully run the dcos-iot-demo and visualized taxi movement using geoaggregation techniques.
