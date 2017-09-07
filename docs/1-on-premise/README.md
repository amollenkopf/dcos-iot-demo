# On-Premise (Advanced)

This section provides details on how to provision and install DC/OS on premise. 

## Pre-requisites
- Computers have access to the Internet
- Each computer should have at least 4 cores; 8GB memory; 50GB Hard Drives 
- Computers installed with "Min" install of [CentOS 7.3](https://www.centos.org/). Use the DVD iso; build 1611.
  - Use standard partitions instead of LVM
  - Give root partition most of disk space instead (default install creates a large /home partition)
- Configure the computers with static IP

## Configure each Computer

Login to each computer and run the following commands.  These commands install bash-completion; stop and disable firewall; disable selinux; and modify the sudoer file.

<pre>
# yum -y install bash-completion
# systemctl stop firewalld.service
# systemctl disable firewalld.service
# sed -i s/=enforcing/=disabled/g /etc/selinux/config
# sed -i -e 's/^%wheel/#%wheel/g' -e 's/^# %wheel/%wheel/g' /etc/sudoers
</pre>

## Create Centos User

### On first computer 

This will be the "boot" server.  

Create a key pair

Run the following command and change the path to the key (e.g. /home/david/centos.pem). 

<pre>
$ ssh-keygen
</pre>

You'll need to remove the password from the private key.

<pre>
$ mv centos.pem centos.pem.withpassword
$ openssl rsa -in centos.pem.withpassword -out centos.pem
</pre>

### On all computers 

Copy the centos.pem.pub file to root's home.  For example:

<pre>
# scp centos.pem.pub root@<computer's ip>:~
</pre>

Run these commands as root to create the user and configure pki access for centos user.

<pre>
useradd centos
usermod -aG wheel centos
mkdir /home/centos/.ssh/
chown centos. /home/centos/.ssh/
chmod 700 /home/centos/.ssh
cp centos.pem.pub /home/centos/.ssh/authorized_keys
chown centos. /home/centos/.ssh/authorized_keys
</pre>

### Test Centos

You should now be able to login to the servers with the pki key from the boot server.

<pre>
ssh -i centos.pem centos@<computer's ip>
</pre>

## Edit hosts file

### On the boot server modify the /etc/hosts file.

Add a line for each computer you want to be in the cluster.  For example:

<pre>
192.168.0.130   boot
192.168.0.131   m1
192.168.0.141   a1
192.168.0.142   a2
192.168.0.143   a3
192.168.0.144   a4
192.168.0.145   a5
192.168.0.146   a6
192.168.0.147   a7
192.168.0.148   a8
192.168.0.151   p1
</pre>

- Use the static IP's you have assigned to each computer
- For boot server; min requirements are sufficient
- For masters name them starting from m1. If you want more name them consecutively m2,m3,...
  -- At least one Master
  -- You should use an odd number of masters (e.g. 1,3,5)
- For private agents name them consecutively a1,a2,a3,... 
  -- These are the worker agents 
  -- Use more powerful computers here. Faster cores, memory, and disk will improve performance.
- For public agents name them consecutively p1,p2,p3,...  
  -- You can run DC/OS with no public agents
  -- At least one public agent is required if you use Marathon-LB

### Update /etc/hosts file to other computers

You can manually copy (scp) the /etc/hosts to each computer or use a script like:

<pre>
#!/bin/bash

for a in $(cat /etc/hosts | grep ^192 | cut -d ' ' -f2 | grep -v boot)
do
	echo Updating /etc/hosts on $a
	scp -i centos.pem /etc/hosts centos@${server}:~
        ssh -t -i centos.pem centos@${server}  'sudo cp /home/centos/hosts /etc/hosts'
done
</pre>

This script assumes that all of the private IP's start with 192 and the second file (f2) is the name of the server (e.g. m1,a2,etc)

## Install DC/OS

### Customize the Script

The installation script for on-premise is slightly different from the one used for cloud providers.  

You will need to review and tweak the script for you're specific on-premise configuration.  

Here are two example
- [install_dcos_onpremise.sh](install_dcos_onpremise.sh)
  - Assumes a specific name of NETWORK_DEVICE (e.g. enp0s8)
- [install_dcos_onpremise2.sh](install_dcos_onpremise2.sh)
  - Uses a NETWORK_MASK (e.g. 10.\*/16). This is used to determine the network device name.

### Run Install Script

When you run the installer you should see output like:
<pre>
$ sudo bash install_dcos_onpremise.sh 1 8 1 
1) Latest Community Edition  3) Version 1.9.0
2) Version 1.9.1	     4) Custom
Which version of DCOS do you want to install: 1
Enter OS Username (centos): 
Enter PKI Filename (centos.pem): 

Install Details
DCOS_URL:  https://downloads.dcos.io/dcos/stable/dcos_generate_config.sh
OS Username:  centos
PKI Filename:  centos.pem
Number of Masters:  1
Number of Agents:  8
Number of Public Agents: 1

Press Enter to Continue or Ctrl-C to abort

Start Boot Setup
Boot Setup should take about 5 minutes. If it takes longer than 10 minutes then use Ctrl-C to exit this Script and review the log files (e.g. boot.log)
master_list
- 192.168.0.131

Boot Setup Complete
Time Elapsed (Seconds):  190

Installing DC/OS.
This should take about 5 minutes or less. If it takes longer than 10 minutes then use Ctrl-C to exit this Script and review the log files (e.g. m1.log)
.................................................................................Boot Server Installation (sec): 190
DCOS Installation (sec): 406
Total Time (sec): 596

DCOS is Ready
</pre>

## Done
You now have DC/OS running on-premise.

## Add Additional Agents  (Optional)

### Modify the hosts file 
Add the new node to the hosts file on boot server and update on all clients.

### Login to New Computer
SSH to the new computer.

### Copy Installer from Boot
<pre>
sudo curl -O http://boot/install.sh
</pre>

Verify contents of install.sh

<pre>
cat install.sh
</pre>

You should see the contents of the script; not an error message.

### Run install.sh

<pre>
sudo bash install.sh slave
</pre>

Use "slave" for private agents or "slave_public" for public agents.

After script completes the new computer should appear in DC/OS. 








