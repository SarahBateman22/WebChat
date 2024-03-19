import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class MyRunnable implements Runnable{

    Socket client_;
    private String username_;
    private String roomName_;
    private String message_;

    RoomManager rm_;

    public MyRunnable(Socket client, RoomManager rm) {
        client_ = client;
        username_ = null;
        roomName_ = null;
        message_ = null;
        rm_ = rm;
    }

    private String decodeMessage() throws IOException {
        Boolean fin;
        int opcode;
        Boolean mask;
        long payloadLen;
        byte[] maskBytes = new byte[0];
        String message;

        DataInputStream in = new DataInputStream(client_.getInputStream());

        byte[] input = in.readNBytes(2);

        fin = (input[0] & 0x80) > 0;
        opcode = (input[0] & 0x0F);
        mask = (input[1] & 0x80) > 0;
        payloadLen = (input[1] & 0x7f);

        if (payloadLen == 126) {
            payloadLen = in.readShort();
        } else if (payloadLen == 127) {
            payloadLen = in.readLong();
        }

        if (mask) {
            maskBytes = in.readNBytes(4);
        }

        byte[] payloadArr = in.readNBytes((int) payloadLen);

        if (mask) {
            for (int i = 0; i < payloadArr.length; i++) {
                payloadArr[i] = (byte) (payloadArr[i] ^ maskBytes[i % 4]);
            }
            message = new String(payloadArr, StandardCharsets.UTF_8);
        }
        else {
            message = new String(payloadArr, StandardCharsets.UTF_8);
        }
        return message;
    }

    public void encodeMessage(String message) {
        try {
            DataOutputStream dataOut = new DataOutputStream(client_.getOutputStream());
            byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);

            byte[] frame = new byte[messageBytes.length + 2];
            frame[0] = (byte) 0x81;
            frame[1] = (byte) messageBytes.length;

            System.arraycopy(messageBytes, 0, frame, 2, messageBytes.length);

            dataOut.write(frame);
            dataOut.flush();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parseMessType(String message) {
        String cmd_;
        String[] messArr = message.split(":");
        cmd_ = messArr[0];
        username_=messArr[1];
        roomName_=messArr[2];
        if(cmd_.equals("join")){
            rm_.joinRoom(this);
        }
        if (cmd_.equals("leave")){
            rm_.leaveRoom(this);
        }
        if (cmd_.equals("message")){
            message_=messArr[3];
            rm_.sendMess(message_, this);
        }
    }

    public String getUsername_(){
        return username_;
    }
    public String getRoomName_(){
        return roomName_;
    }
    public String getMessage_(){
        return message_;
    }

    public static String makeJoinMsg(String room, String name){
        return "{ \"type\": \"join\", \"room\": \"" + room + "\", \"user\": \"" + name + "\" }";
    }
    public static String makeMsgMsg(String room, String name, String message) {
        return "{ \"type\": \"message\", \"user\": \"" + name + "\", \"room\": \"" + room + "\", \"message\": \"" + message + "\" }";
    }
    public static String makeLeaveMsg(String room, String name) {
        return "{ \"type\": \"leave\", \"room\": \"" + room + "\", \"user\": \"" + name + "\" }";
    }

    @Override
    public void run() {
        HTTPResponse response = new HTTPResponse();
        HTTPRequest request = null;
        try {
            request = new HTTPRequest(client_);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            if (!request.parse()) {
                System.out.println("bad req");
            }

            if (request.isWebSocket()) {
                response.sendWebSocketResponse(client_, request.getWebSocketKey());
                String endMessage = new String(new byte[]{3,-23});
                boolean done = false;
                while (!done) {
                    String msg = decodeMessage();
                    if( msg.equals(endMessage)){
                        System.out.println("closing socket");
                        done = true;
                        client_.close();
                    }
                    else{
                        parseMessType(msg);
                    }
                }
            }
            else {
                File file = response.createFile(request.getParameter());
                response.sendResponse(client_, file, request.getFileType());
                client_.close();
            }
        }
        catch (Exception e) {
            try {
                response.sendFailResponse(client_);
            }
            catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}

