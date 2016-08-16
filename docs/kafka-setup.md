# Schedule Kafka brokers

<b>Step 1:</b> In the DC/OS dashboard navigate to 'Universe'.  The 'Universe' is where you can manage what packages (mesos frameworks) your cluster can make use of.
<img src="../images/03-kafka-setup/kafka-01.png"/>
<br><br><b>Step 2:</b> Scroll down in the 'Universe' package listings to find the 'Kafka' package and click it's corresponding 'Install' button.
<img src="../images/03-kafka-setup/kafka-02.png"/>
<br><br><b>Step 3:</b> Click the 'Install Package' button to start the package installation process.
<img src="../images/03-kafka-setup/kafka-03.png"/>
<br><br><b>Step 4:</b> Click the 'Advanced Installation' link to fine tune the installation parameters.
<img src="../images/03-kafka-setup/kafka-04.png"/>
<br><br><b>Step 5:</b> Click the 'service' parameter group to scroll through it's available options and keep all defaults.
<img src="../images/03-kafka-setup/kafka-05.png"/>
<br><br><b>Step 6:</b> Click the 'brokers' parameter group to scroll through it's available options and change the count to the number of brokers you desire, for the purpose of the demo we will keep the default of '3'.
<img src="../images/03-kafka-setup/kafka-06.png"/>
