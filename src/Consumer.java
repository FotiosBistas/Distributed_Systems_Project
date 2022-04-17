

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class Consumer extends UserNode implements Runnable, Serializable {

    private List<Tuple<String,Byte>> chunks_to_be_received;
    private boolean exit = false;
    private ObjectInputStream is;
    private ObjectOutputStream os;
    private Socket connection;

    public Consumer(String ip, int port) {
        super(ip, port);
    }

    @Override
    public void run(){
        try {
            connection = new Socket("localhost",1234);
            os = new ObjectOutputStream(connection.getOutputStream());
            is = new ObjectInputStream(connection.getInputStream());
            InputHandler inputHandler = new InputHandler();
            Thread t = new Thread(inputHandler);
            t.start();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            killclient();
        }
    }

    public void killclient(){
        exit = true;
        try{
            is.close();
            os.close();
            connection.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private class InputHandler implements Runnable{

        @Override
        public void run() {
            try{
                Scanner sc = new Scanner(System.in);
                System.out.println("What do you want to do?");
                String userinput = sc.nextLine();
                while(!exit) {
                    is = new ObjectInputStream(connection.getInputStream());
                    os = new ObjectOutputStream(connection.getOutputStream());
                    if (userinput.equals("register")) {
                        String request = userinput;
                        System.out.println("What topic are you interested in?");
                        String topic_name = sc.nextLine();
                        os.writeUTF(request);
                        os.flush();
                        os.writeUTF(topic_name);
                        os.flush();
                        os.writeObject(this);
                        os.flush();
                        os.writeInt(chunks_to_be_received.size());
                        os.flush();
                    } else if (userinput.equals("disconnect")) {
                        String request = userinput;
                        System.out.println("Disconnect from what topic?");
                        String topic_name = sc.nextLine();
                        os.writeUTF(request);
                        os.flush();
                        os.writeUTF(topic_name);
                        os.flush();
                    } else if (userinput.equals("show conversation data")){// show conversation data

                    } else if(userinput.equals("quit")){
                        os.close();
                        is.close();
                        killclient();
                    }
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
                killclient();
            } catch (IOException e) {
                e.printStackTrace();
                killclient();
            }
        }
    }


    public void disconnect(String topic){

    }

    public void connect(){

    }

    public Broker findBroker(Topic topic){



        int identifier = SHA1.hextoInt(topic.getName(),3);
        for(Broker val:BrokerList){
            if(val.getId() == identifier){
                return val;
            }
        }
        return null;

    }
    public void register(Topic topic){



        socket = connect(BrokerList.get(0).IPaddress,BrokerList.get(0).port);

    }
    public void showConversationData(String topic, int value){

    }
}
© 2022 GitHub, Inc.
        Terms
        Privacy
        Security
        Status
        Docs
        Contact GitHub
        Pricing
        API
        Training
        Blog
        About
        Loading complete