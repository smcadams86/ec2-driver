package llc.rockford.webcast;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.OceanTheme;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

public class EC2Driver {

	JButton startButton;
	JLabel statusLabel = new JLabel("INITIALIZING");
	JButton stopButton;
	
	EC2StatusMonitor statusMonitor;
	EC2TaskExecutor taskExecutor;
	EC2Handle ec2Handle;
	AmazonProperties amazonProperties;
	
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
	}
	
	public Component createComponents() {
		startButton = new JButton("START");
		startButton.setEnabled(false);
		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				startButton.setEnabled(false);
				stopButton.setEnabled(false);
				taskExecutor.startInstance();
			}
		});
		
		statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
		statusLabel.setOpaque(true);
		statusLabel.setBackground(Color.YELLOW);
		
		stopButton = new JButton("STOP");
		stopButton.setEnabled(false);
		stopButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				startButton.setEnabled(false);
				stopButton.setEnabled(false);
				taskExecutor.terminateInstance();
			}
		});
		
		
		JPanel pane = new JPanel(new GridLayout(3, 1));
		pane.add(startButton);
		pane.add(statusLabel);
		pane.add(stopButton);
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

	/**
	 * @param args
	 */
	public static void main(final String[] args) {
//		new EC2Driver(args);
		
		// Schedule a job for the event dispatch thread:
		// creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new EC2Driver(args);
			}
		});
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
			    taskExecutor = new EC2TaskExecutor(ec2Handle, amazonProperties);
				statusMonitor = new EC2StatusMonitor(this, ec2Handle, taskExecutor);
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

}
