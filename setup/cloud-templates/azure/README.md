# Azure Templates

These Azure Templates were created by hand. 

Started with "DC/OS on Azure Template". From the Resource Group under Automation script downloaded the json.

Edited and customized the json using using [Azure docs](https://azure.microsoft.com/en-us/resources/templates/) as a guideline.

## Templates Overview
- [dcos.json](dcos.json): Used to create servers to support Mesosphere DCOS Installation.
- [add_publicagents.json](add_publicagents.json): Used to add additional public agents to an existing DCOS resource group.
- [add_agents.json](add_agents.json): Used to add additional private agents to an existing DCOS resource group.

## Importing to Azure
- Log into Azure Account
- Under More Services Search for Templates
- Click Add  **Note:** *This only needs to be done once. If you already see "dcos" template you can stop here.*
- Give the Template a Name (e.g. dcos) and Description (e.g. This is the dcos template); Click OK
- Click ARM Template; Replace the default json with the contents of the dcos.json.  Click OK

