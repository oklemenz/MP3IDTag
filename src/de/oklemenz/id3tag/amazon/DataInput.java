package de.oklemenz.id3tag.amazon;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;


/**
 * This class will create all the GUI Components for a given query and store the input data
 * 
 */
public class DataInput extends Container {
	private Query query;
	private Hashtable commonParameters;
	private Hashtable queryParameters;
	
	private String[] commonParameterNames;
	private String[] queryParameterNames;

	private Query.Listener listener;
	private JTextField[] commonTextFields;
	private JTextField[] queryTextFields;
	private String mode;
	
	/**
	 * Mode specifying that GUI for both common and query specific parameters must be constructed. Button is also created
	 */
	public static String ALL = "ALL";    
	/**
	 * Mode specifying that GUI for only common parameters must be constructed
	 */
	public static String COMMON = "COMMON";
	/**
	 * Mode specifying that GUI for only query specific parameters must be constructed
	 */	
	public static String QUERY = "QUERY";

	/**
	 * Initializes member variables and creates the GUI
	 * @param commonParameters_ Hashtable containing common parameters. Input values are stored in this hashtable
	 * @param queryParameters_ Hashtable containing query parameters. Input values are stored in this hashtable
	 * @param commonParameterNames_ String array containing common parameter nmaes. These names are the keys in the commonParameters hashtable, thus can be extracted from that hashtable.
	 * 								However, they are taken seperately since this allows the invoker to specify the order that the parameters are displayed in.
	 * @param queryParameterNames_ String array containing query parameter nmaes. These names are the keys in the queryParameters hashtable, thus can be extracted from that hashtable.
	 * 								However, they are taken seperately since this allows the invoker to specify the order that the parameters are displayed in.
	 * @param listener_ The listener object for this dialog
	 * @param query_ The query that this GUI is for
	 * @param mode_ The mode specifying what GUI should be created. Must be either ALL, COMMON or QUERY
	 */
	public DataInput(Hashtable commonParameters_, Hashtable queryParameters_,
					 String[] commonParameterNames_, String[] queryParameterNames_, 
					  Query.Listener listener_,
					  Query query_,
					  String mode_) 
	{
		this.mode = mode_;
		this.query = query_;
		this.commonParameters = commonParameters_;
		this.queryParameters = queryParameters_;
		this.commonParameterNames = commonParameterNames_;
		this.queryParameterNames = queryParameterNames_;
		this.listener = listener_;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.createGUI();
	}

	/**
	 * Creates all the GUI
	 */
	private void createGUI() {
		if(this.mode.equals(ALL) || this.mode.equals(COMMON)) {
			this.commonTextFields = new JTextField[this.commonParameterNames.length];
		}
		if(this.mode.equals(ALL) || this.mode.equals(QUERY)) {
			this.queryTextFields = new JTextField[this.queryParameterNames.length];
		}
		JLabel label;
		Container container;
		JButton button;
		
		if(this.mode.equals(ALL) || this.mode.equals(COMMON)) {
			for(int i = 0; i < commonTextFields.length; i++) {
				commonTextFields[i] = new JTextField();
				container = new Container();
				container.setLayout(new BoxLayout(container, BoxLayout.X_AXIS));
				label = new JLabel(this.commonParameterNames[i]);
				label.setLabelFor(commonTextFields[i]);
				container.add(label);
				container.add(commonTextFields[i]);
				add(container);
			}
		
			//Put button in between common parameters and query specific parameters
			button = new JButton("Send");
			button.setActionCommand("Send");
			button.addActionListener(new ActionListener () {
				public void actionPerformed(ActionEvent e) {
					if(e.getActionCommand().equals("Send")) {
						DataInput.this.dialogFinished();
					}
				}
			});
			add(button);
		}
		
		if(this.mode.equals(ALL) || this.mode.equals(QUERY)) {
			for(int i = 0; i < queryTextFields.length; i++) {
				queryTextFields[i] = new JTextField();
				container = new Container();
				container.setLayout(new BoxLayout(container, BoxLayout.X_AXIS));
				label = new JLabel(this.queryParameterNames[i]);
				label.setLabelFor(queryTextFields[i]);
				container.add(label);
				container.add(queryTextFields[i]);
				add(container);
			}		
		}
	}
	
	/**
	 * Stores all the input values. Called when the Send button is clicked
	 * This is public since we may want to force it to get the values
	 */
	public void dialogFinished() {
		if(this.mode.equals(ALL) || this.mode.equals(COMMON)) {
			for(int i = 0; i < this.commonTextFields.length; i++) {
				this.commonParameters.put(this.commonParameterNames[i], this.commonTextFields[i].getText().trim());
			}
		}
		if(this.mode.equals(ALL) || this.mode.equals(QUERY)) {		
			for(int i = 0; i < this.queryTextFields.length; i++) {
				this.queryParameters.put(this.queryParameterNames[i], this.queryTextFields[i].getText().trim());
			}	
		}	
		if(this.listener != null) {
			this.listener.finished(this.query);
		}
	}
}
