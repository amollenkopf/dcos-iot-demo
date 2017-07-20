# Create Cloud Formation Template
#
# Author: David Jennings
# Date: 16 Feb 2017
#
# Requires: https://github.com/cloudtools/troposphere
# Description: Creates Cloudformation Template to create a Stack Suitable for Advanced install of DCOS

from troposphere import Base64, FindInMap, GetAtt, Join, Output, GetAZs
from troposphere import Parameter, Ref, Tags, Template
from troposphere.autoscaling import Metadata
from troposphere.ec2 import PortRange, NetworkAcl, Route, \
    VPCGatewayAttachment, SubnetRouteTableAssociation, Subnet, RouteTable, \
    VPC, NetworkInterfaceProperty, NetworkAclEntry, \
    SubnetNetworkAclAssociation, EIP, Instance, InternetGateway, \
    SecurityGroupRule, SecurityGroup, NatGateway, Volume, VolumeAttachment, \
    BlockDeviceMapping, EBSBlockDevice
from troposphere.policies import CreationPolicy, ResourceSignal
from troposphere.cloudformation import Init, InitFile, InitFiles, \
    InitConfig, InitService, InitServices
import troposphere.elasticloadbalancing as elb

# **********************************************************************************
# Call create_template from the main function at the bottom of this Python script
# **********************************************************************************


useNatInstance=True

