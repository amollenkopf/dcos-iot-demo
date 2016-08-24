# Install Elasticsearch & Schedule a Cluster

<b>Step 1:</b> Elasticsearch can be installed via the Universe.  However, we will NOT be installing it via Universe as we need to make a small modification to Elasticsearch that is unfortunately not exposed by the Elasticsearch package parameters.
<img src="../images/04-es-setup/es-00.png"/>
<br><br><b>Step 2:</b> The small modification that is needed is to enable <a href="https://www.w3.org/TR/cors/">Cross-Origin Resource Sharing (CORS)</a> so that the JavaScript web map app (that we will use in later steps) can establish a line of communication with Elasticsearch. A Docker image <a href="https://hub.docker.com/r/amollenkopf/elasticsearch/">amollenkopf/elasticsearch</a> has been created and provided for you to work with that has the appropriate configuration in place.  If you prefer to create your own Docker image instead of using the provided one, see the <a href="es-setup-docker.md">How to create your own Elasticsearch docker image with CORS enabled</a> instructions.
<br><br><b>Step 3:</b> To establish an Elasticsearch cluster with the CORS setting in place we will make use of the <a href="https://hub.docker.com/r/amollenkopf/elasticsearch/">amollenkopf/elasticsearch</a> Docker image and schedule the cluster to run via Marathon.  Prior to doing this lets review the contents of this file <a href="../elasticsearch-marathon.json">elasticsearch-marathon.json</a>.<br>
<img src="../images/04-es-setup/es-01.png" width="70%" height="70%"/>
<br><br><b>Step 4:</b> Schedule an Elasticsearch cluster on DC/OS by submitting the <a href="../elasticsearch-marathon.json">elasticsearch-marathon.json</a> to Marathon.<br>
<img src="../images/04-es-setup/es-02.png" width="70%" height="70%"/>
<br><br><b>Step 5:</b>... 
<img src="../images/04-es-setup/es-03.png"/>
<br><br><b>Step 6:</b>... 
<img src="../images/04-es-setup/es-04.png"/>
<br><br><b>Step 7:</b>... 
<img src="../images/04-es-setup/es-05.png"/>
<br><br><b>Step 8:</b>... 
<img src="../images/04-es-setup/es-06.png"/>
<br><br><b>Step 9:</b>... 
<img src="../images/04-es-setup/es-07.png"/>
<br><br><b>Step 10:</b>... 
<img src="../images/04-es-setup/es-08.png"/>
