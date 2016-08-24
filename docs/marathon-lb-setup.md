# Schedule a Load Balancer to run on Public Agents

<b>Step 1:</b> To install a load balancer (marathon-lb) on the public agents of the DC/OS cluster navigate to the DC/OS dashboard and click the 'Universe' tab.
<img src="../images/05-marathon-lb-setup/marathon-lb-01.png"/><br>
<br><b>Step 2:</b> Scroll down until you find the <a href="https://github.com/mesosphere/marathon-lb">marathon-lb</a> package and click on the package to install it.
<img src="../images/05-marathon-lb-setup/marathon-lb-02.png"/><br>
<br><b>Step 3:</b> The marathon-lb package is a configuration of <a href="https://www.google.com/#q=haproxy">HAProxy</a> that uses Marathon state.  Click on 'Install Package' to install marathon-lb.
<img src="../images/05-marathon-lb-setup/marathon-lb-03.png"/><br>
<br><b>Step 4:</b> ...
<img src="../images/05-marathon-lb-setup/marathon-lb-04.png"/><br>
<br><b>Step 5:</b> ...
<img src="../images/05-marathon-lb-setup/marathon-lb-05.png"/><br>
<br><b>Step 6:</b> ...
<img src="../images/05-marathon-lb-setup/marathon-lb-06.png"/><br>
<br><b>Step 7:</b> ...
<img src="../images/05-marathon-lb-setup/marathon-lb-07.png"/><br>
<br><b>Step 8:</b> ...
<img src="../images/05-marathon-lb-setup/marathon-lb-08.png"/><br>
<br><b>Step 9:</b> ...
<img src="../images/05-marathon-lb-setup/marathon-lb-09.png"/><br>
<br><b>Step 10:</b> ...
<img src="../images/05-marathon-lb-setup/marathon-lb-10.png"/><br>
<br><b>Step 11:</b> ...
<img src="../images/05-marathon-lb-setup/marathon-lb-11.png"/><br>

<br><br><b>Congratulations:</b> You now have a load balancer (marathon-lb) installed on public agents are ready to load balance application requests on the DC/OS cluster.  Next, we will walk through how to Open up Load Balancer Ports on either <a href="../docs/ports-setup.md">Microsoft Azure or <a href="../docs/ports-amazon-setup.md">Amazon Web Services</a>.



