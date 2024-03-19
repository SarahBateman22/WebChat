import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Scanner;

public class HTTPResponse {
    public File createFile(String filename) {

        return new File("Resources" + filename);
    }

    /*sendResponse creates an OutputStream from the socket, passes a file into FileInputStream,
    and sends a header based on the filetype.*/
    public void sendResponse(Socket client, File file, String fileType) throws IOException {
        OutputStream outStream = client.getOutputStream();
        FileInputStream fileStream = null;
        if (fileType != null)
            fileStream = new FileInputStream(file);
        outStream.write("http/1.1 200 Success \n".getBytes());
        if (fileType.equals("jpeg")) {
            outStream.write(("Content-type: image/" + fileType + "\n").getBytes());
        } else if (fileType.equals("pdf")) {
            outStream.write(("Content-type: application/" + fileType + "\n").getBytes());
        } else {
            outStream.write(("Content-type: text/" + fileType + "\n").getBytes());
        }
        outStream.write("\n".getBytes());
        fileStream.transferTo(outStream);
        outStream.flush();
        outStream.close();
    }

    public void sendWebSocketResponse(Socket client, String key) throws IOException {
        OutputStream outStream = client.getOutputStream();
        key += "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(key.getBytes("UTF-8"));
            key = Base64.getEncoder().encodeToString(digest);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        outStream.write("HTTP/1.1 101 Switching Protocols\r\n".getBytes());
        outStream.write("Upgrade: websocket\r\n".getBytes());
        outStream.write("Connection: Upgrade\r\n".getBytes());
        outStream.write(("Sec-WebSocket-Accept: " + key + "\r\n").getBytes());
        outStream.write("\r\n".getBytes());
        outStream.flush();
    }

    public void sendFailResponse(Socket client) throws IOException {
        File failFile = new File("Resources/failMessage.html");
        OutputStream outStream = client.getOutputStream();
        FileInputStream failFileStream = new FileInputStream(failFile);

        outStream.write("HTTP/1.1 200 OK\n".getBytes());
        outStream.write("Content-type: text/html\n".getBytes());
        outStream.write("\n".getBytes("UTF-8"));
        failFileStream.transferTo(outStream);
        outStream.flush();
        outStream.close();

    }

}