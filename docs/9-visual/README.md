## Visualize IoT movement behavior (map-webapp)
<br><b>Step 1:</b> 
The 'taxi-stream' task writes events it recieves to Elasticsearch data nodes.  The 'map-webapp' that was deployed previously continuously queries Elasticsearch to visualize the latest taxi movement information on a map.  Each second the visualization is automatically updated with the latest taxi movement statistics from Elasticsearch.  The map application can be accessed on the public agent node at /map/index.html, e.g. https://adamdcos04.westus.cloudapp.azure.com/map/index.html.<br>
<img src="01.gif"/><br>

<br><b>Step 2:</b> The 'map-webapp' has the ability to enable 'Replay' of the spatiotemporal observations.  To enable this flip the 'Replay' dial to on and use the time slider on the bottom left corner to specify the time window you want to replay with.  Stepping forward on the replay we can see the counts (labels on the goehash aggregations) increasing:<br>
<img src="02.gif"/><br>

<br><b>Step 3:</b> The 'map-webapp' also supports the ability to generate a client-side heatmap based on content being queried from Elasticsearch.  To enable this flip the 'Heatmap' dial to on and interact with the replay bar to see how taxi movement density changes with time using heatmap as the visualization technique.<br>
<img src="03.gif"/><br>

<br><b>Step 4:</b> Disabling the 'Replay' dial we can switch back into 'live' mode visualizing near real-time taxi movement as it occurs.<br>
<img src="04.png"/><br>

<br><b>Step 5:</b> Disabling the 'Heatmap' dial we can switch back into a geohash aggregation visualization of the near real-time taxi movement. By default 'live' mode updates every second with the latest taxi movement obvservations available.<br>
<img src="05.png"/><br>

<br><br><b>Congratulations:</b> You have successfully run the dcos-iot-demo and visualized taxi movement using geoaggregation techniques.
