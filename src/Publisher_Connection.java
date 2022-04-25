import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

class Publisher_Connection implements Runnable{
    private Socket publisher_connection;
    private ObjectInputStream is;
    private ObjectOutputStream os;
    private final int chunksize = 512*1024;
    private Broker broker;

    Publisher_Connection(Socket publisher_connection, Broker broker){
        this.publisher_connection = publisher_connection;
        this.broker = broker;
        try {
            is = new ObjectInputStream(publisher_connection.getInputStream());
            os = new ObjectOutputStream(publisher_connection.getOutputStream());
        } catch (IOException e) {
            System.out.println("Error in constructor shutting down connection...");
            e.printStackTrace();
            shutdownConnection();
        }
    }

    public void receiveFile(){

        try {
            int bytes = 0;
            System.out.println("Receiving file...");
            String file_name = is.readUTF();
            String new_file = file_name.substring(file_name.lastIndexOf("\\")+1);
            System.out.println("Received file: " + new_file);
            int number_of_chunks = is.readInt();
            System.out.println("You will receive: " + number_of_chunks + " chunks");
            String path_for_broker = new String("C:\\Users\\fotis\\OneDrive\\Desktop\\receive_files\\");
            FileOutputStream fileOutputStream = new FileOutputStream(new File(path_for_broker + new_file));
            System.out.println("Receiving file...");
            byte[] buffer = new byte[chunksize];
            ArrayList<byte[]> chunks = new ArrayList<>();
            while(true) {
                if(number_of_chunks == chunks.size()){
                    System.out.println("Finished receiving chunks: " + chunks.size());
                    fileOutputStream.close();
                    break;
                }
                int actual_size = is.readInt();
                System.out.println("Actual size of the incoming chunk is: " + actual_size);
                is.readFully(buffer,0,actual_size);
                byte[] temp = buffer.clone();
                fileOutputStream.write(temp,0,actual_size);
                fileOutputStream.flush();
                chunks.add(temp);
                System.out.println("Sending received chunk ack");
                os.writeInt(Messages.RECEIVED_CHUNK.ordinal());
                os.flush();
                while(true){
                    if(Messages.RECEIVED_ACK.ordinal() == is.readInt()){
                        System.out.println("Client received ack");
                        break;
                    }
                }
                System.out.println("Chunks size now is: " + chunks.size());
            }
            System.out.println("Finished receiving file");

        } catch (IOException e) {
            System.out.println("Shutting down in receive file...");
            e.printStackTrace();
            shutdownConnection();
        }
    }

    public int waitForUserNodePrompt(){
        try {
            System.out.println("Waiting for user node prompt in publisher connection");
            return is.readInt();
        } catch (IOException e) {
            System.out.println("Shutting down connection in wait for user node...");
            shutdownConnection();
        }
        return -1;
    }


    public void FinishedOperation(){
        try {
            os.writeInt(Messages.FINISHED_OPERATION.ordinal());
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Shutting down connection in finished operation...");
            shutdownConnection();
        }
    }

    @Override
    public void run() {
        System.out.println("Established connection with publisher: " + publisher_connection.getInetAddress());
        int message;
        message = waitForUserNodePrompt();
        System.out.println("Received message from publisher: " + message);
        while (publisher_connection.isConnected()) {
            if(message == Messages.FINISHED_OPERATION.ordinal()){
                System.out.println("Finished operation waiting for publisher's next input");
                message = waitForUserNodePrompt();
            }
            else if (message == Messages.NOTIFY.ordinal()) {
                System.out.println("Notify message was received by publisher: " + publisher_connection.getInetAddress().getHostName());
                receiveFile();
                FinishedOperation();
                message = waitForUserNodePrompt();
            }
        }

    }

    public void notifyPublisher(Topic topic){

    }

    public void removeConnection(){
        broker.getPublisher_Connections().remove(this);
    }

    public void shutdownConnection(){
        System.out.println("Shutted connection: " + publisher_connection.getInetAddress());
        removeConnection();
        try{
            if(os != null){
                os.close();
            }
            if(is != null){
                is.close();
            }
            if(publisher_connection != null){
                publisher_connection.close();
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }
}
