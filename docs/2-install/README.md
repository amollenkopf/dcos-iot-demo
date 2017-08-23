# Install DC/OS
This section walks you through step-by-step on how to install a DC/OS environment.  A DC/OS environment consists of 'master' nodes that administer the DC/OS environment and schedule work to run on agents, 'private agent' nodes that have perform work and 'public agent' nodes that are accessable via the public internet.  The # of masters, # of private & public agents will vary depending on your performance & scalability requirements.  The diagram below and used throughout this documentation illustrates DC/OS environment that has 3 master, 30 private agent & 3 public agent (3-30-3) nodes.<br>
<div align="center">
<i>Compute resources provisioned for a 3 master, 30 private agent & 3 public agent (3-30-3) DC/OS environnment:</i>
<img src="00.jpg"/>
</div>

## Prepare for installation
<b>Step 1:</b> Copy your private key to the boot node.<br>
<pre>
$ scp -i {private-key} {private-key} {username}@{boot server ip}:~

Example:
$ scp -i ~/.ssh/dcosiotdemo ~/.ssh/dcosiotdemo cory6458@40.78.18.217:~
</pre>
<img src="01.png">
<br><br><b>Step 2:</b> Copy DC/OS install script to the boot node.<br>
<pre><nowiki>
$ scp -i {private-key} {your-local-path}/dcos-iot-demo/install/install_dcos.sh {username}@{boot server ip}:~

Example:
$ scp -i ~/.ssh/dcosiotdemo ~/iot/dcos-iot-demo/install/install_dcos.sh cory6458@40.78.18.217:~
</nowiki></pre>
<img src="02.png">
<br><br><b>Step 3:</b> Establish a secure connection to the boot node using ssh.<br>
<pre>
$ ssh -i {private-key} {username}@{boot server ip}

Example:
$ ssh -i ~/.ssh/dcosiotdemo cory6458@40.78.18.217
</pre>
<img src="03.png">

## Run installer
<b>Step 4: From the ssh terminal run the DC/OS installer:</b> <br>
<pre>
$ sudo bash install_dcos.sh {number-of-masters} {number-of-private-agents} {number-of-public-agents}

Example (for 1 master, 5 private agents, and 1 public agent):
$ sudo bash install_dcos.sh 1 5 1
</pre>
<img src="04.png">


## Access DC/OS


<br><br><b>Congratulations:</b> You have successfully installed DC/OS.
