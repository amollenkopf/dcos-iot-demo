# Schedule a Real-Time Analytic Task & a Source that emits events
We will now configure a Source to emit data into the Kafka brokers.  A real-time analytic task using SPark Streaming will then consume the data and write the results to the spatiotemporal-store.  The spatiotemporal-store uses Elasticsearch to efficiently index observations by space, time, and all the other attributes of the event.  The JavaScript map app periodically queries to reflect the latest state of observations on a map.
<img src="../images/00-overview/iot-flow.jpg"/>

<b>Step 1:</b> We will now review a real-time analytic task marathon configuration found at <a href="../spatiotemporal-esri-analytics/rat01.json">spatiotemporal-esri-analytics/rat01.json</a>.  Breaking the marathon app configuration file down:<ul><li>deploys 3 instances of a 'rat01' deployed as Spark 1.6 Docker containers.</li>
<li>each container is allocated 4 cpu shares & 2GB of memory</li>
<li>container starts up with a command to spark-submit with application specific parameters</li>
<li>the --class gets bootstraped in via the URI that is downloaded prior to the start of each container</li></ul>
<img src="../images/07-app-setup/app-01.png"/><br>

<br><b>Step 2:</b> To schedule a real-time analytic task onto the DC/OS cluster issue the following DC/OS CLI command<ul><li>dcos marathon app add spatiotemporal-esri-analytics/rat01.json</li></ul>
<img src="../images/07-app-setup/app-02.png"/><br>

<br><b>Step 3:</b> ...<br>
<img src="../images/07-app-setup/app-03.png"/><br>
<br><b>Step 4:</b> ...<br>
<img src="../images/07-app-setup/app-04.png"/><br>
<br><b>Step 5:</b> ...<br>
<img src="../images/07-app-setup/app-05.png"/><br>
<br><b>Step 6:</b> ...<br>
<img src="../images/07-app-setup/app-06.png"/><br>
<br><b>Step 7:</b> ...<br>
<img src="../images/07-app-setup/app-07.png"/><br>
<br><b>Step 8:</b> ...<br>
<img src="../images/07-app-setup/app-08.png"/><br>
<br><b>Step 9:</b> ...<br>
<img src="../images/07-app-setup/app-09.png" width="70%" height="70%"/><br>
<br><b>Step 10:</b> ...<br>
<img src="../images/07-app-setup/app-10.png"/><br>

<br><b>Step 11:</b> To schedule a Source that emits events into a Kafka topic's partitions running on a DC/OS cluster issue the following DC/OS CLI command<ul><li>dcos marathon app add spatiotemporal-event-source/source01.json</li></ul>
<img src="../images/07-app-setup/app-11.png"/><br>
<br><b>Step 12:</b> ...<br>
<img src="../images/07-app-setup/app-12.png"/><br>
<br><b>Step 13:</b> ...<br>
<img src="../images/07-app-setup/app-13.png"/><br>
<br><b>Step 14:</b> ...<br>
<img src="../images/07-app-setup/app-14.png"/><br>
<br><b>Step 15:</b> ...<br>
<img src="../images/07-app-setup/app-15.png"/><br>
<br><b>Step 16:</b> ...<br>
<img src="../images/07-app-setup/app-16.png" width="50%" height="50%"/><br>
<br><b>Step 17:</b> ...<br>
<img src="../images/07-app-setup/app-17.png"/><br>
<br><b>Step 18:</b> ...<br>
<img src="../images/07-app-setup/app-18.png"/><br>
<br><b>Step 19:</b> ...<br>
<img src="../images/07-app-setup/app-19.png"/><br>
<br><b>Step 20:</b> ...<br>
<img src="../images/07-app-setup/app-20.png"/><br>
<br><b>Step 21:</b> ...<br>
<img src="../images/07-app-setup/app-21.png"/><br>
<br><b>Step 22:</b> ...<br>
<img src="../images/07-app-setup/app-22.png"/><br>
<br><b>Step 23:</b> ...<br>
<img src="../images/07-app-setup/app-23.png"/><br>
<br><b>Step 24:</b> ...<br>
<img src="../images/07-app-setup/app-24.png"/><br>

