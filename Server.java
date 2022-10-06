import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.charset.*;
import java.nio.file.Files;
import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;

public class Server extends Node {
	static final int DEFAULT_PORT = 50001;
	byte[] bytes;	// Data Stream
	String lastFileRequest;
	String response;
	Boolean fileFound;
	int currentFrame = 0;
	boolean fileSent = false;
	/*
	 *
	 */
	Server(int port) {		// MAY NOT BE WORKING!!!!!!!!! PROBABLY ISN'T
		try {
			socket= new DatagramSocket(port);
			listener.go();
		}
		catch(java.lang.Exception e) {e.printStackTrace();}
	}

	/**
	 * Assume that incoming packets contain a String and print the string.
	 */
	public void onReceipt(DatagramPacket packet) {
		try {
			System.out.println("Received packet");
			PacketContent content= PacketContent.fromDatagramPacket(packet); // Create packet content obj, 

			if (content.getType()==PacketContent.MESSAGEPACKET) {
				// If send message recieved send file
				if(((MessagePacketContent)content).getPacketMessage().equalsIgnoreCase("send")){ // WARNING may need fix
					sendMessage("send", LASTWORKERADDRESS); // send request for file at last address
					fileSent = false;
				}
				else if(((MessagePacketContent)content).getPacketMessage().equalsIgnoreCase("finished")){ // WARNING may need fix
					System.out.println("Finished recieving file packets");
					fileSent = true;
				}
				// if recieved response  !!!! NOT IMPLEMENTED YET
				else if(((MessagePacketContent)content).getPacketMessage().equalsIgnoreCase("true")){ // WARNING may need fix
					System.out.println("File found");
					fileFound = true;
					this.notify();
				}
				else if(((MessagePacketContent)content).getPacketMessage().equalsIgnoreCase("true")){ // WARNING may need fix
					System.out.println("File not found");
					fileFound = false;
					this.notify();
				}
				else{
					System.out.println("Checking for file: "  + ((MessagePacketContent)content).getPacketMessage());
					
					lastFileRequest = ((MessagePacketContent)content).getPacketMessage();

					this.notify(); // notifies the start() thread

					// Check workers
					/* 
					InetSocketAddress addressw1 = new InetSocketAddress(null, DEFAULT_PORT) // !!! (address, port) of worker1
					sendMessageTest(lastFileRequest, addressw1); // Send request for message
					this.wait(); 
					InetSocketAddress addressw2 = new InetSocketAddress(null, DEFAULT_PORT) // !!! (address, port) of worker2
					sendMessageTest(lastFileRequest, addressw2);
					this.wait();
					
					System.out.println("Existanse of file: " + response);
					
					// Return response message to client
					System.out.println("Sending response to query");
					InetSocketAddress addressc = new InetSocketAddress(null, DEFAULT_PORT) // !!! (address, port) of client
					sendMessage(response, addressc); // Send to client
					System.out.println("Sent packet");
					*/
				}
			}
			// AFTER RECIEVING ACK ON DATA RETRIEVAL
			else if(content.getType()==PacketContent.ACKPACKET){
				// Client Ack

				System.out.println("Recieved ACK");
				if(fileSent == false){
					System.out.println("Waiting for next packet")
				}
				else{
					// return completion message
					System.out.println("Sending completion message");
					sendMessage("finished", packet);
					System.out.println("Sent packet");

				}
			}
			
			else if(content.getType()==PacketContent.DATAPACKET){
				try{
					// Data
					System.out.println("Recieved data");
					//Send ACK
					DatagramPacket response;
					response= new AckPacketContent("OK - Received this").toDatagramPacket();
					response.setSocketAddress(packet.getSocketAddress());
					socket.send(response);
					System.out.println("Sent ACK response");
					//Send packet straight to client
					System.out.println("Sending packet to client");
					packet.setSocketAddress(CLIENTADDRESS);
					socket.send(packet);
					} catch(Exception e) {e.printStackTrace();}
			}

			else System.out.println("Something went wrong!! Packet Type: " + content.getType());
		}
		catch(Exception e) {e.printStackTrace();}
    }

		// Should work
    	public synchronized void start() throws Exception {
		while(true){
			System.out.println("Waiting for contact");
			this.wait();

				InetSocketAddress addressw1 = new InetSocketAddress(null, DEFAULT_PORT) // !!! (address, port) of worker1
				sendMessageTest(lastFileRequest, addressw1); // Send request for message
				this.wait(); 
				if(fileFound == true){
					System.out.println("Sending response to query");
					InetSocketAddress addressc = new InetSocketAddress(null, DEFAULT_PORT) // !!! (address, port) of client
					sendMessage(response, addressc); // Send to client
					System.out.println("Sent packet");
				}
				else{
					InetSocketAddress addressw2 = new InetSocketAddress(null, DEFAULT_PORT) // !!! (address, port) of worker2
					sendMessageTest(lastFileRequest, addressw2);
					this.wait();
					System.out.println("Sending response to query");
					InetSocketAddress addressc = new InetSocketAddress(null, DEFAULT_PORT) // !!! (address, port) of client
					sendMessage(response, addressc); // Send to client
					System.out.println("Sent packet");
				}


			System.out.println("Contact recieved");
		}
	}

	// Sends a data packet
	public synchronized void sendDataPacket(byte[]bytes, int frame, DatagramPacket packet){
		try{
			DataPacketContent data;
			DatagramPacket dataPacket;
			byte[] tmpByte;
			int i = frame*65000;	// 65000 taken as amount of bytes being sent
			// Send data packets to client
			if(bytes.length - i >= 65000){
				System.out.println("TESTING COND 1");
				tmpByte = Arrays.copyOfRange(bytes, i, i+65000);	// bytes i to i+65000
				data = new DataPacketContent(tmpByte);
				dataPacket = data.toDatagramPacket();
				dataPacket.setSocketAddress(packet.getSocketAddress());
				socket.send(dataPacket);
			}
			else{
				System.out.println("TESTING COND 2");
				tmpByte = Arrays.copyOfRange(bytes, i, bytes.length); // removed constraint probrably will break
				System.out.println("TESTING creating datagram packet");
				data = new DataPacketContent(tmpByte);
				dataPacket = data.toDatagramPacket();
				dataPacket.setSocketAddress(packet.getSocketAddress());
				System.out.println("TESTING sent packet");
				socket.send(dataPacket);
				System.out.println("TESTING packet sent");
				fileSent = true;
			}
		} catch (java.lang.Exception e) {e.printStackTrace();}
	}
	// test program
	public static void main(String[] args) {
		try {
			(new Server(DEFAULT_PORT)).start();
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

	// Function for sending a message packet
	public void sendMessage(String message, DatagramPacket packet){
		try{
		MessagePacketContent msg = new MessagePacketContent(message);
		DatagramPacket messagePacket = msg.toDatagramPacket();
		messagePacket.setSocketAddress(packet.getSocketAddress());
		socket.send(messagePacket);
		}catch (java.lang.Exception e) {e.printStackTrace();}
	}

	// Function for sending a message packet
	public void sendMessageTest(String message, InetSocketAddress address){
		try{
		MessagePacketContent msg = new MessagePacketContent(message);
		DatagramPacket messagePacket = msg.toDatagramPacket();
		messagePacket.setSocketAddress(address);
		socket.send(messagePacket);
		}catch (java.lang.Exception e) {e.printStackTrace();}
	}
}