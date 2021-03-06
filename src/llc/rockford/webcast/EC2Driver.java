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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.OceanTheme;

import llc.rockford.webcast.worker.CheckAmazonStatusWorker;
import llc.rockford.webcast.worker.InitializeWorker;
import llc.rockford.webcast.worker.StartInstanceWorker;
import llc.rockford.webcast.worker.StreamBroadcaster;
import llc.rockford.webcast.worker.TerminateInstanceWorker;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.exec.ExecuteException;

public class EC2Driver implements ActionListener {

	JButton startStreamButton;
	JButton stopStreamButton;
	JButton startButton;
	JLabel statusLabel = new JLabel("INITIALIZING");
	JLabel broadcastStatusLabel = new JLabel("NOT BROADCASTING");
	JButton stopButton;
	
	ApplicationState applicationState;
	EC2Handle ec2Handle;
	AmazonProperties amazonProperties;
	StreamBroadcaster broadcaster;
	
	// Specify the look and feel to use by defining the LOOKANDFEEL constant
	// Valid values are: null (use the default), "Metal", "System", "Motif",
	// and "GTK"
	final static String LOOKANDFEEL = "System";

	// If you choose the Metal L&F, you can also choose a theme.
	// Specify the theme to use by defining the THEME constant
	// Valid values are: "DefaultMetal", "Ocean", and "Test"
	final static String THEME = "Test";
	
