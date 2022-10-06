import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;



public class DataPacketContent extends PacketContent {
        byte[] data;
		//String dataString;
    // Constructor takes data from file
    DataPacketContent(byte[] data){ 
        type= DATAPACKET;
        this.data = data;
    }



    /**
	 * Constructs an object out of a datagram packet.
	 * @param packet Packet that contains information about a file.
	 */
	protected DataPacketContent(ObjectInputStream oin) {
		try {
			System.out.println("TESTING data object input stream");
			//System.out.println("preTest data: " + data + " dataString:" + dataString); // testing
			type= DATAPACKET;
			//dataString = data.toString();
			//dataString = oin.readUTF();
			
			// For loop to read 
			System.out.println("TESTING reading bytes creating arraylist");
			ArrayList<Byte> tmp = new ArrayList<Byte>();
			int available = oin.available();
			//byte tmpb = oin.readByte();
			System.out.println("TESTING available " + available);
			try{ 
				while(oin.available()!=0){//for(int i=0; i<65000; i++){	// READS 10 Bytes
					//System.out.println("TESTING available " + oin.available());
					tmp.add(oin.readByte());
				}
			} catch(java.lang.Exception e){}
			System.out.println("TESTING reading bytes creating byte array");
			data = new byte[tmp.size()];
			for(int i=0; i<tmp.size(); i++){
				data[i] = tmp.get(i);
			}
			


			//System.out.println("midTest data: " + data + " dataString:" + dataString); // testing
			//data = dataString.getBytes();
			//System.out.println("TESTING data object input stream set");
			//System.out.println("data: " + data + " dataString:" + dataString); // testing
		}
		catch(Exception e) {e.printStackTrace();}
	}

	/**
	 * Writes the content into an ObjectOutputStream
	 *
	 */
	protected void toObjectOutputStream(ObjectOutputStream oout) {
		try {
			System.out.println("TESTING data object output stream");
			//System.out.println("TESTING oout values, data: " + data + " dataString: " + dataString);
			//oout.writeUTF(dataString);	//Write byte for loop

			for(int i = 0; i<=data.length-1; i++){
				oout.writeByte(data[i]);
			}
		}
		catch(Exception e) {e.printStackTrace();}
	}

    public String toString() {
		return "DATA:" + data;
	}

    public byte[] getPacketData() {
		return this.data;
		//return dataString.getBytes();  // SHOULD FAIL IF NOT SENT AS PACKET
	}
}
