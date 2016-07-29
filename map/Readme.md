### Deployment & Configuration

1. Deploy the files in this folder in your clusters web server.
  * If you want to deploy the map web application on an external web server, you need to establish a tunnel from the web server to the {es} instant of the cluster and change the {es} host url as described in 2.
2. On default the app will try to communicate with {es} on http://spatiotemporal-store.elasticsearch.mesos:9200
  * Open the URL in a browser to verify communication with {es} is working
  * If you need to change the {es} host URL go to line 89/90 of the index.html (in this section you can also change the name of the datasource name (index/type) in case you did not use the default taxi name)
  * Optional: To change the FeatureThreshold (value when to switch from aggregated view to individual feature view) edit the number in line 93 of the index.html
  * 
