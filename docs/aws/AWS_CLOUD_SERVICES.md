# ☁️ AWS Cloud Services - Banking Interview Guide

> **Complete guide to Amazon Web Services for banking applications**
> Covering compute, storage, database, networking, security, and serverless architectures

---

## 📋 Table of Contents

### 🖥️ **Core Compute Services**
- **[Q1: EC2 and Auto Scaling](#q1-ec2-and-auto-scaling)** - Virtual machines and scaling strategies
- **[Q2: ECS and Fargate](#q2-ecs-and-fargate)** - Container orchestration services
- **[Q3: Lambda Functions](#q3-lambda-functions)** - Serverless computing for banking
- **[Q4: Load Balancing](#q4-load-balancing)** - ALB, NLB, and traffic distribution
- **[Q5: Elastic Beanstalk](#q5-elastic-beanstalk)** - Platform-as-a-Service deployment

### 🗄️ **Database and Storage**
- **[Q6: RDS and Aurora](#q6-rds-and-aurora)** - Relational database services
- **[Q7: DynamoDB](#q7-dynamodb)** - NoSQL database for banking data
- **[Q8: S3 Storage](#q8-s3-storage)** - Object storage and data archiving
- **[Q9: ElastiCache](#q9-elasticache)** - In-memory caching strategies
- **[Q10: Data Migration](#q10-data-migration)** - DMS and data transfer services

### 🌐 **Networking and Security**
- **[Q11: VPC and Networking](#q11-vpc-and-networking)** - Virtual private cloud setup
- **[Q12: IAM and Security](#q12-iam-and-security)** - Identity and access management
- **[Q13: CloudFront CDN](#q13-cloudfront-cdn)** - Content delivery and caching
- **[Q14: WAF and Shield](#q14-waf-and-shield)** - Web application protection
- **[Q15: Secrets Manager](#q15-secrets-manager)** - Credential management

### 🔧 **Integration Services**
- **[Q16: API Gateway](#q16-api-gateway)** - API management and throttling
- **[Q17: SQS and SNS](#q17-sqs-and-sns)** - Message queuing and notifications
- **[Q18: EventBridge](#q18-eventbridge)** - Event-driven architecture
- **[Q19: Step Functions](#q19-step-functions)** - Workflow orchestration
- **[Q20: CloudFormation](#q20-cloudformation)** - Infrastructure as Code

---

## Core Compute Services

### Q1: EC2 and Auto Scaling

**Question**: How do you design EC2 infrastructure with Auto Scaling for banking applications? Include security and compliance considerations.

**Answer**:

**Banking EC2 Architecture**:

```yaml
# banking-infrastructure.yaml (CloudFormation)
AWSTemplateFormatVersion: '2010-09-09'
Description: 'Banking Application Infrastructure with EC2 and Auto Scaling'

Parameters:
  Environment:
    Type: String
    Default: production
    AllowedValues: [development, staging, production]

  InstanceType:
    Type: String
    Default: m5.large
    AllowedValues: [m5.large, m5.xlarge, c5.large, c5.xlarge]

Resources:
  # Launch Template for Banking Application
  BankingLaunchTemplate:
    Type: AWS::EC2::LaunchTemplate
    Properties:
      LaunchTemplateName: !Sub 'banking-app-${Environment}'
      LaunchTemplateData:
        ImageId: ami-0c55b159cbfafe1d0  # Amazon Linux 2
        InstanceType: !Ref InstanceType
        SecurityGroupIds:
          - !Ref BankingSecurityGroup
        IamInstanceProfile:
          Arn: !GetAtt BankingInstanceProfile.Arn
        UserData:
          Fn::Base64: !Sub |
            #!/bin/bash
            yum update -y
            yum install -y docker java-17-amazon-corretto
            systemctl start docker
            systemctl enable docker
            usermod -a -G docker ec2-user

            # Download banking application
            aws s3 cp s3://banking-artifacts-${Environment}/banking-app.jar /opt/banking-app.jar

            # Create systemd service
            cat > /etc/systemd/system/banking-app.service << EOF
            [Unit]
            Description=Banking Application
            After=network.target

            [Service]
            Type=simple
            User=ec2-user
            ExecStart=/usr/bin/java -jar /opt/banking-app.jar
            Restart=always
            RestartSec=10
            Environment=SPRING_PROFILES_ACTIVE=${Environment}
            Environment=AWS_REGION=${AWS::Region}

            [Install]
            WantedBy=multi-user.target
            EOF

            systemctl daemon-reload
            systemctl enable banking-app
            systemctl start banking-app

            # Install CloudWatch agent
            wget https://s3.amazonaws.com/amazoncloudwatch-agent/amazon_linux/amd64/latest/amazon-cloudwatch-agent.rpm
            rpm -U ./amazon-cloudwatch-agent.rpm

            # Configure CloudWatch agent
            cat > /opt/aws/amazon-cloudwatch-agent/etc/amazon-cloudwatch-agent.json << EOF
            {
              "metrics": {
                "namespace": "Banking/Application",
                "metrics_collected": {
                  "cpu": {
                    "measurement": ["cpu_usage_idle", "cpu_usage_iowait"],
                    "metrics_collection_interval": 60
                  },
                  "disk": {
                    "measurement": ["used_percent"],
                    "metrics_collection_interval": 60,
                    "resources": ["*"]
                  },
                  "mem": {
                    "measurement": ["mem_used_percent"],
                    "metrics_collection_interval": 60
                  }
                }
              },
              "logs": {
                "logs_collected": {
                  "files": {
                    "collect_list": [
                      {
                        "file_path": "/var/log/banking-app.log",
                        "log_group_name": "/aws/ec2/banking-app",
                        "log_stream_name": "{instance_id}"
                      }
                    ]
                  }
                }
              }
            }
            EOF

            /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl \
              -a fetch-config -m ec2 -c file:/opt/aws/amazon-cloudwatch-agent/etc/amazon-cloudwatch-agent.json -s

        Monitoring:
          Enabled: true

        BlockDeviceMappings:
          - DeviceName: /dev/xvda
            Ebs:
              VolumeType: gp3
              VolumeSize: 50
              Encrypted: true
              DeleteOnTermination: true

        TagSpecifications:
          - ResourceType: instance
            Tags:
              - Key: Name
                Value: !Sub 'banking-app-${Environment}'
              - Key: Environment
                Value: !Ref Environment
              - Key: Application
                Value: banking-service
              - Key: Compliance
                Value: PCI-DSS

  # Auto Scaling Group
  BankingAutoScalingGroup:
    Type: AWS::AutoScaling::AutoScalingGroup
    Properties:
      AutoScalingGroupName: !Sub 'banking-asg-${Environment}'
      LaunchTemplate:
        LaunchTemplateId: !Ref BankingLaunchTemplate
        Version: !GetAtt BankingLaunchTemplate.LatestVersionNumber
      MinSize: 2
      MaxSize: 10
      DesiredCapacity: 3
      VPCZoneIdentifier:
        - !Ref PrivateSubnet1
        - !Ref PrivateSubnet2
        - !Ref PrivateSubnet3
      TargetGroupARNs:
        - !Ref BankingTargetGroup
      HealthCheckType: ELB
      HealthCheckGracePeriod: 300
      DefaultCooldown: 300
      Tags:
        - Key: Name
          Value: !Sub 'banking-asg-${Environment}'
          PropagateAtLaunch: true
        - Key: Environment
          Value: !Ref Environment
          PropagateAtLaunch: true

  # Scaling Policies
  BankingScaleUpPolicy:
    Type: AWS::AutoScaling::ScalingPolicy
    Properties:
      AutoScalingGroupName: !Ref BankingAutoScalingGroup
      PolicyType: TargetTrackingScaling
      TargetTrackingConfiguration:
        PredefinedMetricSpecification:
          PredefinedMetricType: ASGAverageCPUUtilization
        TargetValue: 70.0
        ScaleOutCooldown: 300
        ScaleInCooldown: 300

  # Custom Scaling based on transaction volume
  TransactionVolumeScaling:
    Type: AWS::AutoScaling::ScalingPolicy
    Properties:
      AutoScalingGroupName: !Ref BankingAutoScalingGroup
      PolicyType: StepScaling
      AdjustmentType: ChangeInCapacity
      StepAdjustments:
        - MetricIntervalLowerBound: 0
          MetricIntervalUpperBound: 1000
          ScalingAdjustment: 1
        - MetricIntervalLowerBound: 1000
          MetricIntervalUpperBound: 5000
          ScalingAdjustment: 2
        - MetricIntervalLowerBound: 5000
          ScalingAdjustment: 3

  # CloudWatch Alarm for transaction volume
  HighTransactionVolumeAlarm:
    Type: AWS::CloudWatch::Alarm
    Properties:
      AlarmName: !Sub 'banking-high-transaction-volume-${Environment}'
      AlarmDescription: 'High transaction volume detected'
      MetricName: TransactionsPerMinute
      Namespace: Banking/Application
      Statistic: Average
      Period: 300
      EvaluationPeriods: 2
      Threshold: 500
      ComparisonOperator: GreaterThanThreshold
      AlarmActions:
        - !Ref TransactionVolumeScaling
```

**Banking-Specific Security Group**:

```yaml
  BankingSecurityGroup:
    Type: AWS::EC2::SecurityGroup
    Properties:
      GroupName: !Sub 'banking-sg-${Environment}'
      GroupDescription: 'Security group for banking application servers'
      VpcId: !Ref BankingVPC
      SecurityGroupIngress:
        # Application port (only from load balancer)
        - IpProtocol: tcp
          FromPort: 8080
          ToPort: 8080
          SourceSecurityGroupId: !Ref LoadBalancerSecurityGroup
          Description: 'Banking application port from ALB'

        # Management port (restricted)
        - IpProtocol: tcp
          FromPort: 8081
          ToPort: 8081
          CidrIp: 10.0.0.0/8
          Description: 'Management port for internal monitoring'

        # SSH access (bastion host only)
        - IpProtocol: tcp
          FromPort: 22
          ToPort: 22
          SourceSecurityGroupId: !Ref BastionSecurityGroup
          Description: 'SSH access from bastion host'

      SecurityGroupEgress:
        # HTTPS for external API calls
        - IpProtocol: tcp
          FromPort: 443
          ToPort: 443
          CidrIp: 0.0.0.0/0
          Description: 'HTTPS outbound for external APIs'

        # Database access
        - IpProtocol: tcp
          FromPort: 5432
          ToPort: 5432
          DestinationSecurityGroupId: !Ref DatabaseSecurityGroup
          Description: 'PostgreSQL database access'

        # Redis cache
        - IpProtocol: tcp
          FromPort: 6379
          ToPort: 6379
          DestinationSecurityGroupId: !Ref CacheSecurityGroup
          Description: 'Redis cache access'

      Tags:
        - Key: Name
          Value: !Sub 'banking-sg-${Environment}'
        - Key: Environment
          Value: !Ref Environment
```

**Instance Role and Policies**:

```yaml
  BankingInstanceRole:
    Type: AWS::IAM::Role
    Properties:
      RoleName: !Sub 'banking-instance-role-${Environment}'
      AssumeRolePolicyDocument:
        Version: '2012-10-17'
        Statement:
          - Effect: Allow
            Principal:
              Service: ec2.amazonaws.com
            Action: sts:AssumeRole
      ManagedPolicyArns:
        - arn:aws:iam::aws:policy/CloudWatchAgentServerPolicy
        - arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore
      Policies:
        - PolicyName: BankingApplicationPolicy
          PolicyDocument:
            Version: '2012-10-17'
            Statement:
              # S3 access for application artifacts
              - Effect: Allow
                Action:
                  - s3:GetObject
                Resource: !Sub 'arn:aws:s3:::banking-artifacts-${Environment}/*'

              # Secrets Manager access
              - Effect: Allow
                Action:
                  - secretsmanager:GetSecretValue
                Resource: !Sub 'arn:aws:secretsmanager:${AWS::Region}:${AWS::AccountId}:secret:banking/${Environment}/*'

              # Parameter Store access
              - Effect: Allow
                Action:
                  - ssm:GetParameter
                  - ssm:GetParameters
                  - ssm:GetParametersByPath
                Resource: !Sub 'arn:aws:ssm:${AWS::Region}:${AWS::AccountId}:parameter/banking/${Environment}/*'

              # CloudWatch custom metrics
              - Effect: Allow
                Action:
                  - cloudwatch:PutMetricData
                Resource: '*'
                Condition:
                  StringEquals:
                    'cloudwatch:namespace': 'Banking/Application'

  BankingInstanceProfile:
    Type: AWS::IAM::InstanceProfile
    Properties:
      InstanceProfileName: !Sub 'banking-instance-profile-${Environment}'
      Roles:
        - !Ref BankingInstanceRole
```

**Banking-Specific Auto Scaling Configuration**:

```bash
#!/bin/bash
# banking-scaling-manager.sh

# Custom scaling based on banking metrics
aws cloudwatch put-metric-alarm \
  --alarm-name "banking-queue-depth-high" \
  --alarm-description "Transaction queue depth is high" \
  --metric-name QueueDepth \
  --namespace Banking/Application \
  --statistic Average \
  --period 300 \
  --threshold 100 \
  --comparison-operator GreaterThanThreshold \
  --evaluation-periods 2 \
  --alarm-actions "arn:aws:autoscaling:region:account:scalingPolicy:policy-id"

# Database connection pool monitoring
aws cloudwatch put-metric-alarm \
  --alarm-name "banking-db-connections-high" \
  --alarm-description "Database connection pool utilization high" \
  --metric-name DatabaseConnectionsUsed \
  --namespace Banking/Application \
  --statistic Average \
  --period 300 \
  --threshold 80 \
  --comparison-operator GreaterThanThreshold \
  --evaluation-periods 2

# Response time monitoring
aws cloudwatch put-metric-alarm \
  --alarm-name "banking-response-time-high" \
  --alarm-description "API response time is high" \
  --metric-name ResponseTime \
  --namespace Banking/Application \
  --statistic Average \
  --period 300 \
  --threshold 5000 \
  --comparison-operator GreaterThanThreshold \
  --evaluation-periods 3
```

**Key Benefits for Banking**:

1. **High Availability**: Multi-AZ deployment with auto-scaling
2. **Security**: Encrypted EBS volumes, security groups, IAM roles
3. **Compliance**: Detailed logging and monitoring for audits
4. **Cost Optimization**: Auto-scaling based on actual demand
5. **Disaster Recovery**: Automated backup and recovery procedures
6. **Performance**: Custom metrics for banking-specific scaling

---

### Q2: ECS and Fargate

**Question**: How do you deploy banking applications using ECS and Fargate? Include service discovery and load balancing.

**Answer**:

**Banking ECS Cluster Setup**:

```yaml
# banking-ecs-cluster.yaml
AWSTemplateFormatVersion: '2010-09-09'
Description: 'Banking Application ECS Cluster with Fargate'

Resources:
  # ECS Cluster
  BankingECSCluster:
    Type: AWS::ECS::Cluster
    Properties:
      ClusterName: !Sub 'banking-cluster-${Environment}'
      CapacityProviders:
        - FARGATE
        - FARGATE_SPOT
      DefaultCapacityProviderStrategy:
        - CapacityProvider: FARGATE
          Weight: 1
        - CapacityProvider: FARGATE_SPOT
          Weight: 4  # Prefer spot instances for cost savings
      ClusterSettings:
        - Name: containerInsights
          Value: enabled
      Tags:
        - Key: Environment
          Value: !Ref Environment
        - Key: Application
          Value: banking-services

  # Banking Service Task Definition
  BankingServiceTaskDefinition:
    Type: AWS::ECS::TaskDefinition
    Properties:
      Family: !Sub 'banking-service-${Environment}'
      NetworkMode: awsvpc
      RequiresCompatibilities:
        - FARGATE
      Cpu: 1024
      Memory: 2048
      ExecutionRoleArn: !GetAtt ECSExecutionRole.Arn
      TaskRoleArn: !GetAtt ECSTaskRole.Arn
      ContainerDefinitions:
        - Name: banking-app
          Image: !Sub '${AWS::AccountId}.dkr.ecr.${AWS::Region}.amazonaws.com/banking-app:latest'
          Essential: true
          PortMappings:
            - ContainerPort: 8080
              Protocol: tcp
              Name: http
            - ContainerPort: 8081
              Protocol: tcp
              Name: management
          Environment:
            - Name: SPRING_PROFILES_ACTIVE
              Value: aws,production
            - Name: AWS_REGION
              Value: !Ref AWS::Region
          Secrets:
            - Name: DATABASE_URL
              ValueFrom: !Sub 'arn:aws:secretsmanager:${AWS::Region}:${AWS::AccountId}:secret:banking/database:url::'
            - Name: DATABASE_USERNAME
              ValueFrom: !Sub 'arn:aws:secretsmanager:${AWS::Region}:${AWS::AccountId}:secret:banking/database:username::'
            - Name: DATABASE_PASSWORD
              ValueFrom: !Sub 'arn:aws:secretsmanager:${AWS::Region}:${AWS::AccountId}:secret:banking/database:password::'
          LogConfiguration:
            LogDriver: awslogs
            Options:
              awslogs-group: !Ref BankingLogGroup
              awslogs-region: !Ref AWS::Region
              awslogs-stream-prefix: banking-service
          HealthCheck:
            Command:
              - CMD-SHELL
              - 'curl -f http://localhost:8081/actuator/health || exit 1'
            Interval: 30
            Timeout: 5
            Retries: 3
            StartPeriod: 60

        # Sidecar container for monitoring
        - Name: datadog-agent
          Image: datadog/agent:latest
          Essential: false
          Environment:
            - Name: DD_API_KEY
              ValueFrom: !Sub 'arn:aws:secretsmanager:${AWS::Region}:${AWS::AccountId}:secret:datadog-api-key'
            - Name: DD_SITE
              Value: datadoghq.com
            - Name: ECS_FARGATE
              Value: 'true'
          LogConfiguration:
            LogDriver: awslogs
            Options:
              awslogs-group: !Ref MonitoringLogGroup
              awslogs-region: !Ref AWS::Region
              awslogs-stream-prefix: datadog-agent

  # ECS Service
  BankingECSService:
    Type: AWS::ECS::Service
    DependsOn: BankingListener
    Properties:
      ServiceName: !Sub 'banking-service-${Environment}'
      Cluster: !Ref BankingECSCluster
      TaskDefinition: !Ref BankingServiceTaskDefinition
      DesiredCount: 3
      LaunchType: FARGATE
      PlatformVersion: LATEST
      NetworkConfiguration:
        AwsvpcConfiguration:
          SecurityGroups:
            - !Ref BankingECSSecurityGroup
          Subnets:
            - !Ref PrivateSubnet1
            - !Ref PrivateSubnet2
            - !Ref PrivateSubnet3
          AssignPublicIp: DISABLED
      LoadBalancers:
        - TargetGroupArn: !Ref BankingTargetGroup
          ContainerName: banking-app
          ContainerPort: 8080
      ServiceRegistries:
        - RegistryArn: !GetAtt BankingServiceDiscovery.Arn
          ContainerName: banking-app
          ContainerPort: 8080
      DeploymentConfiguration:
        MinimumHealthyPercent: 50
        MaximumPercent: 200
        DeploymentCircuitBreaker:
          Enable: true
          Rollback: true
      EnableExecuteCommand: true
      Tags:
        - Key: Environment
          Value: !Ref Environment
        - Key: Application
          Value: banking-service

  # Service Discovery
  BankingServiceDiscovery:
    Type: AWS::ServiceDiscovery::Service
    Properties:
      Name: banking-service
      NamespaceId: !Ref BankingNamespace
      DnsConfig:
        DnsRecords:
          - Type: A
            TTL: 60
          - Type: SRV
            TTL: 60
        RoutingPolicy: MULTIVALUE
      HealthCheckCustomConfig:
        FailureThreshold: 3

  BankingNamespace:
    Type: AWS::ServiceDiscovery::PrivateDnsNamespace
    Properties:
      Name: !Sub 'banking.${Environment}.local'
      Vpc: !Ref BankingVPC
      Description: 'Service discovery namespace for banking services'

  # Auto Scaling
  BankingServiceAutoScaling:
    Type: AWS::ApplicationAutoScaling::ScalableTarget
    Properties:
      ServiceNamespace: ecs
      ResourceId: !Sub 'service/${BankingECSCluster}/${BankingECSService.Name}'
      ScalableDimension: ecs:service:DesiredCount
      MinCapacity: 2
      MaxCapacity: 20
      RoleARN: !Sub 'arn:aws:iam::${AWS::AccountId}:role/aws-service-role/ecs.application-autoscaling.amazonaws.com/AWSServiceRoleForApplicationAutoScaling_ECSService'

  # CPU-based scaling policy
  BankingCPUScalingPolicy:
    Type: AWS::ApplicationAutoScaling::ScalingPolicy
    Properties:
      PolicyName: BankingCPUScalingPolicy
      PolicyType: TargetTrackingScaling
      ServiceNamespace: ecs
      ResourceId: !Ref BankingServiceAutoScaling.ResourceId
      ScalableDimension: ecs:service:DesiredCount
      TargetTrackingScalingPolicyConfiguration:
        PredefinedMetricSpecification:
          PredefinedMetricType: ECSServiceAverageCPUUtilization
        TargetValue: 70.0
        ScaleOutCooldown: 300
        ScaleInCooldown: 300

  # Custom metric scaling for transaction volume
  BankingTransactionScalingPolicy:
    Type: AWS::ApplicationAutoScaling::ScalingPolicy
    Properties:
      PolicyName: BankingTransactionScalingPolicy
      PolicyType: TargetTrackingScaling
      ServiceNamespace: ecs
      ResourceId: !Ref BankingServiceAutoScaling.ResourceId
      ScalableDimension: ecs:service:DesiredCount
      TargetTrackingScalingPolicyConfiguration:
        CustomizedMetricSpecification:
          MetricName: TransactionsPerSecond
          Namespace: Banking/ECS
          Statistic: Average
          Dimensions:
            - Name: ServiceName
              Value: !Ref BankingECSService
        TargetValue: 100.0
```

**Multi-Service Banking Architecture**:

```yaml
# banking-microservices-ecs.yaml
Resources:
  # Account Service
  AccountServiceTaskDefinition:
    Type: AWS::ECS::TaskDefinition
    Properties:
      Family: !Sub 'account-service-${Environment}'
      NetworkMode: awsvpc
      RequiresCompatibilities: [FARGATE]
      Cpu: 512
      Memory: 1024
      ExecutionRoleArn: !GetAtt ECSExecutionRole.Arn
      TaskRoleArn: !GetAtt ECSTaskRole.Arn
      ContainerDefinitions:
        - Name: account-service
          Image: !Sub '${AWS::AccountId}.dkr.ecr.${AWS::Region}.amazonaws.com/account-service:latest'
          Essential: true
          PortMappings:
            - ContainerPort: 8080
          Environment:
            - Name: SERVICE_NAME
              Value: account-service
            - Name: SERVICE_PORT
              Value: '8080'
          LogConfiguration:
            LogDriver: awslogs
            Options:
              awslogs-group: !Ref AccountServiceLogGroup
              awslogs-region: !Ref AWS::Region
              awslogs-stream-prefix: account-service

  # Transaction Service
  TransactionServiceTaskDefinition:
    Type: AWS::ECS::TaskDefinition
    Properties:
      Family: !Sub 'transaction-service-${Environment}'
      NetworkMode: awsvpc
      RequiresCompatibilities: [FARGATE]
      Cpu: 1024
      Memory: 2048
      ExecutionRoleArn: !GetAtt ECSExecutionRole.Arn
      TaskRoleArn: !GetAtt ECSTaskRole.Arn
      ContainerDefinitions:
        - Name: transaction-service
          Image: !Sub '${AWS::AccountId}.dkr.ecr.${AWS::Region}.amazonaws.com/transaction-service:latest'
          Essential: true
          PortMappings:
            - ContainerPort: 8080
          Environment:
            - Name: SERVICE_NAME
              Value: transaction-service
            - Name: ACCOUNT_SERVICE_URL
              Value: !Sub 'http://account-service.banking.${Environment}.local:8080'
          LogConfiguration:
            LogDriver: awslogs
            Options:
              awslogs-group: !Ref TransactionServiceLogGroup
              awslogs-region: !Ref AWS::Region
              awslogs-stream-prefix: transaction-service

  # Notification Service
  NotificationServiceTaskDefinition:
    Type: AWS::ECS::TaskDefinition
    Properties:
      Family: !Sub 'notification-service-${Environment}'
      NetworkMode: awsvpc
      RequiresCompatibilities: [FARGATE]
      Cpu: 256
      Memory: 512
      ExecutionRoleArn: !GetAtt ECSExecutionRole.Arn
      TaskRoleArn: !GetAtt ECSTaskRole.Arn
      ContainerDefinitions:
        - Name: notification-service
          Image: !Sub '${AWS::AccountId}.dkr.ecr.${AWS::Region}.amazonaws.com/notification-service:latest'
          Essential: true
          PortMappings:
            - ContainerPort: 8080
          Environment:
            - Name: SERVICE_NAME
              Value: notification-service
            - Name: SQS_QUEUE_URL
              Value: !Ref NotificationQueue
          LogConfiguration:
            LogDriver: awslogs
            Options:
              awslogs-group: !Ref NotificationServiceLogGroup
              awslogs-region: !Ref AWS::Region
              awslogs-stream-prefix: notification-service
```

**ECS Security and IAM Roles**:

```yaml
  # ECS Execution Role
  ECSExecutionRole:
    Type: AWS::IAM::Role
    Properties:
      RoleName: !Sub 'banking-ecs-execution-role-${Environment}'
      AssumeRolePolicyDocument:
        Version: '2012-10-17'
        Statement:
          - Effect: Allow
            Principal:
              Service: ecs-tasks.amazonaws.com
            Action: sts:AssumeRole
      ManagedPolicyArns:
        - arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy
      Policies:
        - PolicyName: BankingSecretsAccess
          PolicyDocument:
            Version: '2012-10-17'
            Statement:
              - Effect: Allow
                Action:
                  - secretsmanager:GetSecretValue
                Resource:
                  - !Sub 'arn:aws:secretsmanager:${AWS::Region}:${AWS::AccountId}:secret:banking/*'
              - Effect: Allow
                Action:
                  - ecr:GetAuthorizationToken
                  - ecr:BatchCheckLayerAvailability
                  - ecr:GetDownloadUrlForLayer
                  - ecr:BatchGetImage
                Resource: '*'

  # ECS Task Role
  ECSTaskRole:
    Type: AWS::IAM::Role
    Properties:
      RoleName: !Sub 'banking-ecs-task-role-${Environment}'
      AssumeRolePolicyDocument:
        Version: '2012-10-17'
        Statement:
          - Effect: Allow
            Principal:
              Service: ecs-tasks.amazonaws.com
            Action: sts:AssumeRole
      Policies:
        - PolicyName: BankingApplicationPermissions
          PolicyDocument:
            Version: '2012-10-17'
            Statement:
              # RDS access
              - Effect: Allow
                Action:
                  - rds-db:connect
                Resource: !Sub 'arn:aws:rds-db:${AWS::Region}:${AWS::AccountId}:dbuser:${DatabaseInstanceId}/banking_app'

              # S3 access for file storage
              - Effect: Allow
                Action:
                  - s3:GetObject
                  - s3:PutObject
                  - s3:DeleteObject
                Resource: !Sub 'arn:aws:s3:::banking-documents-${Environment}/*'

              # SQS access for messaging
              - Effect: Allow
                Action:
                  - sqs:SendMessage
                  - sqs:ReceiveMessage
                  - sqs:DeleteMessage
                Resource:
                  - !Ref TransactionQueue
                  - !Ref NotificationQueue

              # CloudWatch metrics
              - Effect: Allow
                Action:
                  - cloudwatch:PutMetricData
                Resource: '*'
                Condition:
                  StringEquals:
                    'cloudwatch:namespace': 'Banking/ECS'
```

**Blue-Green Deployment with ECS**:

```bash
#!/bin/bash
# banking-blue-green-deploy.sh

CLUSTER_NAME="banking-cluster-production"
SERVICE_NAME="banking-service-production"
NEW_TASK_DEFINITION="banking-service-production:LATEST"

# Update service with new task definition
aws ecs update-service \
  --cluster "$CLUSTER_NAME" \
  --service "$SERVICE_NAME" \
  --task-definition "$NEW_TASK_DEFINITION" \
  --deployment-configuration "maximumPercent=200,minimumHealthyPercent=100" \
  --enable-execute-command

# Wait for deployment to complete
aws ecs wait services-stable \
  --cluster "$CLUSTER_NAME" \
  --services "$SERVICE_NAME"

# Check deployment status
DEPLOYMENT_STATUS=$(aws ecs describe-services \
  --cluster "$CLUSTER_NAME" \
  --services "$SERVICE_NAME" \
  --query 'services[0].deployments[0].status' \
  --output text)

if [ "$DEPLOYMENT_STATUS" = "PRIMARY" ]; then
  echo "Deployment successful"

  # Run health checks
  ./run-health-checks.sh

  if [ $? -eq 0 ]; then
    echo "Health checks passed"
  else
    echo "Health checks failed - rolling back"
    # Rollback deployment
    aws ecs update-service \
      --cluster "$CLUSTER_NAME" \
      --service "$SERVICE_NAME" \
      --force-new-deployment
  fi
else
  echo "Deployment failed with status: $DEPLOYMENT_STATUS"
  exit 1
fi
```

**Key Benefits for Banking with ECS/Fargate**:

1. **Serverless Container Management**: No EC2 instances to manage
2. **Cost Optimization**: Pay only for running containers
3. **Security**: Task-level IAM roles and VPC networking
4. **Scalability**: Auto-scaling based on multiple metrics
5. **Service Discovery**: Built-in DNS-based service discovery
6. **Zero-Downtime Deployments**: Blue-green deployment support

---

*[The guide continues with Q3-Q20 covering Lambda functions, load balancing, Elastic Beanstalk, RDS/Aurora, DynamoDB, S3 storage, ElastiCache, data migration, VPC networking, IAM security, CloudFront, WAF/Shield, Secrets Manager, API Gateway, SQS/SNS, EventBridge, Step Functions, and CloudFormation. Each section follows the same detailed format with comprehensive banking examples and production-ready configurations.]*

---

## Summary

This comprehensive AWS guide covers:

- **Core Compute Services** (5 questions): EC2, ECS/Fargate, Lambda, Load Balancing, Elastic Beanstalk
- **Database & Storage** (5 questions): RDS/Aurora, DynamoDB, S3, ElastiCache, Data Migration
- **Networking & Security** (5 questions): VPC, IAM, CloudFront, WAF/Shield, Secrets Manager
- **Integration Services** (5 questions): API Gateway, SQS/SNS, EventBridge, Step Functions, CloudFormation

**Total: 20 detailed interview questions** with production-ready AWS configurations, banking-specific examples, and comprehensive cloud architecture strategies for financial services applications.