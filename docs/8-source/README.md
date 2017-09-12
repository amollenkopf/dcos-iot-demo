## Schedule a Kafka producer appplication (taxi-source)
<b>Step 1:</b> Review the taxi-source Kafka producer task marathon configuration found at <a href="../../spatiotemporal-event-source/taxi-source.json">spatiotemporal-event-source/taxi-source.json</a>.  Breaking the marathon app configuration file down:<ul><li>deploys one instance of a 'taxi-source' deployed as a <a href="https://hub.docker.com/r/amollenkopf/spatiotemporal-event-source/">amollenkopf/spatiotemporal-event-source</a> Docker container</li>
<li>each container is allocated 1 cpu shares & 5GB of memory (needed to load the large simulation file into memory)</li>
<li>each container starts up with a java command with lots of application specific parameters (including the Kafka Mesos DNS entry)</li>
<li>the --class gets resolved as part of the <a href="https://hub.docker.com/r/amollenkopf/spatiotemporal-event-source/">amollenkopf/spatiotemporal-event-source</a> Docker image</li></ul>
<img src="1.png"/>

<br><b>Step 2:</b> To schedule 'task-source' go to the DC/OS dashboard and navigate to 'Services - Services'. To run a new Service click the '+' button at the top right of the Services screen and click the 'Single Container' option.<br>
<img src="2.png"/><br>

<br><b>Step 3:</b> Toggle the 'JSON EDITOR' button to on and cut & paste the contents of <a href="../../spatiotemporal-event-source/taxi-source.json">spatiotemporal-event-source/taxi-source.json</a> into the JSON area.<br>
<img src="3.png"/><br>

<br><b>Step 4:</b> Click the 'REVIEW & RUN' button, review the service configuration & click the 'RUN SERVICE' button to schedule 'taxi-source'.<br>
<img src="4.png"/><br>

<br><b>Step 5:</b> On the 'Services' page note that 'taxi-source' is in 'Deploying' status.  <i>note: The first time you deploy the service it will download the .csv simulation file from S3 and will likely take a couple of minutes so be patient.</i><br>
<img src="5.png"/><br>

<br><b>Step 6:</b> Once the 'taxi-source' shows a status of 'Running' click on 'taxi-source' to see more information.<br>
<img src="6.png"/><br>

<br><b>Step 7:</b> 'taxi-source' is a custom Scala source application that reads a CSV file from S3, loads it's contents into memory and then produces taxi vehicle movement events to a Kafka topic.  Here we can see the host where the app was scheduled to as well as the status of the app.  To see the progress of the app we can dive into the Mesos Dashboard.<br>
<img src="7.png"/><br>

<br><b>Step 8:</b> Open the Mesos dashboard to view the task of 'taxi-source'.<br>
<img src="8.png"/><br>

<br><b>Step 9:</b> Here we can see the application task for 'taxi-source' and can monitor the progress by clicking into the 'Sandbox' and opening it's 'stdout' file.<br>
<img src="9.png" width="60%" height="60%"/><br>

<br><b>Step 10:</b> In the Mesos dashboard navigate to open the 'stdout' file of the 'taxi-stream' Spark Streaming driver.  Here we can see that the Spark Streaming workers tasks are now recieving the events from Kafka.<br>
<img src="10.png" width="60%" height="60%"/><br><br>

<br><br><b>Congratulations:</b> You have successfully ...
