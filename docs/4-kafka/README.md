# Install Kafka & schedule brokers

<b>Step 1:</b> In the DC/OS dashboard navigate to 'Universe'.  The 'Universe' is where you can manage what packages (aka mesos frameworks) your cluster can make use of.<br>
<img src="01.png"/>

<br><b>Step 2:</b> Scroll down in the 'Universe' package listings to find the 'Kafka' package and click it's corresponding 'Install' button.<br>
<img src="02.png"/>

<br><b>Step 3:</b> Click the 'Advanced Installation' button to start the package installation process.<br>
<img src="03.png"/>

<br><b>Step 4:</b> Click the 'Advanced Installation' link to fine tune the installation parameters.<br>
<img src="04.png"/>

<br><b>Step 5:</b> Click the 'service' parameter group, scroll through it's available options, and keep all defaults.<br>
<img src="05.png"/>

<br><b>Step 6:</b> Click the 'brokers' parameter group and scroll through it's available options.<br>
<img src="06.png"/>

<br><b>Step 7:</b> Find the 'count' parameter and change the value to the number of brokers you desire, for the purpose of the demo we will keep the default of '3'.<br>
<img src="07.png"/>

<br><b>Step 8:</b> Click the 'kafka' parameter group and scroll through it's available options.<br>
<img src="08.png"/>

<br><b>Step 9:</b> Find the 'delete.topic.enable' parameter and enable it so that we will be able to delete topics between demo runs.<br>
<img src="09.png"/>

<br><b>Step 10:</b> Click the 'Review and Install' button and review the parameters values.<br>
<img src="10.png"/>

<br><b>Step 11:</b> Click the 'Install' button to install the package and click the 'Acknowledge (check)' button.<br>
<img src="11.png"/>

<br><b>Step 12:</b> In the DC/OS dashboard navigate to 'Services'.  The 'Services' tab is where you can monitor what services have been scheduled and are running on the DC/OS cluster.  Notice there is a Service named 'kafka' that now appears.<br>
<img src="12.png"/>

<br><b>Step 13:</b> Click on 'kafka' in the service listing to open up more information on the 'kafka' service.  Here we can see the three brokers, their names, their status, and the resources that have been allocated to them.<br>
<img src="13.png"/>

<br><b>Step 14:</b> Install the dcos-cli (Command Line Interface) by grabbing the binaries for your operating system at <a href="https://github.com/dcos/dcos-cli/releases">https://github.com/dcos/dcos-cli/releases</a><br>
<img src="14.png"/>

<br><b>Step 15:</b> Using the dcos-cli we can interact with Kafka.  Type 'dcos kafka --help' to see the available commands.<br>
<img src="15.png"/>

<br><b>Step 16:</b> You can get a listing of broker names.  Type 'dcos kafka broker list'.<br>
<img src="16.png"/>

<br><b>Step 17:</b> You can get connection details which is needed information to share with Kafka producers.  Type 'dcos kafka connection'.<br>
<img src="17.png"/>

<br><br><b>Congratulations:</b> You now have Kafka installed with three brokers ready to receive data on the DC/OS cluster.  Next, we will walk through how to <a href="../docs/es-setup.md">Install & schedule an Elasticsearch cluster</a>
