# Configure the Map Application

<b>Step 1:</b> In order for the map application to work properly you first need to establish an SSH tunneling session for connectivity with the Elasticsearch cluster running in DC/OS.  This ssh tunnel will enable all local traffic on port :9200 to be redirected to the <a href="https://github.com/mesosphere/mesos-dns">Mesos DNS</a> value of 'spatiotemporal-store.elasticsearch.mesos' which evaluates to the Elasticsearch cluster running in DC/OS.<ul><li>sudo ssh -i ~/.ssh/azure -A -L 9200:spatiotemporal-store.elasticsearch.mesos:9200 azureuser@esri51mgmt.westus.cloudapp.azure.com -p 2200</li></ul>
<img src="../images/07-map-setup/map-01.png"/><br>
<br><b>Step 2:</b> Open a browser to the location where you put <a href="../map/index.html">map/index.html</a> ...<br>
<img src="../images/07-map-setup/map-02.png"/><br>
