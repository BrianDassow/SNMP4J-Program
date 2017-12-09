import java.awt.Color;
import java.io.File;
import java.io.IOException;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.CommunityTarget;
import org.snmp4j.MessageDispatcher;
import org.snmp4j.MessageDispatcherImpl;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.mp.MPv1;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.security.Priv3DES;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.TransportIpAddress;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.MultiThreadedMessageDispatcher;
import org.snmp4j.util.ThreadPool;

import com.snmp4j.smi.SmiParseException;

public class MainListener implements CommandResponder {
	static MainFrame main;
	
	//main method
	public static void main(String[] args) throws SmiParseException, ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
		//Why not set it to windows... good luck mac :)
		UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		//alternating table colors
		UIManager.put("Table.alternateRowColor", new Color(220, 220, 220));
		
		//Create mibs directory if it doesn't exist
		new File(System.getProperty("user.dir") + "\\mibs").mkdir();
		
		MainListener trapReceiver = new MainListener();
		try {
			//setup mainframe
			main = new MainFrame();
			main.setVisible(true);
			//dont forget to start the client
			main.startSnmpClient();
			//listen for traps...
			trapReceiver.listen(new UdpAddress("127.0.0.1/162"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	//fucntion to start listening
	public synchronized void listen(UdpAddress address) throws IOException {
		//get transport mapping
		DefaultUdpTransportMapping transport = new DefaultUdpTransportMapping(address);

		//create a threadpool to listen and get
		ThreadPool threadPool = ThreadPool.create("Pool", 20);
		MessageDispatcher dispathcher = new MultiThreadedMessageDispatcher(threadPool, new MessageDispatcherImpl());

		//add message processing models
		dispathcher.addMessageProcessingModel(new MPv1());
		dispathcher.addMessageProcessingModel(new MPv2c());

		//add some security because why not
		SecurityProtocols.getInstance().addDefaultProtocols();
		SecurityProtocols.getInstance().addPrivacyProtocol(new Priv3DES());

		//create target and set it's community
		CommunityTarget target = new CommunityTarget();
		target.setCommunity(new OctetString("public"));

		//create snmp object and add "this" specific object to listen for incoming traps
		Snmp snmp = new Snmp(dispathcher, transport);
		snmp.addCommandResponder(this);

		//make it listen
		transport.listen();

		try {
			this.wait();
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
	}

	//function to process incoming events
	public synchronized void processPdu(CommandResponderEvent event) {
		//get the pdu
		PDU pdu = event.getPDU();
		
		//process the pdu (find out if its v1 trap or v2 trap then get the parts of it and send it off to the mainframe to add to table)
		if (pdu != null) {
			if (pdu.toString().contains("V1TRAP")) {
				String[] parts = pdu.toString().split("timestamp=")[1].split(",enterprise=");
				main.receivedTrap(parts[1].split(",")[0], parts[0]);
			}
			else if (pdu.toString().contains("TRAP")) {
				String[] parts = pdu.toString().split("VBS")[1].substring(1, pdu.toString().split("VBS")[1].length()).split(";");
				parts = parts[0].split(" = ");
				main.receivedTrap(parts[0], parts[1]);
			}
		}
	}
}