	public EC2Driver(String[] args) { 
		parseCommandLine(args);
		createAndShowGUI();
		applicationState = new ApplicationState(this);

		new InitializeWorker(ec2Handle.getEc2Handle(), applicationState).execute();
		
		Timer timer = new Timer(5000, this);
		timer.setInitialDelay(3000);
		timer.start(); 
		
		broadcaster = new StreamBroadcaster(amazonProperties);

		// add shut down hooks to terminate amazon EC2 instance
		// to prevent over billing
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				new TerminateInstanceWorker(ec2Handle.getEc2Handle(), applicationState, amazonProperties).execute();
			}
		});
    
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		new CheckAmazonStatusWorker(ec2Handle.getEc2Handle(), applicationState, amazonProperties).execute();
	}
	
	
	public Component createComponents() {
		startButton = new JButton("START SERVER");
		startButton.setEnabled(false);
		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				startButton.setEnabled(false);
				stopButton.setEnabled(false);
				new StartInstanceWorker(ec2Handle.getEc2Handle(), applicationState, amazonProperties).execute();
			}
		});
		
		startStreamButton = new JButton("START BROADCAST");
		startStreamButton.setEnabled(false);
		startStreamButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				startStreamButton.setEnabled(false);
				stopStreamButton.setEnabled(true);
				try {
					broadcaster.start();
				} catch (ExecuteException e1) {
					startStreamButton.setEnabled(true);
					e1.printStackTrace();
				} catch (IOException e1) {
					startStreamButton.setEnabled(true);
					e1.printStackTrace();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		});
		
		stopStreamButton = new JButton("STOP BROADCAST");
		stopStreamButton.setEnabled(false);
		stopStreamButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				broadcaster.stop();
			}
		});
		
		
		statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
		statusLabel.setOpaque(true);
		statusLabel.setBackground(Color.YELLOW);
		
		broadcastStatusLabel.setHorizontalAlignment(SwingConstants.CENTER);
		broadcastStatusLabel.setOpaque(true);
		broadcastStatusLabel.setBackground(Color.RED);
		
		stopButton = new JButton("STOP SERVER");
		stopButton.setEnabled(false);
		stopButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				startButton.setEnabled(false);
				stopButton.setEnabled(false);
				new TerminateInstanceWorker(ec2Handle.getEc2Handle(), applicationState, amazonProperties).execute();
			}
		});
		
		
		JPanel pane = new JPanel(new GridLayout(7,1));
		pane.add(startButton);
		pane.add(statusLabel);
		pane.add(stopButton);
		pane.add(new JSeparator(SwingConstants.HORIZONTAL));
		pane.add(startStreamButton);
		pane.add(broadcastStatusLabel);
		pane.add(stopStreamButton);
		
		pane.setBorder(BorderFactory.createEmptyBorder(30, // top
				30, // left
				10, // bottom
				30) // right
		);

		return pane;
	}

	private void initLookAndFeel() {
		String lookAndFeel = null;

		if (LOOKANDFEEL != null) {
			if (LOOKANDFEEL.equals("Metal")) {
				lookAndFeel = UIManager.getCrossPlatformLookAndFeelClassName();
			}
			else if (LOOKANDFEEL.equals("System")) {
				lookAndFeel = UIManager.getSystemLookAndFeelClassName();
			}
			else if (LOOKANDFEEL.equals("Motif")) {
				lookAndFeel = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
			}
			else if (LOOKANDFEEL.equals("GTK")) {
				lookAndFeel = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
			}
			else {
				System.err.println("Unexpected value of LOOKANDFEEL specified: " + LOOKANDFEEL);
				lookAndFeel = UIManager.getCrossPlatformLookAndFeelClassName();
			}
			try {
				UIManager.setLookAndFeel(lookAndFeel);
				// If L&F = "Metal", set the theme
				if (LOOKANDFEEL.equals("Metal")) {
					if (THEME.equals("DefaultMetal"))
						MetalLookAndFeel.setCurrentTheme(new DefaultMetalTheme());
					else if (THEME.equals("Ocean"))
						MetalLookAndFeel.setCurrentTheme(new OceanTheme());
					UIManager.setLookAndFeel(new MetalLookAndFeel());
				}
			}
			catch (ClassNotFoundException e) {
				System.err.println("Couldn't find class for specified look and feel:" + lookAndFeel);
				System.err.println("Did you include the L&F library in the class path?");
				System.err.println("Using the default look and feel.");
			}
			catch (UnsupportedLookAndFeelException e) {
				System.err.println("Can't use the specified look and feel (" + lookAndFeel + ") on this platform.");
				System.err.println("Using the default look and feel.");
			}
			catch (Exception e) {
				System.err.println("Couldn't get specified look and feel ("
						+ lookAndFeel + "), for some reason.");
				System.err.println("Using the default look and feel.");
				e.printStackTrace();
			}
		}
	}

	private void createAndShowGUI() {
		// Set the look and feel.
		initLookAndFeel();

		// Make sure we have nice window decorations.
		JFrame.setDefaultLookAndFeelDecorated(true);

		// Create and set up the window.
		JFrame frame = new JFrame("Webcast Utility");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setPreferredSize(new Dimension(300,400));
		java.net.URL url = ClassLoader.getSystemResource("llc/rockford/webcast/resources/internet.png");
		Toolkit kit = Toolkit.getDefaultToolkit();
		Image img = kit.createImage(url);
		frame.setIconImage(img);
		Component contents = createComponents();
		frame.getContentPane().add(contents, BorderLayout.CENTER);

		// Display the window.
		frame.pack();
		frame.setVisible(true);

	}

	protected void parseCommandLine(String[] args) {
		
		// create the command line parser
		CommandLineParser parser = new PosixParser();

		// create the Options
		Options options = new Options();
		options.addOption( "a", "aws-credentials", true, "contains the Amazon Web Services (AWS) credentials of a user" );
		options.addOption( "p", "properties", true, "webcasting config file" );
		
		try {
		    // parse the command line arguments
		    CommandLine line = parser.parse( options, args );
		    
		    if( line.hasOption( "aws-credentials" ) &&
		    	line.hasOption( "properties" )) {
		    	
		    	EC2Logger.getInstance();
		    	EC2Logger.log("aws-credentials : " + line.getOptionValue( "aws-credentials" ) );
			    EC2Logger.log("properties : " + line.getOptionValue( "properties" ) );
			    
			    amazonProperties = new AmazonProperties(line.getOptionValue( "properties" ));
			    ec2Handle = new EC2Handle(amazonProperties, line.getOptionValue( "aws-credentials" ));
		    }
		    else {
		    	// automatically generate the help statement
		    	HelpFormatter formatter = new HelpFormatter();
		    	formatter.printHelp( "EC2Driver", options, true );
		    	System.exit(0);
		    }

		}
		catch( ParseException exp ) {
		    System.out.println( "Unexpected exception:" + exp.getMessage() );
		}
		
	}
	
	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		// Schedule a job for the event dispatch thread:
		// creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new EC2Driver(args);
			}
		});
	}

}
