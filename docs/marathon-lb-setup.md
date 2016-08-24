# Schedule a Load Balancer to run on Public Agents

<b>Step 1:</b> To install a load balancer (marathon-lb) on the public agents of the DC/OS cluster navigate to the DC/OS dashboard and click the 'Universe' tab.
<img src="../images/05-marathon-lb-setup/marathon-lb-01.png"/><br>
<br><b>Step 2:</b> Scroll down until you find the <a href="https://github.com/mesosphere/marathon-lb">marathon-lb</a> package and click on the package to install it.
<img src="../images/05-marathon-lb-setup/marathon-lb-02.png"/><br>
<br><b>Step 3:</b> The marathon-lb package is a configuration of <a href="http://www.haproxy.org/">HAProxy</a> that uses Marathon state.  Click on 'Install Package' to start the installation process of marathon-lb.
<img src="../images/05-marathon-lb-setup/marathon-lb-03.png"/><br>
<br><b>Step 4:</b> Click on the 'Advanced Installation' link to review the default configuration of marathon-lb.
<img src="../images/05-marathon-lb-setup/marathon-lb-04.png"/><br>
<br><b>Step 5:</b> review the marathon-lb default values of the configuration properties of marathon-lb.
<img src="../images/05-marathon-lb-setup/marathon-lb-05.png"/><br>
<br><b>Step 6:</b> scroll down to review the remaining marathon-lb default values and then click the 'Review and Install' button.
<img src="../images/05-marathon-lb-setup/marathon-lb-06.png"/><br>
<br><b>Step 7:</b> Review the configuration of marathon-lb one more time then click the 'Install' button to install marathon-lb on your DC/OS cluster.
<img src="../images/05-marathon-lb-setup/marathon-lb-07.png"/><br>
<br><b>Step 8:</b> Close the 'Success!' acknowledgement dialog by clicking the checkmark button.
<img src="../images/05-marathon-lb-setup/marathon-lb-08.png"/><br>
<br><b>Step 9:</b> To verify that marathon-lb was successfully installed navigate to the DC/OS Services tab and open the Marathon UI.
<img src="../images/05-marathon-lb-setup/marathon-lb-09.png"/><br>
<br><b>Step 10:</b> In the Marathon UI we can see a new application listed named 'marathon-lb-default'.
<img src="../images/05-marathon-lb-setup/marathon-lb-10.png"/><br>
<br><b>Step 11:</b> Clicking on the marathon-lb app in the listing we can see more detailed configuration including the range of ports that HAProxy has pre-configured including ports 10000-10100.  These ports are where we will assign any applications that run within the DC/OS cluster that need to have the ability to be accessed externally from the DC/OS cluster.
<img src="../images/05-marathon-lb-setup/marathon-lb-11.png"/><br>

<br><br><b>Congratulations:</b> You now have a load balancer (marathon-lb) installed on public agents are ready to load balance application requests on the DC/OS cluster.  Next, we will walk through how to Open up Load Balancer Ports on either <a href="../docs/ports-setup.md">Microsoft Azure</a> or <a href="../docs/ports-amazon-setup.md">Amazon Web Services</a>.



