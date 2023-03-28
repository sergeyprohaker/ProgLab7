package server;

import server.utils.RequestHandler;
import common.functional.*;
import javax.xml.crypto.Data;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class Server {

    private int port;
    private final DatagramSocket datagramSocket;
    private final byte[] BUFFER = new byte[4096];
    private RequestHandler requestHandler;
    private InetAddress host = InetAddress.getByName("localhost");;
    public Server(int port, RequestHandler requestHandler) throws SocketException, UnknownHostException {
        this.port = port;
        this.requestHandler = requestHandler;
        this.datagramSocket = new DatagramSocket(this.port);

    }



    private Request receiveData() {

        Request userRequest = null;

        try {
            DatagramPacket receivedPacket = new DatagramPacket(BUFFER, BUFFER.length);

            datagramSocket.receive(receivedPacket);
            byte[] receivedData = receivedPacket.getData();
            ByteArrayInputStream in = new ByteArrayInputStream(receivedData);
            ObjectInputStream ois = new ObjectInputStream(in);
            userRequest = (Request) ois.readObject();
            this.host = receivedPacket.getAddress();
            this.port = receivedPacket.getPort();


        }catch (IOException | ClassNotFoundException e){
            e.printStackTrace();
        }
        //выполняем действия

        return userRequest;
    }

    private void sendData(Response response){
        byte[] sendByteArray = null;


        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(response);
            sendByteArray = bos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }



        assert sendByteArray != null;
        DatagramPacket packet = new DatagramPacket(sendByteArray, sendByteArray.length, host, port);
        try {
            datagramSocket.send(packet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void connection() {
        while (true) {
            Request request = receiveData();

            Response response = requestHandler.handle(request);
            System.out.println(response);
            sendData(response);
        }
    }
}
