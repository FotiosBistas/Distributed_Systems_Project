package Broker;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class InterBrokerCommunications{
    private MulticastSocket multicastSocket = null;
    private final byte[] id_buffer = new byte[3];
    private final byte[] alive_buffer = new byte[5];
    private final String multicast_host = "225.6.7.8";
    private final Broker caller_broker;
    private final int datagram_port = 9860;
    private final String alive_message = "alive";
    private static ScheduledExecutorService executorCompletionService = Executors.newScheduledThreadPool(1);
    InterBrokerCommunications(Broker caller_broker){
        this.caller_broker = caller_broker;
        executorCompletionService.scheduleAtFixedRate(this::sendAliveMessage,5,5, TimeUnit.SECONDS);
        new Thread(this::receiveAliveMessage).start();
        try {
            multicastSocket = new MulticastSocket(datagram_port);
            InetAddress group = InetAddress.getByName(multicast_host);
            multicastSocket.joinGroup(group);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @param sender_id
     * @param msg_received
     */
    private void setAlive(String sender_id,String msg_received){
        if(msg_received.equals("alive")){
            int id = Integer.parseInt(sender_id);
            int index = caller_broker.getId_list().indexOf(id);

        }
    }

    /**
     * Receives the alive messages from the other brokers
     */
    private void receiveAliveMessage(){
        System.setProperty("java.net.preferIPv4Stack","true");
        try{
            while(true) {
                InetAddress group = InetAddress.getByName(multicast_host);
                MulticastSocket multicastSocket = new MulticastSocket(datagram_port);
                DatagramPacket packet = new DatagramPacket(id_buffer, id_buffer.length);
                multicastSocket.joinGroup(group);
                multicastSocket.receive(packet);
                //in order to identify which broker sent the alive message you need its ID
                String sender_ID = new String(packet.getData(), StandardCharsets.UTF_8);
                System.out.println("Sender ID: " + sender_ID);
                packet = new DatagramPacket(alive_buffer, alive_buffer.length);
                multicastSocket.receive(packet);
                String alive_msg = new String(packet.getData(), StandardCharsets.UTF_8);
                System.out.println("Alive message: " + alive_msg);
                setAlive(sender_ID,alive_msg);
            }
        }catch (IOException ioException){
            closeSocket();
        }
    }
    /**
     * Sends an alive message every interval specified at broker start.
     */
    private void sendAliveMessage(){
        System.setProperty("java.net.preferIPv4Stack","true");
        try{
            InetAddress group = InetAddress.getByName(multicast_host);
            MulticastSocket multicastSocket = new MulticastSocket();
            System.out.println("Sending alive message for broker: " + caller_broker.getId());
            String string_id = String.valueOf(caller_broker.getId());
            DatagramPacket ip_packet = new DatagramPacket(
                    string_id.getBytes(),
                    string_id.length(),
                    group,
                    datagram_port
            );
            DatagramPacket alive_packet = new DatagramPacket(
                    alive_message.getBytes(),
                    alive_message.length(),
                    group,
                    datagram_port
            );
            multicastSocket.send(ip_packet);
            multicastSocket.send(alive_packet);
            multicastSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void closeSocket(){
        if(multicastSocket != null) {
            multicastSocket.close();
        }
    }

}
