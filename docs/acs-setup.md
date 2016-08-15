This section describes [Getting a DC/OS cluster running on Microsoft Azure](#azure) or [Amazon](#amazon).<br>

#<a name="azure"></a>Getting a DC/OS cluster running on Microsoft Azure
Microsoft Azure has a capability named Azure Container Service (ACS) that allows you to choose DC/OS as the orchestrator of the virtual machines group created.  This section walks you through step-by-step on how to provision DC/OS on Azure using the Azure Container Service capability.

<b>Step 1:</b> Login to <a href="http://portal.azure.com">portal.azure.com</a> using your Microsoft Azure credentials.
<img src="../images/01-acs-setup/acs-create-01.png"/>
<br><br><b>Step 2:</b> Click the 'New' button in the left hand menu.
<img src="../images/01-acs-setup/acs-create-02.png"/>
<br><br><b>Step 3:</b> Type 'Azure Container Service'
<img src="../images/01-acs-setup/acs-create-03.png"/>
<br><br><b>Step 4:</b> Click the 'Azure Container Service' result that has the purple icon.
<img src="../images/01-acs-setup/acs-create-04.png"/>
<br><br><b>Step 5:</b> Review the 'Azure Container Service' description and click the 'Create' button.
<img src="../images/01-acs-setup/acs-create-05.png"/>
<br><br><b>Step 6:</b> Create a SSH key that will enable you to access the 'Azure Container Service' once it has been created.<ul><li>for more info see the <a href="https://azure.microsoft.com/en-us/documentation/articles/virtual-machines-linux-ssh-from-linux/">Azure ssh doc</a>.</li></ul>
<img src="../images/01-acs-setup/acs-create-06.png"/>
<br><br><b>Step 7:</b> Fill in the 'Basic' information needed to create the 'Azure Container Service' including:<ul>
<li>SSH user name & public key, more ~/.ssh/azureuser.pub and very carefully cut and paste the public key contents.</li>
<li>select the Azure subscription you wish to associate this Azure Container Service with.</li>
<li>create a new 'Resource Group' and enter a new unique name, e.g. esri40.</li>
<li>select the 'Location'/region you wish this Azure Container Service to run in.</li></ul>
<img src="../images/01-acs-setup/acs-create-07.png"/>
<br><br><b>Step 8:</b> In the 'Framework configuration' section choose 'DC/OS' as the Orchestrator configuration and click the 'OK' button.
<img src="../images/01-acs-setup/acs-create-08.png"/>
<br><br><b>Step 9:</b> Fill in the 'Azure Container service settings' information needed to create the 'Azure Container Service' including:<ul>
<li>Set the 'Agent count' to the number of private agents you desire for your cluster</li>
<img src="../images/01-acs-setup/acs-create-09.png"/>
<br><br><li>Click on 'Agent virtual machine size' to choose the VM size you would like your private agents to be.</li>
<img src="../images/01-acs-setup/acs-create-10.png"/>
<br><br><li>Click the 'View all' link to browse through all available VM sizes.</li>
<li>Select the VM size you desire and click the 'Select' button.</li>
<img src="../images/01-acs-setup/acs-create-11.png"/>
<br><br><li>Set the 'Master count' to the amount you desire.</li>
<li>Specify a new unique value for the 'DNS prefix for container service' field and hit the 'OK' button.</li></ul>
<img src="../images/01-acs-setup/acs-create-12.png"/>
<br><br><b>Step 10:</b> Review the 'Summary' section, make sure that the 'Validation passed', and hit the 'OK' button.
<img src="../images/01-acs-setup/acs-create-13.png"/>
<br><br><b>Step 11:</b> Carefully review the 'Buy' section and if you are ok with the pricing & terms of use hit the 'Purchase' button.
<img src="../images/01-acs-setup/acs-create-14.png"/>
<br><br><b>Step 12:</b> Wait patiently for between 10-15 minutes for your 'Azure Container Service' to be provisioned.
<img src="../images/01-acs-setup/acs-create-15.png"/>
<br><br><b>Step 13:</b> You will know your 'Azure Container Service' has been succefully created when you see the 'Resource group' appear.
<img src="../images/01-acs-setup/acs-create-16.png"/>
<br><br><b>Step 14:</b> Scroll down in the 'Resource group' to find the entry marked 'Public IP address' for the DC/OS master(s) and click it.
<img src="../images/01-acs-setup/acs-create-17.png"/>
<br><br><b>Step 15:</b> Wait a few seconds for it to appear, then hover the 'DNS name' and click the copy to clipboard icon.
<img src="../images/01-acs-setup/acs-create-18.png"/>
<br><br><b>Congratulations:</b> You now have an 'Azure Container Service' in place on Microsoft Azure that is configured to orchestrate using DC/OS.  Next, we will walk through <a href="../docs/dcos-mesos-explore.md">Exploring the DC/OS and Mesos dashboards</a>

<br><br>
#<a name="amazon">Getting a DC/OS cluster running on Amazon
Coming soon ...
