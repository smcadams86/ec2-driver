ec2-driver
==========

Start and Stop amazon instance via simple GUI

Usage
==========
``` java
EC2Driver [-a <arg>] [-p <arg>]
 -a,--aws-credentials <arg>   contains the Amazon Web Services (AWS)
                              credentials of a user
 -p,--properties <arg>        webcasting config file
```

Example aws-credentials file
==========
``` java
accessKey=amazon-access-key
secretKey=amazon-secret-key
```

Example properties file
==========
``` java
// --------------- WEBCASTING CONFIG FILE ------------------//
// This file contains configuration necessary to 
// utilize amazons EC2 platform services
// ---------------------------------------------------------//
// Author : Steve McAdams
// Email  : smcadams86@gmail.com
// ---------------------------------------------------------//
wowza_ami = ami-9aba68f3
ec2_url = https://ec2.us-east-1.amazonaws.com
ec2_key = your-ec2-key
ec2_security_group = your-security-group
ec2_user_data_file = user-data-file.zip
ec2_elastic_ip = elastic-ip-for-instance
```
