SNMP4J Program

This program was created to do get, sets, and traps for snmp.

To run this program, set it up in eclipse and add the libraries in the lib folder to the project build path.  Below is a reference on how to do that if you do not know how to.
https://www.wikihow.com/Add-JARs-to-Project-Build-Paths-in-Eclipse-(Java)

To use get/set in this program:
Run the program
Enter in IP and port that you want to use
Enter in an OID
From the drop down menu select get or set
If set, enter in a value that you want to set it to
press Go

Mult is just a name for getting multiple gets.

To use mult in this program:
Run the program
Enter in IP and port that you want to use
Enter in an OID
From the drop down menu select mult
Enter in an integer that you want your time period to be (in seconds)
press Go

To use trap receiver:
Run the program
Press "Start trap receiver"
the program will automatically listen on port 162 for traps
Press "Stop trap receiver" to stop receiving trap messages

This program will automatically create a mibs folder where you keep the source files (ex. where programdirectory/src is it will create programdirectory/mibs).  Keep your precompiled mibs in the mibs folder for easy access to them.

To load a mib file:
Run the program
Press "File" at the top
Select "Load MIB"
From the prompt, select a mib file that you would like to load
Press "Open"

Unfortunately doing anything with the loaded mib files, other than loading them, only comes with the pro version.  Since the pro version is $700 that functionality is not included in this program.

Get, set, and trap captures are located in the "captures" folder.  An overall photo displaying the program is also included.

Created by Brian Dassow