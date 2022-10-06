import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.charset.*;
import java.nio.file.Files;
import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;

public class Worker extends Node{
    static final int DEFAULT_PORT = 50001;
	byte[] bytes;	// Data Stream
	String lastFileRequest;
	int currentFrame = 0;
	boolean fileSent = false;

    Worker(int port) {		
		try {
			socket= new DatagramSocket(port);
			listener.go();
		}
		catch(java.lang.Exception e) {e.printStackTrace();}
	}

    public void onReceipt(DatagramPacket packet) {
		try {
			System.out.println("Received packet");
			PacketContent content= PacketContent.fromDatagramPacket(packet); // Create packet content obj, 

			if (content.getType()==PacketContent.MESSAGEPACKET) {
				// If send message recieved send file
				if(((MessagePacketContent)content).getPacketMessage().equalsIgnoreCase("send")){ // WARNING may need fix
					File f = new File(lastFileRequest);
					bytes = new byte[(int) f.length()];		
					FileInputStream fis = null;
					try{
						fis = new FileInputStream(f);
						fis.read(bytes);
					}catch (java.lang.Exception e) {e.printStackTrace();}
					fis.close();
					fileSent = false;
					System.out.println("Sending dataPacket");
					currentFrame = 0;	// Set starting frame
					sendDataPacket(bytes, currentFrame, packet);
					currentFrame ++;	// increment to next frame
				}
				// CHECK FOR FILE ON SERVER
				else{
					System.out.println("Checking for file: "  + ((MessagePacketContent)content).getPacketMessage());
					File f = new File(((MessagePacketContent)content).getPacketMessage());
					String response = Boolean.toString(f.exists());
					lastFileRequest = ((MessagePacketContent)content).getPacketMessage();
					System.out.println("Existanse of file: " + response);
					
					// Return response message
					System.out.println("Sending response to query");
					sendMessage(response, packet);
					System.out.println("Sent packet");
				}
			}
			// AFTER RECIEVING ACK ON DATA RETRIEVAL
			else if(content.getType()==PacketContent.ACKPACKET){
				System.out.println("Recieved ACK");
				// Send Data
                if(fileSent == false){
					System.out.println("Sending data packet");
					sendDataPacket(bytes, currentFrame, packet);
					System.out.println("Sent data packet");
					currentFrame ++;
				}
				// return completion message
                else{
					System.out.println("Sending completion message");
					sendMessage("finished", packet);
					System.out.println("Sent packet");

				}
			}
			else System.out.println("Something went wrong!! Packet Type: " + content.getType());
		}
		catch(Exception e) {e.printStackTrace();}
    }

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
				sendMessage("File Sent", dataPacket);
			}
		} catch (java.lang.Exception e) {e.printStackTrace();}
	}

    public synchronized void start() throws Exception {
		while(true){
			System.out.println("Waiting for contact");
			this.wait();
			System.out.println("Contact recieved");
		}
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

    public static void main(String[] args) {
		try {
			(new Server(DEFAULT_PORT)).start();
			System.out.println("Program completed");
		} catch(java.lang.Exception e) {e.printStackTrace();}
	}
}
