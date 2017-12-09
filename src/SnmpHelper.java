import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.SNMP4JSettings;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import com.snmp4j.smi.SmiManager;
import com.snmp4j.smi.SmiParseException;

public class SnmpHelper {

	private SmiManager smiManager;
	private Snmp snmp;
	private final String community = "public";

	public SnmpHelper() {
		try {
			loadSMIManager();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	//load smi manager so i can do things like load mib files
	private void loadSMIManager() throws IOException {
		//null is to use free version of SMIManager.  SMIManager is used to load the MIB
		smiManager = new SmiManager(null, new File(System.getProperty("user.dir") + "\\mibs"));
		// Register the SmiManager as OID and VariableBinding formatter with SNMP4J:
		SNMP4JSettings.setOIDTextFormat(smiManager);
		SNMP4JSettings.setVariableTextFormat(smiManager);
	}
	
	//function to compile a MIB
	public void compileMIB(String filePath) throws FileNotFoundException, SmiParseException {
		smiManager.compile(new File(filePath));
	}
	
	//function to load a mib
	public boolean loadMIB(String mib) {
		System.out.println("load MIB: " + mib);
		return smiManager.loadModule(mib);
	}
	
	//function to unload a mib
	public boolean unloadMIB(String mib) {
		return smiManager.unloadModule(mib);
	}

	//function to set
	public boolean set(String ip, String oidToSet, String toWhat) throws IOException {
		int snmpVersion = SnmpConstants.version2c;
		boolean valueSet = false;

		//create transport then listen to it
		TransportMapping transport = new DefaultUdpTransportMapping();
		transport.listen();

		//create target
		CommunityTarget target = new CommunityTarget();
		target.setCommunity(new OctetString(community));
		target.setVersion(snmpVersion);
		target.setAddress(new UdpAddress(ip));
		target.setRetries(1);
		target.setTimeout(1000);

		//create pdu
		PDU pdu = new PDU();

		//create oid and set pdu to "set"
		OID oid = new OID(oidToSet);
		Variable var = new OctetString(toWhat);
		VariableBinding varBind = new VariableBinding(oid, var);
		pdu.add(varBind);

		pdu.setType(PDU.SET);
		pdu.setRequestID(new Integer32(1));

		//create snmp... then do set... then get response
		Snmp snmp = new Snmp(transport);
		ResponseEvent response = snmp.set(pdu, target);

	    //do something with the response
	    if (response != null) {
	      PDU responsePDU = response.getResponse();

	      if (responsePDU != null) {
	        int errorStatus = responsePDU.getErrorStatus();
	        
	        if (errorStatus == PDU.noError) {
	        	valueSet = true;
	        }

	      }
	    }
	    
	    //return if it was successful
	    snmp.close();
	    return valueSet;
	}

	//function to get
	public String get(String ip, String oid) throws IOException {
		String returnValue = null;
		int snmpVersion = SnmpConstants.version2c;

		//create transport and listen
		TransportMapping transport = new DefaultUdpTransportMapping();
		transport.listen();

		//create target
		CommunityTarget target = new CommunityTarget();
		target.setCommunity(new OctetString(community));
		target.setVersion(snmpVersion);
		target.setAddress(new UdpAddress(ip));
		target.setRetries(1);
		target.setTimeout(1000);

		//create pdu
		PDU pdu = new PDU();
		pdu.add(new VariableBinding(new OID(oid)));
		pdu.setType(PDU.GET);
		pdu.setRequestID(new Integer32(1));

		//create snmp object
		Snmp snmp = new Snmp(transport);

		//do get and get response
		ResponseEvent response = snmp.get(pdu, target);

		//do something with the response
		if (response != null) {
			PDU responsePDU = response.getResponse();

			if (responsePDU != null) {
				int errorStatus = responsePDU.getErrorStatus();
				
				if (errorStatus == PDU.noError) {
					returnValue = responsePDU.get(0).getVariable().toString();
				}
			}
		}
		
		//close and return if the get was a success
		snmp.close();
		return returnValue;
	}
}