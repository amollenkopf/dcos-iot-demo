# Configure an Azure Template

The Azure Template for this application was created by hand using Mesosphere's [DC/OS on Azure Template](https://downloads.dcos.io/dcos/stable/azure.html) as the foundation with JSON that was customized using [Azure docs](https://azure.microsoft.com/en-us/resources/templates/) as a guideline.<br>

## Azure Template
- [dcos.json](dcos.json): Used to create compute resources to support as Mesosphere DC/OS installation.
- [add_publicagents.json](add_publicagents.json): Used post-installation to add additional public agents to an existing resource group.
- [add_agents.json](add_agents.json): Used post-installation to add additional private agents to an existing DCOS resource group.

## Importing to Azure
- Log into Azure Account
- Under More Services Search for Templates
- Click Add  **Note:** *This only needs to be done once. If you already see "dcos" template you can stop here.*
- with your desired compute resources.
- Give the Template a Name (e.g. dcos) and Description (e.g. This is the dcos template); Click OK
- Click ARM Template; Replace the default json with the contents of the dcos.json.  Click OK