def create_template(num_masters, num_agents, num_publicAgents):

    #outfilename = "test.json"
    outfilename = "cf_" + str(num_masters) + "." + str(num_agents) + "." + str(num_publicAgents) + ".json"

    # Create the Template
    t = Template()

    t.add_version('2010-09-09')

    t.add_description('Creates a set of Servers for DC/OS using CentOS 7.3 AMI.  Creates a boot server to host the DC/OS installer and a NAT Instance for outbound connections from private agents.  Creates ' + str(num_masters) + ' Master(s), '  \
                      + str(num_agents) + ' Private Agent(s), and ' + str(num_publicAgents) + ' Public Agent(s).  After creating the Stack; Log into the boot server and run the DCOS Bash Script installer for AWS')

    # Amazon Linux AMI 2016.09.1.20170119 x86_64 VPC NAT HVM EBS
    # amzn-ami-vpc-nat-hvm-2016.09.1.20170119-x86_64-ebs - 
    # ami-dd3dd7cb us-east-1 (N. Virginia)
    # ami-564b6e33  us-east-2 (Ohio)
    # ami-7d54061d us-west-1 (N. Cal)
    # ami-3b6fd05b us-west-2 (Oregon)
    t.add_mapping('NATAmi', {
        'us-east-1': {'default': 'ami-dd3dd7cb'},
        'us-east-2': {'default': 'ami-564b6e33'},
        'us-west-1': {'default': 'ami-7d54061d'},
        'us-west-2': {'default': 'ami-3b6fd05b'},    
        
    })

    # The c73 AMI pre created and deployed on each region
    t.add_mapping('c73Ami', {
        'us-east-1': {'default': 'ami-46c1b650'},
        'us-east-2': {'default': 'ami-18f8df7d'},
        'us-west-1': {'default': 'ami-f5d7f195'},
        'us-west-2': {'default': 'ami-f4533694'},    
        
    })

    # CloudFormation Parameters

    # Sometimes when I deployed stack on us-east-1; it would fail on av zone us-east-1c with error messages instance type not support on this AZ.  I added this parameter to fix all of the components in on AZ for now
    avzone_param = t.add_parameter(Parameter(
        "AVZoneName",
        ConstraintDescription='Must be the name of an an Availability Zone',
        Description='Name of an Availability Zone',
        Type='AWS::EC2::AvailabilityZone::Name',
        ))

    # Every agent will get a data drive of this size
    dataDriveSizeGB_param = t.add_parameter(Parameter(
        "dataDriveSizeGB",
        Default="100",
        MinValue=20,
        MaxValue=1000,
        Description='Size of data drive to add to private agents from 20 to 1000GB',
        Type='Number'
        ))

    # The key will be added to the centos user so you can login to centos using the key
    keyname_param = t.add_parameter(Parameter(
        "KeyName",
        ConstraintDescription='Must be the name of an existing EC2 KeyPair.',
        Description='Name of an existing EC2 KeyPair to enable SSH access to the instance',
        Type='AWS::EC2::KeyPair::KeyName',
        ))

    # While you can allow everyone it's more secure to just allow a single machine or subnet of machines; web port will also be opened to this CIDR
    sshlocation_param = t.add_parameter(Parameter(
        "sshlocation",
        Type="String",
        Description="Subnet allowed to ssh to these servers. 0.0.0.0/0 to allow all."
        ))

    # Instance type for Master
    instanceTypeMaster_param = t.add_parameter(Parameter(
        'InstanceTypeMaster',
        Type='String',
        Description='EC2 instance type for ' + str(num_masters) + ' Masters(s)',    
        Default='m4.xlarge',
        AllowedValues=[
            't2.xlarge', 't2.2xlarge',
            'm4.xlarge', 'm4.2xlarge',
            'm4.4xlarge', 'm4.10xlarge',
            'c4.xlarge', 'c4.2xlarge',
            'c4.4xlarge', 'c4.8xlarge',
        ],
        ConstraintDescription='Must be a valid EC2 instance type.',
    ))

    # Instance type for Agents
    instanceTypeAgent_param = t.add_parameter(Parameter(
        'InstanceTypeAgent',
        Type='String',
        Description='EC2 instance type for ' + str(num_agents) + ' Private Agent(s)',
        Default='m4.2xlarge',
        AllowedValues=[
            't2.xlarge', 't2.2xlarge',
            'm4.xlarge', 'm4.2xlarge',
            'm4.4xlarge', 'm4.10xlarge',
            'c4.xlarge', 'c4.2xlarge',
            'c4.4xlarge', 'c4.8xlarge',
        ],
        ConstraintDescription='Must be a valid EC2 instance type.',
    ))

    # Instance type for Public Agents
    instanceTypePublicAgent_param = t.add_parameter(Parameter(
        'InstanceTypePublicAgent',
        Type='String',
        Description='EC2 instance type for ' + str(num_publicAgents) + ' Public Agent(s)',
        Default='m4.xlarge',
        AllowedValues=[
            't2.xlarge', 't2.2xlarge',
            'm4.xlarge', 'm4.2xlarge',
            'm4.4xlarge', 'm4.10xlarge',
            'c4.xlarge', 'c4.2xlarge',
            'c4.4xlarge', 'c4.8xlarge',
        ],
        ConstraintDescription='Must be a valid EC2 instance type.',
    ))


    # Adding Resources

    ref_stack_id = Ref('AWS::StackId')
    ref_region = Ref('AWS::Region')
    ref_stack_name = Ref('AWS::StackName')


    # Create VPC
    nm='vpc'
    vpc = t.add_resource(
        VPC(
            nm,
            CidrBlock='10.10.0.0/16',
            EnableDnsSupport=True,
            EnableDnsHostnames=True,      
            Tags=Tags(Application=ref_stack_id,Name=Join("",[Ref('AWS::StackName'),"-",nm]))
        ))


    # Create Subnet for Masters 
    nm='mastersSubnet'
    subnetMasters = t.add_resource(
        Subnet(
            nm,
            AvailabilityZone=Ref(avzone_param),
            CidrBlock='10.10.0.0/24',
            VpcId=Ref(vpc),
            Tags=Tags(Application=ref_stack_id,Name=Join("",[Ref('AWS::StackName'),"-",nm]))
        ))

    # Create Subnet for Agents
    nm='agentsSubnet'
    subnetAgents = t.add_resource(
        Subnet(
            nm,
            AvailabilityZone=Ref(avzone_param),
            CidrBlock='10.10.16.0/24',
            VpcId=Ref(vpc),
            Tags=Tags(Application=ref_stack_id,Name=Join("",[Ref('AWS::StackName'),"-",nm]))
        ))

    # Create Subnet for Public Agents
    nm='publicAgentsSubnet'
    subnetPublicAgents = t.add_resource(
        Subnet(
            nm,
            AvailabilityZone=Ref(avzone_param),
            CidrBlock='10.10.32.0/24',
            VpcId=Ref(vpc),
            Tags=Tags(Application=ref_stack_id,Name=Join("",[Ref('AWS::StackName'),"-",nm]))
        ))

    # Create Gateway; route to the outside world (Internet)
    nm='ig'
    internetGateway = t.add_resource(
        InternetGateway(
            nm,
            Tags=Tags(Application=ref_stack_id,Name=Join("",[Ref('AWS::StackName'),"-",nm]))
        ))

    # Attach Gateway to VPC
    nm='gatewayAttachment'
    gatewayAttachment = t.add_resource(
        VPCGatewayAttachment(
            nm,
            VpcId=Ref(vpc),
            InternetGatewayId=Ref(internetGateway)
        ))


    # Create Route Table
    nm='routeTable'
    routeTable = t.add_resource(
        RouteTable(
            nm,
            VpcId=Ref(vpc),
            Tags=Tags(Application=ref_stack_id,Name=Join("",[Ref('AWS::StackName'),"-",nm]))
        ))

    # Add Routes

    # Allow all outbound traffic
    nm='route'
    route = t.add_resource(
        Route(
            nm,
            DependsOn=gatewayAttachment.title,
            GatewayId=Ref(internetGateway),
            DestinationCidrBlock='0.0.0.0/0',
            RouteTableId=Ref(routeTable),
        ))


    # Associate RouteTable to Master and Public Subnets
    nm='subnetRTAMasters'
    subnetRouteTableAssociation = t.add_resource(
        SubnetRouteTableAssociation(
            nm,
            SubnetId=Ref(subnetMasters),
            RouteTableId=Ref(routeTable),
        ))

    nm='subnetRTAPublicAgents'
    subnetRouteTableAssociation = t.add_resource(
        SubnetRouteTableAssociation(
            nm,
            SubnetId=Ref(subnetPublicAgents),
            RouteTableId=Ref(routeTable),
        ))

    # Create Security Group (General access to ssh and internal connectionsn between masters, agents, and public agents)
    nm='securityGroup'
    securityGroup = t.add_resource(
        SecurityGroup(
            nm,
            GroupDescription='Security Group',
            SecurityGroupIngress=[
                SecurityGroupRule(
                    IpProtocol='tcp',
                    FromPort='22',
                    ToPort='22',
                    CidrIp=Ref(sshlocation_param)),
                SecurityGroupRule(
                    IpProtocol='-1',
                    CidrIp='10.10.0.0/16')
                ],
            VpcId=Ref(vpc),
            Tags=Tags(Application=ref_stack_id,Name=Join("",[Ref('AWS::StackName'),"-",nm]))        
            
        ))

    # Create Security Group Public Agents
    nm='securityGroupPublicAgents'
    publicAgentsSG = t.add_resource(
        SecurityGroup(
            nm,
            GroupDescription='Security Group Public Agents',
            SecurityGroupIngress=[
                SecurityGroupRule(                
                    IpProtocol='tcp',
                    FromPort='80',
                    ToPort='80',                
                    CidrIp='0.0.0.0/0'),
                SecurityGroupRule(                
                    IpProtocol='tcp',
                    FromPort='443',
                    ToPort='443',                
                    CidrIp='0.0.0.0/0'),                
                SecurityGroupRule(                
                    IpProtocol='tcp',
                    FromPort='10000',
                    ToPort='10010',                
                    CidrIp='0.0.0.0/0'),
                SecurityGroupRule(                
                    IpProtocol='tcp',
                    FromPort='9090',
                    ToPort='9090',                
                    CidrIp='0.0.0.0/0')            
                ],
            VpcId=Ref(vpc),
            Tags=Tags(Application=ref_stack_id,Name=Join("",[Ref('AWS::StackName'),"-",nm]))        
            
        ))


    # Create Security Group Masters Allow Access from sshlocation param as test
    nm='securityGroupMasters'
    mastersSG = t.add_resource(
        SecurityGroup(
            nm,
            GroupDescription='Security Group Masters',
            SecurityGroupIngress=[
                SecurityGroupRule(                
                    IpProtocol='tcp',
                    FromPort='80',
                    ToPort='80',                
                    CidrIp=Ref(sshlocation_param)),
                SecurityGroupRule(                
                    IpProtocol='tcp',
                    FromPort='443',
                    ToPort='443',                
                    CidrIp=Ref(sshlocation_param))
                ],
            VpcId=Ref(vpc),
            Tags=Tags(Application=ref_stack_id,Name=Join("",[Ref('AWS::StackName'),"-",nm]))        
            
        ))

    
    if useNatInstance:
        # **** Also change in natRoute ****
        # Create NAT instance; This allows private agents to get out to the Internet
        nm='nat'
        nat = t.add_resource(
            Instance(
                nm,
                SourceDestCheck="false",
                ImageId=FindInMap("NATAmi", Ref("AWS::Region"), "default"),
                InstanceType="m4.large",
                AvailabilityZone=Ref(avzone_param),
                KeyName=Ref(keyname_param),
                DependsOn=internetGateway.title,
                NetworkInterfaces=[
                    NetworkInterfaceProperty(
                        GroupSet=[
                            Ref(securityGroup)],
                        AssociatePublicIpAddress='true',
                        DeviceIndex='0',
                        DeleteOnTermination='true',
                        SubnetId=Ref(subnetMasters),
                        PrivateIpAddress='10.10.0.9')],
                BlockDeviceMappings=[
                    BlockDeviceMapping(
                        DeviceName="/dev/xvda",
                        Ebs=EBSBlockDevice(
                            DeleteOnTermination='true',
                            ))],                            
                Tags=Tags(Application=ref_stack_id,Name=Join("",[Ref('AWS::StackName'),"-",nm]))           
            ))
    else:
        # Create Elastic IP for NatGateay        
        nm='natIP'
        nat_eip = t.add_resource(
            EIP(
                nm,
                Domain="vpc",     
            ))

        # Create NAT Gateway
        nm='natGateway'
        nat = t.add_resource(
            NatGateway(
                nm,
                AllocationId=GetAtt(nat_eip, 'AllocationId'),
                SubnetId=Ref(subnetMasters),
            ))
        
    
    

    # Create Route Table for NAT
    nm='natRouteTable'
    routeTableNAT = t.add_resource(
        RouteTable(
            nm,
            VpcId=Ref(vpc),
            Tags=Tags(Application=ref_stack_id,Name=Join("",[Ref('AWS::StackName'),"-",nm]))                   
        ))

    # Associate Agent Subnet to NAT
    nm='subnetRTAAgents'
    subnetRouteTableAssociation = t.add_resource(
        SubnetRouteTableAssociation(
            nm,
            SubnetId=Ref(subnetAgents),
            RouteTableId=Ref(routeTableNAT),
        ))



    # Add Routes (Agents can reach out anywhere)
    nm='natRoute'
    if useNatInstance:        
        route = t.add_resource(
            Route(
                nm,
                RouteTableId=Ref(routeTableNAT),
                DestinationCidrBlock='0.0.0.0/0',                
                InstanceId=Ref(nat),  
            ))
    else:
        route = t.add_resource(
            Route(
                nm,
                RouteTableId=Ref(routeTableNAT),
                DestinationCidrBlock='0.0.0.0/0',                
                NatGatewayId=Ref(nat),
            ))
        

    # ****************************************
    # NOTE: I am using static PrivateIPAddresses; this may not be a good choice; however, it simplified the install script.  The range of IP's for the master and agents are limited to 24 subnet and I start at 11
    #      With this configuration the max number of agents is around 240.  
    # ****************************************


    # Create boot instance
    # Installs on AWS so far have taken longer than on Azure.  Takes about 10 minutes for the boot server to configure.
    # Tried several InstanceType from t2.micro to m4.large; all take about 10 minutes for boot to load.  The docker start of mesosphere/dcos-genconf seems to be taking longer than it did on azure.
    nm='boot'
    boot = t.add_resource(
        Instance(
            nm,
            ImageId=FindInMap("c73Ami", Ref("AWS::Region"), "default"),
            InstanceType="m4.xlarge",
            AvailabilityZone=Ref(avzone_param),
            KeyName=Ref(keyname_param),
            NetworkInterfaces=[
                NetworkInterfaceProperty(
                    GroupSet=[
                        Ref(securityGroup)],
                    AssociatePublicIpAddress='true',
                    DeviceIndex='0',
                    DeleteOnTermination='true',
                    SubnetId=Ref(subnetMasters),
                    PrivateIpAddress='10.10.0.10')],
                BlockDeviceMappings=[
                    BlockDeviceMapping(
                        DeviceName="/dev/sda1",
                        Ebs=EBSBlockDevice(
                            VolumeSize="100",
                            DeleteOnTermination='true',
                            ))],                  
            Tags=Tags(Application=ref_stack_id,Name=Join("",[Ref('AWS::StackName'),"-",nm]))
        ))


    # Create master instance(s)
    masters = []
    i = 1
    while i <= num_masters:
        nm='m' + str(i)
        private_ip = "10.10.0." + str(i+10)
        instance = t.add_resource(
            Instance(
                nm,
                ImageId=FindInMap("c73Ami", Ref("AWS::Region"), "default"),
                InstanceType=Ref(instanceTypeMaster_param),
                AvailabilityZone=Ref(avzone_param),
                KeyName=Ref(keyname_param),
                NetworkInterfaces=[
                    NetworkInterfaceProperty(
                        GroupSet=[
                            Ref(securityGroup),
                            Ref(mastersSG)],
                        AssociatePublicIpAddress='true',
                        DeviceIndex='0',
                        DeleteOnTermination='true',
                        SubnetId=Ref(subnetMasters),
                        PrivateIpAddress=private_ip)],
                BlockDeviceMappings=[
                    BlockDeviceMapping(
                        DeviceName="/dev/sda1",
                        Ebs=EBSBlockDevice(
                            VolumeSize="100",
                            DeleteOnTermination='true',
                            ))],                                  
                Tags=Tags(Application=ref_stack_id,Name=Join("",[Ref('AWS::StackName'),"-",nm]))
            ))
        masters.append(instance)
        i += 1


    # Create agent instance(s)
    i = 1
    while i <= num_agents:
            
        nm='a' + str(i)
        private_ip = "10.10.16." + str(i+10)

        instance = t.add_resource(
            Instance(
                nm,
                ImageId=FindInMap("c73Ami", Ref("AWS::Region"), "default"),
                InstanceType=Ref(instanceTypeAgent_param),
                AvailabilityZone=Ref(avzone_param),
                KeyName=Ref(keyname_param),
                NetworkInterfaces=[
                    NetworkInterfaceProperty(
                        GroupSet=[
                            Ref(securityGroup)],
                        AssociatePublicIpAddress='false',
                        DeviceIndex='0',
                        DeleteOnTermination='true',
                        SubnetId=Ref(subnetAgents),
                        PrivateIpAddress=private_ip)],
                BlockDeviceMappings=[
                    BlockDeviceMapping(
                        DeviceName="/dev/sda1",
                        Ebs=EBSBlockDevice(
                            VolumeSize="100",
                            DeleteOnTermination='true',
                            ))],                                  
                Tags=Tags(Application=ref_stack_id,Name=Join("",[Ref('AWS::StackName'),"-",nm]))
            ))


        volume = t.add_resource(
            Volume(
                nm+"data",
                AvailabilityZone=Ref(avzone_param),
                Size=Ref(dataDriveSizeGB_param),
                Tags=Tags(Application=ref_stack_id,Name=Join("",[Ref('AWS::StackName'),"-",nm+"data"]))
            ))

        volattach = t.add_resource(
            VolumeAttachment(
                nm+"dataattach",
                InstanceId=Ref(instance),
                VolumeId=Ref(volume),
                Device="/dev/sdc"
            ))
        
        i += 1


    # Create public agent instance(s)
    publicAgents = []
    i = 1
    nm="p1"
    while i <= num_publicAgents:
        nm='p' + str(i)
        private_ip = "10.10.32." + str(i+10)
        instance = t.add_resource(
            Instance(
                nm,
                ImageId=FindInMap("c73Ami", Ref("AWS::Region"), "default"),
                InstanceType=Ref(instanceTypePublicAgent_param),
                AvailabilityZone=Ref(avzone_param),
                KeyName=Ref(keyname_param),
                NetworkInterfaces=[
                    NetworkInterfaceProperty(
                        GroupSet=[
                            Ref(securityGroup),
                            Ref(publicAgentsSG)],
                        AssociatePublicIpAddress='true',
                        DeviceIndex='0',
                        DeleteOnTermination='true',
                        SubnetId=Ref(subnetPublicAgents),
                        PrivateIpAddress=private_ip)],
                BlockDeviceMappings=[
                    BlockDeviceMapping(
                        DeviceName="/dev/sda1",
                        Ebs=EBSBlockDevice(
                            VolumeSize="100",                                                  
                            DeleteOnTermination='true',
                            ))],                             
                Tags=Tags(Application=ref_stack_id,Name=Join("",[Ref('AWS::StackName'),"-",nm]))
            ))
        publicAgents.append(instance)
        i += 1    


    # Load Balancer Masters
    nm="masters"
    elasticLBMasters = t.add_resource(elb.LoadBalancer(
        nm,
        Instances=[Ref(r) for r in masters],
        Subnets=[Ref(subnetMasters)],
        SecurityGroups=[Ref(mastersSG)],
        CrossZone=False,
        Listeners=[
            elb.Listener(
                LoadBalancerPort="80",
                InstancePort="80",
                Protocol="TCP",
            ),
            elb.Listener(
                LoadBalancerPort="443",
                InstancePort="443",
                Protocol="TCP",
            ),        
        ],
        # Health Checking on port 80 which should be there after DCOS has been installed.
        HealthCheck=elb.HealthCheck(
            Target="TCP:80",
            HealthyThreshold="2",
            UnhealthyThreshold="2",
            Interval="30",
            Timeout="5",
        ),
        Tags=Tags(Application=ref_stack_id,Name=Join("",[Ref('AWS::StackName'),"-",nm]))
    ))

    # Load Balancer Public Agents
    nm="publicagents"
    elasticLBPublicAgents = t.add_resource(elb.LoadBalancer(
        nm,
        #AvailabilityZones=GetAZs(""),
        Instances=[Ref(r) for r in publicAgents],
        Subnets=[Ref(subnetPublicAgents)],
        SecurityGroups=[Ref(publicAgentsSG)],
        CrossZone=False,
        Listeners=[
            elb.Listener(
                LoadBalancerPort="10000",
                InstancePort="10000",
                Protocol="TCP",
            ),
            elb.Listener(
                LoadBalancerPort="10001",
                InstancePort="10001",
                Protocol="TCP",
            ),
            elb.Listener(
                LoadBalancerPort="10002",
                InstancePort="10002",
                Protocol="TCP",
            ),
            elb.Listener(
                LoadBalancerPort="10003",
                InstancePort="10003",
                Protocol="TCP",
            ),
            elb.Listener(
                LoadBalancerPort="10004",
                InstancePort="10004",
                Protocol="TCP",
            ),
            elb.Listener(
                LoadBalancerPort="10005",
                InstancePort="10005",
                Protocol="TCP",
            ),
            elb.Listener(
                LoadBalancerPort="10006",
                InstancePort="10006",
                Protocol="TCP",
            ),
            elb.Listener(
                LoadBalancerPort="10007",
                InstancePort="10007",
                Protocol="TCP",
            ),
            elb.Listener(
                LoadBalancerPort="10008",
                InstancePort="10008",
                Protocol="TCP",
            ),
            elb.Listener(
                LoadBalancerPort="10009",
                InstancePort="10009",
                Protocol="TCP",
            ),
            elb.Listener(
                LoadBalancerPort="10010",
                InstancePort="10010",
                Protocol="TCP",
            ),
            elb.Listener(
                LoadBalancerPort="9090",
                InstancePort="9090",
                Protocol="TCP",
            ),
            elb.Listener(
                LoadBalancerPort="80",
                InstancePort="80",
                Protocol="TCP",
            ),        
            elb.Listener(
                LoadBalancerPort="443",
                InstancePort="443",
                Protocol="TCP",
            )        
        ],
        # I've added health check for port 9090; becomes healthy after Marathon-LB is installed.
        HealthCheck=elb.HealthCheck(
            Target="TCP:9090",
            HealthyThreshold="2",
            UnhealthyThreshold="2",
            Interval="30",
            Timeout="5",
        ),
        Tags=Tags(Application=ref_stack_id,Name=Join("",[Ref('AWS::StackName'),"-",nm]))
    ))


    # Outputs
    t.add_output(Output(
        "BootServer",
        Description="Name/IP of Boot Server",
        Value=Join("/", [GetAtt(boot, "PublicDnsName"), GetAtt(boot, "PublicIp")])
    ))    



    t.add_output(Output(
        "MastersURL",
        Description="URL of the Masters",
        Value=Join("", ["http://", GetAtt(elasticLBMasters, "DNSName")])
    ))    

    t.add_output(Output(
        "PublicAgentsURL",
        Description="URL of the Public Agents haproxy stats.",
        Value=Join("", ["http://", GetAtt(elasticLBPublicAgents, "DNSName"), ":9090/haproxy?stats"])
    ))    


    # Write json to file
    jsonStr = t.to_json()

    fout = open(outfilename,"w")
    fout.write(jsonStr)
    fout.close()

    # Print the json to screen
    print(jsonStr)


if __name__ == "__main__":

    # create 1.3.1
    num_masters = 1
    num_agents = 3
    num_publicAgents = 1

    create_template(num_masters, num_agents, num_publicAgents)


    # create 1.5.1
    num_masters = 1
    num_agents = 5
    num_publicAgents = 1

    create_template(num_masters, num_agents, num_publicAgents)

    # create 3.1.3
    num_masters = 3
    num_agents = 1
    num_publicAgents = 3

    create_template(num_masters, num_agents, num_publicAgents)


    # create 3.3.3
    num_masters = 3
    num_agents = 3
    num_publicAgents = 3

    create_template(num_masters, num_agents, num_publicAgents)
    
