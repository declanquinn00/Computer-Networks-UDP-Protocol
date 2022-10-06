/**
 *
 */
import java.util.Scanner;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.*;
import java.io.*; 

/**
 *
 * Client class
 *
 * An instance accepts user input
 *
 */
public class Client extends Node {
	static final int DEFAULT_SRC_PORT = 50000;
	static final int DEFAULT_DST_PORT = 50001;
	static final String DEFAULT_DST_NODE = "server";
	InetSocketAddress dstAddress;
	boolean finished = false;
	boolean fileFound = false;
	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	String lastQueryName = "";

	/**
	 * Constructor
	 *
	 * Attempts to create socket at given port and create an InetSocketAddress for the destinations
	 */
	Client(String dstHost, int dstPort, int srcPort) {
		try {
			dstAddress= new InetSocketAddress(dstHost, dstPort);
			socket= new DatagramSocket(srcPort);
			listener.go();
		}
		catch(java.lang.Exception e) {e.printStackTrace();}
	}

    /**
	 * Send request
	 *
	 */
	public synchronized void start() throws Exception {
        try{
		Scanner input = new Scanner(System.in);
        DatagramPacket packet= null;
		byte[] buffer;
		String request= null; // have a byte array for payload
		boolean quit = false;
		while(quit == false){ // CONTENTION
			System.out.println("Enter a filename or type quit: ");
			request = (input.nextLine());
			lastQueryName = request;	// SETS LAST QUERY BAD POSITION
			if(request == "quit"){
				System.out.print("Quitting...");
				quit = true;
				input.close();
			}
			else if(serverFileQuery(request)){	//!!!!!!!!!!!!!!!!! send request for file
				while(finished == false){
					this.wait(); // TESTING TO WAIT FOR PACKET RECIEPT
				}
				// Recieve file
				// MESSAGE RESPONSE
			}
			else{
				System.out.println("File not found");
			}
        }
        } catch(java.lang.Exception e) {e.printStackTrace();}
        
    }

	/**
	 * Assume that incoming packets contain a String and print the string.
	 */
	public synchronized void onReceipt(DatagramPacket packet) {
		System.out.println("Recieved incoming packet");
		PacketContent content= PacketContent.fromDatagramPacket(packet);

		if (content.getType()==PacketContent.MESSAGEPACKET) {
			try{
				if(((MessagePacketContent)content).getPacketMessage().equalsIgnoreCase("finished")){
					System.out.println("Writing to file");
					try(OutputStream tmpOutputStream = new FileOutputStream(lastQueryName)){	//file name
						outputStream.writeTo(tmpOutputStream);	// file contents
						tmpOutputStream.close();
					}
					System.out.println("File written testing...");
					File f = new File(lastQueryName);
					System.out.println("File exists: " + f.exists());

					// TESTING READ LINES!!!!!!!!!!!!!!
					try (BufferedReader br = new BufferedReader(new FileReader(lastQueryName))) {
						String line;
						while ((line = br.readLine()) != null) {
							System.out.println(line);
						}
						br.close();
					 }
					finished = true;
					outputStream.flush();
					

				}
				else{
					System.out.println("Query response: " + ((MessagePacketContent)content).getPacketMessage());
					if(((MessagePacketContent)content).getPacketMessage().equalsIgnoreCase("true")){	// !!!!!! UNSTABLE
						System.out.println("File was found");
						fileFound = true;
						finished = false;
						System.out.println("Sending request to send file");
						sendMessage("send", packet);
						System.out.println("Request sent");
					}
					else fileFound = false;
				}
			} catch(Exception e) {e.printStackTrace();}
		}
		else if(content.getType()==PacketContent.DATAPACKET){
			try{
			// Data
			System.out.println("Recieving data...");
			byte[] tmpBytes= ((DataPacketContent)content).getPacketData();
			outputStream.write(tmpBytes);
			//Send ACK
			DatagramPacket response;
			response= new AckPacketContent("OK - Received this").toDatagramPacket();
			response.setSocketAddress(packet.getSocketAddress());
			socket.send(response);
			System.out.println("Sent ACK response");
			} catch(Exception e) {e.printStackTrace();}
		}


		System.out.println(content.toString());
		this.notify();
	}


    public synchronized boolean serverFileQuery(String request) throws Exception{
		try{
			System.out.println("Sending request for file from server");
			MessagePacketContent msg = new MessagePacketContent(request);
			DatagramPacket packet = msg.toDatagramPacket();
			packet.setSocketAddress(dstAddress);
			socket.send(packet);

			PacketContent content = PacketContent.fromDatagramPacket(packet);
			int tmp = content.getType();
			System.out.println("Got packet type" + tmp); //!!!!!!!!!!!!!!

			System.out.println("Packet sent waiting for confirmation from server");
			this.wait(); // wait for return 
			System.out.println("File found before boolean = " + fileFound);
			if(fileFound == true){
				fileFound = false;	// Reset file query status
				return true;
			}
			else return false;
		} catch (java.lang.Exception e) {e.printStackTrace(); return false;}
    }

	/**
	 * Test method
	 *
	 * Sends a packet to a given address
	 */
	public static void main(String[] args) {
		try {
			(new Client(DEFAULT_DST_NODE, DEFAULT_DST_PORT, DEFAULT_SRC_PORT)).start();
			System.out.println("Program completed");
		} catch(java.lang.Exception e) {e.printStackTrace();}
	}

	// Function for sending a message packet
	public void sendMessage(String message, DatagramPacket packet){
		try{
		MessagePacketContent msg = new MessagePacketContent(message);
		DatagramPacket messagePacket = msg.toDatagramPacket();
		messagePacket.setSocketAddress(packet.getSocketAddress());
		socket.send(messagePacket);
		}catch (java.lang.Exception e) {e.printStackTrace();}
	}

}