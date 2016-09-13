# Schedule a Real-Time Analytic Task & a Source that emits events
We will now configure a Source to emit data into the Kafka brokers.  A real-time analytic task using SPark Streaming will then consume the data and write the results to the spatiotemporal-store.  The spatiotemporal-store uses Elasticsearch to efficiently index observations by space, time, and all the other attributes of the event.  The JavaScript map app periodically queries to reflect the latest state of observations on a map.
<img src="../images/00-overview/iot-flow.jpg"/>

<b>Step 1:</b> We will now review a real-time analytic task marathon configuration found at <a href="../spatiotemporal-esri-analytics/rat01.json">spatiotemporal-esri-analytics/rat01.json</a>.  Breaking the marathon app configuration file down:<ul><li>deploys 3 instances of a 'rat01' deployed as Spark 1.6 Docker containers</li>
<li>each container is allocated 4 cpu shares & 2GB of memory</li>
<li>each container starts up with the command spark-submit with lots of application specific parameters</li>
<li>the --class gets bootstraped in via a URI downloaded prior to the start of each container</li></ul>
<img src="../images/07-app-setup/app-01.png"/><br>
<br><b>Step 2:</b> To schedule <a href="../spatiotemporal-esri-analytics/rat01.json">spatiotemporal-esri-analytics/rat01.json</a> onto the DC/OS cluster issue the following DC/OS CLI command<ul><li>dcos marathon app add spatiotemporal-esri-analytics/rat01.json</li></ul>
<img src="../images/07-app-setup/app-02.png"/><br>
<br><b>Step 3:</b> Open the Marathon dashboard to view the deployment progress of rat01:<br>
<img src="../images/07-app-setup/app-03.png"/><br>
<br><b>Step 4:</b> Click on the rat01 application to see more details include what hosts and ports it was scheduled to:<br>
<img src="../images/07-app-setup/app-04.png"/><br>
<br><b>Step 5:</b> Open the Mesos dashboard to view the active tasks of rat01:<br>
<img src="../images/07-app-setup/app-05.png"/><br>
<br><b>Step 6:</b> For each rat01 instance click on it's 'Sandbox' and open the stdout file to monitor verbose print outs of rat01:<br>
<img src="../images/07-app-setup/app-06.png"/><br>
<br><b>Step 7:</b> The three stdout files of the associated rat01 instances are showing that they are saving 0 records to Elasticsearch.  This is because we have not yet enabled a Source that will emit events.<br>
<img src="../images/07-app-setup/app-07.png"/><br>
<br><b>Step 8:</b> In order for us to partition events sent to Kafka in an evenly distributed mode we will create a topic with partitions matching the number of brokers we have deployed.  note: the Source (producer) has code, <a href="../blob/master/spatiotemporal-event-source/src/main/scala/org/cam/geo/source/SimplePartitioner.scala">SimplePartitioner.scala</a> that<br>
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

