import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class MessagePacketContent extends PacketContent{
    String message;

    MessagePacketContent(String message){
        type= MESSAGEPACKET;
        this.message = message;
    }
    
    protected MessagePacketContent(ObjectInputStream oin) {
		try {
			type= MESSAGEPACKET;
			message= oin.readUTF();
		}
		catch(Exception e) {e.printStackTrace();}
	}

    protected void toObjectOutputStream(ObjectOutputStream oout) {
		try {
			oout.writeUTF(message);
		}
		catch(Exception e) {e.printStackTrace();}
	}

    public String toString() {
		return "Message: " + message;
	}

    public String getPacketMessage() {
		return message;
	}
}
