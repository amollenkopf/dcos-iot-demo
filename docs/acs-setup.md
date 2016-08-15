[Running DC/OS on Microsoft Azure](#azure) or [Amazon](#amazon)<br>

#<a name="azure"></a>Running DC/OS on Microsoft Azure
Microsoft Azure has a capability named Azure Container Service (ACS) that allows you to choose DC/OS as the orchestrator of the virtual machines group created.  This section walks you through step-by-step on how to provision DC/OS on Azure using the Azure Container Service capability.

<br><br>Step 1: Login to <a href="http://portal.azure.com">portal.azure.com</a> using your Microsoft Azure credentials.
<img src="../images/01-acs-setup/acs-create-01.png"/>
<br><br>Step 2: Click the 'New' button in the left hand menu.
<img src="../images/01-acs-setup/acs-create-02.png"/>
<br><br>Step 3: Type 'Azure Container Service'
<img src="../images/01-acs-setup/acs-create-03.png"/>
<br><br>Step 4: Click the 'Azure Container Service' result that has the purple icon.
<img src="../images/01-acs-setup/acs-create-04.png"/>
<br><br>Step 5: Review the 'Azure Container Service' description and click the 'Create' button.
<img src="../images/01-acs-setup/acs-create-05.png"/>
<br><br>Step 6: Fill in the 'Basic' information needed to create 'Azure Container Service' including:
<br>&nbsp;&nbsp;&nbsp;&nbsp; SSH user name & public key, for more info see the <a href="https://azure.microsoft.com/en-us/documentation/articles/virtual-machines-linux-ssh-from-linux/">Azure ssh doc</a>.
<br>&nbsp;&nbsp;&nbsp;&nbsp; select the Azure subscription you wish to associate this Azure Container Service with.
<br>&nbsp;&nbsp;&nbsp;&nbsp; Create a new 'Resource Group' and enter a new unique name, e.g. esri40.
<br>&nbsp;&nbsp;&nbsp;&nbsp; Select the 'Location'/region you wish this Azure Container Service to run in.
<img src="../images/01-acs-setup/acs-create-06.png"/>

#<a name="amazon">Running DC/OS on Amazon
Coming soon ...
