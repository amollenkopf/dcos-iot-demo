# Explore the DC/OS and Mesos dashboards<br>

The DC/OS and Mesos dashboards allows you to visualize what has been allocated on the cluster, enables you to manage <a href="https://github.com/mesosphere/universe/tree/version-3.x/repo/packages">packages (Mesos frameworks)</a> that you enable the cluster can use, and to schedule tasks to run on the cluster.

This section provides a brief walk through of the DC/OS & Mesos dashboards and describes what information can be seen and what actions can be performed.

<b>Step 1:</b> Connect to your DC/OS dashboard:
* On Azure, you first need to establish an SSH tunneling session.  To establish a secure SSH tunnel you should use the SSH key created in the previous section along with the value of the 'Public IP address' 'DNS name' of the Mesos master(s).
<img src="../images/01-acs-setup/acs-create-20.png"/><br><br>
* On Azure, with the SSH tunnel in place you can connect to the DC/OS dashboard with <a href="http://localhost:9001">http://localhost:9001</a>.
<img src="../images/01-acs-setup/acs-create-21.png"/>
<br><br>
* On Amazon, obtain the public IP address of your master(s) and connect with &lt;your master url&gt;.
<br><br><b>Step 2:</b> text:
<img src="../images/01-acs-setup/acs-create-22.png"/>
<br><br><b>Step 3:</b> text:
<img src="../images/01-acs-setup/acs-create-23.png"/>

