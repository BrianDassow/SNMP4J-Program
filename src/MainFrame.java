import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.snmp4j.smi.SmiParseException;
import javax.swing.JLabel;
import java.awt.Color;
import javax.swing.JMenuBar;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JMenu;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JTextField;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.JScrollPane;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.border.EtchedBorder;
import java.awt.Component;
import java.awt.Dimension;

public class MainFrame extends JFrame {
	private JPanel contentPane;
	private static JTextField OIDTextField;
	private static JTable resultTable;
	private static JTextField ipTextField;
	private static JButton trapReceiver;
	private static JComboBox comboBox;
	private static JTextField setTextField;
	private static JLabel lblSetValue;
	private static JButton goButton;
	private static JScrollPane scrollPane;
	private static JPanel panel_1;
	private static SnmpHelper client;
	private static Timer t;
	private static boolean trapOn = false;

	//create MainFrame
	public MainFrame() {
		//Setting up the frame
		setTitle("SNMP Client");
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 788, 545);
		
		//creating items...
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu mnNewMenu = new JMenu("File");
		menuBar.add(mnNewMenu);
		
		//listener for the load mib jmenuitem
		JMenuItem loadMIBFileButton = new JMenuItem("Load MIB");
		loadMIBFileButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				new File(System.getProperty("user.dir") + "\\mibs").mkdir();
				JFileChooser chooser = new JFileChooser(System.getProperty("user.dir") + "\\mibs");
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				int returnVal = chooser.showOpenDialog(getParent());
				if(returnVal == JFileChooser.APPROVE_OPTION) {
					System.out.println("Did the client load mib? " + client.loadMIB(chooser.getSelectedFile().getName()));
				}
			}
		});
		mnNewMenu.add(loadMIBFileButton);
		
		//hidden unload (can make work but useless)
		JMenuItem unloadMIBFileButton = new JMenuItem("Unload MIB");
		unloadMIBFileButton.setEnabled(false);
		unloadMIBFileButton.setVisible(false);
		mnNewMenu.add(unloadMIBFileButton);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		//setup panel
		JPanel panel = new JPanel();
		panel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panel.setBounds(5, 11, 767, 43);
		contentPane.add(panel);
		panel.setLayout(null);
		
		//oid label
		JLabel lblOid = new JLabel("OID");
		lblOid.setBounds(112, 10, 37, 17);
		panel.add(lblOid);
		
		//text field for setting value
		setTextField = new JTextField();
		setTextField.setVisible(false);
		setTextField.setBounds(473, 8, 106, 20);
		panel.add(setTextField);
		setTextField.setColumns(10);
		
		lblSetValue = new JLabel("Set Value");
		lblSetValue.setVisible(false);
		lblSetValue.setBounds(423, 11, 75, 14);
		panel.add(lblSetValue);
		
		//oid text field and default value... (shows sys contact)
		OIDTextField = new JTextField();
		OIDTextField.setText(".1.3.6.1.2.1.1.4.0");
		OIDTextField.setBounds(135, 8, 120, 20);
		panel.add(OIDTextField);
		OIDTextField.setColumns(10);
		
		//go button... with action listener
		goButton = new JButton("Go");
		goButton.setFocusable(false);
		goButton.setBounds(326, 8, 70, 20);
		goButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					goPress(OIDTextField.getText());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		panel.add(goButton);
		
		//combo box for get set and mult
		comboBox = new JComboBox();
		comboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				comboBoxChanged();
			}
		});
		comboBox.setFocusable(false);
		comboBox.setModel(new DefaultComboBoxModel(new String[] {"Get", "Set", "Mult"}));
		comboBox.setBounds(259, 8, 58, 20);
		panel.add(comboBox);
		
		//ip text field with default value
		ipTextField = new JTextField();
		ipTextField.setText("127.0.0.1/161");
		ipTextField.setBounds(25, 8, 77, 20);
		panel.add(ipTextField);
		ipTextField.setColumns(10);
		
		//ip label
		JLabel lblIp = new JLabel("IP");
		lblIp.setBounds(10, 11, 24, 14);
		panel.add(lblIp);
		
		//trap receiver button and action listener
		trapReceiver = new JButton("Start trap receiver");
		trapReceiver.setFocusable(false);
		trapReceiver.setBackground(Color.GREEN);
		trapReceiver.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					trapPress();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		trapReceiver.setBounds(607, 7, 150, 23);
		panel.add(trapReceiver);
		
		//another panel
		panel_1= new JPanel();
		panel_1.setPreferredSize(new Dimension(0, 0));
		panel_1.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panel_1.setBounds(5, 65, 767, 409);
		contentPane.add(panel_1);
		panel_1.setLayout(null);
		
		JLabel outputLabel = new JLabel("Result Table");
		outputLabel.setBounds(10, 11, 80, 21);
		panel_1.add(outputLabel);
		
		//scroll pane (to scroll)
		scrollPane = new JScrollPane();
		scrollPane.setAlignmentY(Component.BOTTOM_ALIGNMENT);
		scrollPane.setBounds(10, 35, 747, 363);
		panel_1.add(scrollPane);
		
		//table to show everything
		resultTable = new JTable();
		resultTable.setAutoscrolls(false);
		scrollPane.setViewportView(resultTable);
		resultTable.setModel(new DefaultTableModel(
			new Object[][] {
			},
			new String[] {
				"OID", "Value", "Type", "IP:Port"
			}
		) {
			boolean[] columnEditables = new boolean[] {
				false, false, false, false
			};
			public boolean isCellEditable(int row, int column) {
				return columnEditables[column];
			}
		});
		
		resultTable.getColumnModel().getColumn(0).setPreferredWidth(100);
		resultTable.getColumnModel().getColumn(1).setPreferredWidth(140);
		resultTable.getColumnModel().getColumn(2).setPreferredWidth(70);
		resultTable.getColumnModel().getColumn(3).setPreferredWidth(25);
	}
	
	//function that handles when you change the combo box
	private static void comboBoxChanged() {
		if (comboBox.getSelectedItem() == "Get") {
			setTextField.setVisible(false);
			lblSetValue.setVisible(false);
		}
		else if (comboBox.getSelectedItem() == "Set") {
			setTextField.setText("");
			setTextField.setVisible(true);
			lblSetValue.setVisible(true);
		}
		else if (comboBox.getSelectedItem() == "Mult") {
			setTextField.setText("1");
			setTextField.setVisible(true);
			lblSetValue.setVisible(true);
		}
	}
	
	//function that handles the go button being pressed
	private static void goPress(final String oid) throws IOException {
		final DefaultTableModel model = (DefaultTableModel) resultTable.getModel();
		if (comboBox.getSelectedItem() == "Get") {
			String stringGot = client.get(ipTextField.getText(), oid);
			if (stringGot != null) {
				model.addRow(new Object[]{oid, stringGot, "Get", ipTextField.getText()});
			}
			else {
				model.addRow(new Object[]{oid, "Get Failed", "Get", ipTextField.getText()});
			}
			
			resizeTable();
		}
		else if (comboBox.getSelectedItem() == "Set") {
			boolean valueSet = client.set(ipTextField.getText(), OIDTextField.getText(), setTextField.getText());
			if (valueSet) {
				model.addRow(new Object[]{oid, client.get(ipTextField.getText(), oid), "Set", ipTextField.getText()});
			}
			else {
				model.addRow(new Object[]{oid, "Set Failed", "Set", ipTextField.getText()});
			}
			
			resizeTable();
		}
		else if (comboBox.getSelectedItem() == "Mult") {
			  try { 
			        Integer.parseInt(setTextField.getText()); 
			    } catch(Exception e) { 
			    	 JOptionPane.showMessageDialog(null, "Not an integer.", "Warning",  JOptionPane.WARNING_MESSAGE);
			        return;
			    }
			
			if (comboBox.isEnabled()) {
				comboBox.setEnabled(false);
				ipTextField.setEnabled(false);
				OIDTextField.setEnabled(false);
				goButton.setText("Stop");
				t = new Timer();
				t.schedule(new TimerTask() {
				    public void run() {
				    	try {
							String stringGot = client.get(ipTextField.getText(), oid);
							if (stringGot != null) {
								model.addRow(new Object[]{oid, stringGot, "Get", ipTextField.getText()});
							}
							else {
								model.addRow(new Object[]{oid, "Get Failed", "Get", ipTextField.getText()});
							}
							
							//resize multiple times because the timer isn't playing well with my auto scroll...
							resizeTable();
							resizeTable();
							resizeTable();
							resizeTable();
							resizeTable();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				    	finally {
				    		
				    	}
				    }
				}, 0, Integer.parseInt(setTextField.getText())*1000);
			}
			else {
				t.cancel();
				comboBox.setEnabled(true);
				ipTextField.setEnabled(true);
				OIDTextField.setEnabled(true);
				goButton.setText("Go");
			}
		}
	}
	
	//function that handles the trap button being pressed
	private static void trapPress() throws IOException {
		if (trapOn) {
			trapOn = false;
			trapReceiver.setText("Start trap receiver");
			trapReceiver.setBackground(Color.GREEN);
		}
		else {
			trapOn = true;
			trapReceiver.setText("Stop trap receiver");
			trapReceiver.setBackground(Color.RED);
		}
		
	}

	//start the client function
	public void startSnmpClient() throws IOException, SmiParseException {
		client = new SnmpHelper();
	}
	
	//function that gets called when a trap is received
	public void receivedTrap(String oid, String desc) {
		if (!trapOn) {
			return;
		}
		
		DefaultTableModel model = (DefaultTableModel) resultTable.getModel();
		model.addRow(new Object[]{oid, desc, "Trap", "127.0.0.1:162"});
		resizeTable();
	}
	
	//resize table when something is added
	private synchronized static void resizeTable() {
		panel_1.revalidate();
		panel_1.repaint();
		resultTable.scrollRectToVisible(resultTable.getCellRect(resultTable.getRowCount()-1, resultTable.getColumnCount(), true));
		panel_1.revalidate();
		panel_1.repaint();
	}
}
