# Schedule a Real-Time Analytic Task & a Source that emits events
We will now configure a Source to emit data into the Kafka brokers.  A real-time analytic task using SPark Streaming will then consume the data and write the results to the spatiotemporal-store.  The spatiotemporal-store uses Elasticsearch to efficiently index observations by space, time, and all the other attributes of the event.  The JavaScript map app periodically queries to reflect the latest state of observations on a map.
<img src="../images/00-overview/iot-flow.jpg"/>

<b>Step 1:</b> We will now review a real-time analytic task marathon configuration found at <a href="../spatiotemporal-esri-analytics/rat01.json">spatiotemporal-esri-analytics/rat01.json</a>.
<img src="../images/07-app-setup/app-01.png"/><br>

<br><b>Step 2:</b> To schedule the real-time analytic task onto the DC/OS cluster issue the following DC/OS CLI command<ul><li>dcos marathon app add spatiotemporal-esri-analytics/rat01.json</li></ul>
<img src="../images/07-app-setup/app-02.png"/><br>




dcos marathon app add spatiotemporal-event-source/source01.json





