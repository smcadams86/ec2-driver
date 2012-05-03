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
import java.util.Properties;

public class AmazonProperties {

	private String wowza_ami;
	private String ec2_url;
	private String ec2_key;
	private String ec2_security_group;
	private String ec2_user_data_file;

	private String ec2_elastic_ip;
	
	public AmazonProperties(String propsFileLocation) { 
		Properties prop = new Properties();
		try {
			prop.load(new FileInputStream(propsFileLocation));
			
			wowza_ami = prop.getProperty("wowza_ami");
			ec2_url = prop.getProperty("ec2_url");
			ec2_key = prop.getProperty("ec2_key");
			ec2_security_group = prop.getProperty("ec2_security_group");
			ec2_user_data_file = prop.getProperty("ec2_user_data_file");
			ec2_elastic_ip = prop.getProperty("ec2_elastic_ip");
			
			EC2Logger.getInstance();
			EC2Logger.log("-- properties loaded --");
			EC2Logger.log("\twowza_ami : " + wowza_ami);
			EC2Logger.log("\tec2_url : " + ec2_url);
			EC2Logger.log("\tec2_key : " + ec2_key);
			EC2Logger.log("\tec2_security_group : " + ec2_security_group);
			EC2Logger.log("\tec2_user_data_file : " + ec2_user_data_file);
			EC2Logger.log("\tec2_elastic_ip : " + ec2_elastic_ip);
			
		} catch (IOException ex) {
			ex.printStackTrace();
			System.exit(0);
		}
		
	}

	/**
	 * @return the wowza_ami
	 */
	public String getWowza_ami() {
		return wowza_ami;
	}

	/**
	 * @param wowza_ami the wowza_ami to set
	 */
	public void setWowza_ami(String wowza_ami) {
		this.wowza_ami = wowza_ami;
	}

	/**
	 * @return the ec2_url
	 */
	public String getEc2_url() {
		return ec2_url;
	}

	/**
	 * @param ec2_url the ec2_url to set
	 */
	public void setEc2_url(String ec2_url) {
		this.ec2_url = ec2_url;
	}

	/**
	 * @return the ec2_key
	 */
	public String getEc2_key() {
		return ec2_key;
	}

	/**
	 * @param ec2_key the ec2_key to set
	 */
	public void setEc2_key(String ec2_key) {
		this.ec2_key = ec2_key;
	}

	/**
	 * @return the ec2_security_group
	 */
	public String getEc2_security_group() {
		return ec2_security_group;
	}

	/**
	 * @param ec2_security_group the ec2_security_group to set
	 */
	public void setEc2_security_group(String ec2_security_group) {
		this.ec2_security_group = ec2_security_group;
	}

	/**
	 * @return the ec2_user_data_file
	 */
	public String getEc2_user_data_file() {
		return ec2_user_data_file;
	}

	/**
	 * @param ec2_user_data_file the ec2_user_data_file to set
	 */
	public void setEc2_user_data_file(String ec2_user_data_file) {
		this.ec2_user_data_file = ec2_user_data_file;
	}

	
	/**
	 * @return the ec2_elastic_ip
	 */
	public String getEc2_elastic_ip() {
		return ec2_elastic_ip;
	}

	/**
	 * @param ec2_elastic_ip the ec2_elastic_ip to set
	 */
	public void setEc2_elastic_ip(String ec2_elastic_ip) {
		this.ec2_elastic_ip = ec2_elastic_ip;
	}
	
}
