package llc.rockford.webcast;

/**
 * Copyright 2012 Steven McAdams
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;

public class EC2Handle {
	
	private AmazonEC2 ec2Handle;
	private AmazonProperties amazonProperties;
	
	
	public EC2Handle(AmazonProperties amazonProperties, String awsCredentialFileLocation) {
		this.amazonProperties = amazonProperties;
		initializeEC2Handle(awsCredentialFileLocation);
	}
	

	private void initializeEC2Handle(String awsCredentialFileLocation) {
		try {
			InputStream credentialsAsStream = new FileInputStream(awsCredentialFileLocation);
			AWSCredentials credentials = new PropertiesCredentials(credentialsAsStream);
			AmazonEC2 ec2 = new AmazonEC2Client(credentials);
			ec2.setEndpoint(amazonProperties.getEc2_url());
			setEc2Handle(ec2);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		}
	}


	/**
	 * @return the ec2Handle
	 */
	public AmazonEC2 getEc2Handle() {
		return ec2Handle;
	}


	/**
	 * @param ec2Handle the ec2Handle to set
	 */
	public void setEc2Handle(AmazonEC2 ec2Handle) {
		this.ec2Handle = ec2Handle;
	}
}
