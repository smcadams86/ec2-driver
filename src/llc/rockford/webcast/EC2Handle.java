package llc.rockford.webcast;

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
