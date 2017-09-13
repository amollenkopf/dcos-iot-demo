## Applying cleanup procedures between demo runs
<br><b>Step 1:</b> Destroy taxi-source, wait for taxi-stream to finish processing<br>
<img src="01.gif"/><br>

<br><b>Step 2:</b> Destroy taxi-stream<br>
<img src="02.gif"/><br>

<br><b>Step 3:</b> ssh -i ~/.ssh/dcosiotdemo -L 9200:coordinator.elastic.l4lb.thisdcos.directory:9200 cory6458@40.78.19.245<br>
<img src="03.gif"/><br>

<br><b>Step 4:</b> curl -XGET 'localhost:9200/taxi/_count’<br>
<img src="04.gif"/><br>

<br><b>Step 5:</b> curl -XDELETE 'localhost:9200/taxi?pretty'<br>
<img src="05.gif"/><br>

<br><b>Step 6:</b> Deploy taxi-stream<br>
<img src="06.gif"/><br>

<br><b>Step 7:</b> Deploy taxi-source<br>
<img src="07.gif"/><br>

<br><b>Step 8:</b> Refresh map-webapp<br>
<img src="08.gif"/><br>

