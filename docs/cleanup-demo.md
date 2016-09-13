# Applying Cleanup Procedures Between Demo Runs
Often times when you show this demo you may want to start from a zero data situation to best illustrate how data flows in accumulates.  To accomplish this here is a tear down script that will reset all the components necessary to have re-establish a clean data environment.<br>

<b>Step 1:</b> Remove source01 by issueing the following DC/OS CLI command<ul><li>dcos marathon app remove source01</li></ul>
<img src="../images/08-cleanup-demo/cleanup-01.png" width="70%" height="70%"/><br>
<br><b>Step 2:</b> Remove rat01 by issueing the following DC/OS CLI command<ul><li>dcos marathon app remove rat01</li></ul>
<img src="../images/08-cleanup-demo/cleanup-02.png" width="70%" height="70%"/><br>
<br><b>Step 3:</b> Verify that source01 and rat01 are both removed from the application listing in the Marathon dashboard<br>
<img src="../images/08-cleanup-demo/cleanup-03.png"/><br>
<br><b>Step 4:</b> Remove the Elasticsearch index and all of it's assoicated data by issuing the following command from the ssh tunnel shell of the master node<br><ul><li>curl -XDELETE 'http://spatiotemporal-store.elasticsearch.mesos:9200/taxi'</li>
<img src="../images/08-cleanup-demo/cleanup-04.png"/><br>
<li>to verify you can run: curl -XGET 'http://spatiotemporal-store.elasticsearch.mesos:9200/*' and see that you get empty results</li>
<img src="../images/08-cleanup-demo/cleanup-05.png"/><br>
</ul>
<br><b>Step 5:</b> Navigate to the Zookeeper Exhibitor dashboard<br>
<img src="../images/08-cleanup-demo/cleanup-06.png"/><br>
<br><b>Step 6:</b> delete /kafka/consumers/taxi-consumer-group<br>
<img src="../images/08-cleanup-demo/cleanup-07.png"/><br>



