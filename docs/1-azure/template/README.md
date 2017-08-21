# Configure an Azure Template

The Azure Template for this application was created by hand using Mesosphere's <a href="https://downloads.dcos.io/dcos/stable/azure.html">DC/OS on Azure Template</a> as the foundation with JSON that was customized using [Azure docs](https://azure.microsoft.com/en-us/resources/templates/) as a guideline.<br><br>

## Azure Template
- [dcos.json](dcos.json): Used to create compute resources to support Mesosphere DC/OS installation.

## Importing to Azure
- Log into Azure Account
- Under More Services Search for Templates
- Click Add  **Note:** *This only needs to be done once. If you already see "dcos" template you can stop here.*
- with your desired compute resources.
- Give the Template a Name (e.g. dcos) and Description (e.g. This is the dcos template); Click OK
- Click ARM Template; Replace the default json with the contents of the dcos.json.  Click OK




