# Create DC/OS Cluster Azure

The procedure assumes you already [Created the Template](../cloud-templates/azure).

## You'll need a SSH Key Pair
If you don't have one; here is the process to create one.

This can be done from command line on Linux, Mac, or MobaXterm (Windows).
<pre>
$ ssh-keygen
</pre>
- Change the path to the key (e.g. /home/david/azureuser)
- Leave passphrase blank.

NOTE: If you already have a private key (azureuser) with a password you can remove the password with these commands.
<pre>
$ mv azureuser azureuser_withpassword
$ openssl rsa -in azureuser_withpassword -out azureuser
</pre>

Creates two files (azureuser and azureuser.pub)
<br/>
<br/>
<img src="images/azure/000.png"/><br>

## Goto Azure Portal
<img src="images/azure/001.png"/><br>

Click More Services and Search for Templates
<img src="images/azure/002.png"/><br>
Select Templates

## Select the Template 

Use the "dcos" template.

<img src="images/azure/003.png"/><br>

Click Deploy

<img src="images/azure/004.png"/><br>

NOTE: In the image I used centos for my username. This is the default for the scripts; I recommend changing to centos for username and centos.pem; this is what is used be default in Amazon Web Services.  

## Enter Resource Group

Enter Name: (e.g. esri80)

## Under Parameters
- Username (e.g. azureuser)
- Public Key (e.g. contents of azureuser.pub).
- Num Masters (1 is good for dev; 3 for production)
- Master Size (DS3_V2 works)
- Num Agents (5 is good for dev)
- Agent Size (DS4_V2 works)
- Agent Disk Size GB (50 is good for most dev work)
- Num Public Agents (1 is good for dev)
- Public Agent Size (DS3_V2 works)


NOTE: If you hover over the "i"; this will give you a hint.

Scroll down and check "I agree to terms and conditions stated above"

<img src="images/azure/005.png"/><br>
Click Purchase

It will take about 5-10 minutes for the Resource Group to build.

<img src="images/azure/006.png"/><br>

On Azure Portal navigate to find public IP of the "boot" server.

<img src="images/azure/008.png"/><br>

Now you are ready to [install DCOS](dcos.md)


## Removing Cluster
When you want to remove/delete the cluster
- Login to Azure
- Select the Resource Group
- Click Delete
- Enter the Resource Group Name
- Click OK
