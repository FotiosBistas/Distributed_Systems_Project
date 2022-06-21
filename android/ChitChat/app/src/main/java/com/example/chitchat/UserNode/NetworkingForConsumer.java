
package com.example.chitchat.UserNode;

import android.app.Activity;
import android.app.WallpaperColors;
import android.os.AsyncTask;
import android.text.PrecomputedText;
import android.view.View;
import android.widget.Toast;

import Logging.ConsoleColors;

import com.example.chitchat.Connect_Activity;
import com.example.chitchat.NetworkUtilities.GeneralUtils;
import com.example.chitchat.NetworkUtilities.UserNodeUtils;
import com.example.chitchat.Tools.Messages;
import com.example.chitchat.Tools.Tuple;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ref.WeakReference;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;

public class NetworkingForConsumer extends AsyncTask<Integer,Void,Integer>{

    private WeakReference<Activity> weakReference;
    //default broker ip address to connect to
    private final String default_ip_address = "192.168.1.5";
    //port that listens to consumer services for that broker
    private final int default_port = 1234;

    private String topic_name;
    private UserNode cons;

    private Socket request_socket;
    private ObjectInputStream localinputStream;
    private ObjectOutputStream localoutputStream;

    public NetworkingForConsumer(Activity activity,UserNode cons){
        this.weakReference = new WeakReference<>(activity);
        this.cons = cons;
    }

    public NetworkingForConsumer(WeakReference<Activity> weakReference,String topic_name,UserNode cons){
        this.weakReference = weakReference;
        this.topic_name = topic_name;
        this.cons = cons;
    }

    //Copy Constructor
    public NetworkingForConsumer(Activity activity,String topic_name,UserNode cons){
        this.weakReference = new WeakReference<>(activity);
        this.topic_name = topic_name;
        this.cons = cons;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Activity activity = weakReference.get();
        if (activity == null || activity.isFinishing()) {
            return;
        }
        if(activity instanceof Connect_Activity) {
            Connect_Activity connect_activity = (Connect_Activity) activity;
            connect_activity.progressBar.setVisibility(View.VISIBLE);
        }
    }


    /**
     * integer is the operation type we want to perform
     * @param integers
     * @return
     */
    @Override
    protected Integer doInBackground(Integer... integers) {
        System.out.println(Arrays.toString(integers));
        Integer index;
        Integer success;
        try{
            request_socket = new Socket(default_ip_address,default_port);
            localinputStream = new ObjectInputStream(request_socket.getInputStream());
            localoutputStream = new ObjectOutputStream(request_socket.getOutputStream());
            for (int i = 0; i < integers.length ; i++){
                if(isCancelled()){
                    break;
                }
                int operation = integers[i];
                switch (operation){
                    case 1:
                        if(UserNodeUtils.getBrokerList(localoutputStream) == null){
                            cancel(true);
                            break;
                        };
                        if(UserNodeUtils.receiveBrokerList(localinputStream,localoutputStream,request_socket,cons) == null){
                            cancel(true);
                            break;
                        }
                        if(GeneralUtils.FinishedOperation(localoutputStream) == null){
                            cancel(true);
                            break;
                        }
                        break;
                    case 2:
                        if(UserNodeUtils.getIDList(localoutputStream) == null){
                            cancel(true);
                            break;
                        }
                        if(UserNodeUtils.receiveIDList(localinputStream,localoutputStream,request_socket,cons) == null){
                            cancel(true);
                            break;
                        }
                        if(GeneralUtils.FinishedOperation(localoutputStream) == null){
                            cancel(true);
                            break;
                        }
                        break;
                    case 3:
                        if(UserNodeUtils.sendNickname(localoutputStream,cons) == null){
                            cancel(true);
                            break;
                        }
                        if(GeneralUtils.FinishedOperation(localoutputStream) == null){
                            cancel(true);
                            break;
                        }
                        System.out.println("I'm the client: " + cons.getName() + " and i have connected to the server");
                        break;
                    case 4:
                        if((index = UserNodeUtils.register(localinputStream,localoutputStream,request_socket,topic_name,cons)) == null){
                            shutdownConnection();
                            break;
                        }else if(index == -1) {
                            success = GeneralUtils.waitForNodePrompt(localinputStream, request_socket);
                            if (success == null) {
                                cancel(true);
                            } else if (success == Messages.NO_SUCH_TOPIC.ordinal()) {
                                System.out.println(ConsoleColors.RED + "Broker couldn't subscribe you to the topic so it will create a new one" + ConsoleColors.RESET);
                                cons.addNewSubscription(topic_name);
                                if (GeneralUtils.FinishedOperation(localoutputStream) == null) {
                                    shutdownConnection();
                                    break;
                                }
                            } else if (success == Messages.FINISHED_OPERATION.ordinal()) {
                                cons.addNewSubscription(topic_name);
                                if (GeneralUtils.FinishedOperation(localoutputStream) == null) {
                                    shutdownConnection();
                                    break;
                                }
                            }
                        }
                        break;
                    case 5:
                        if((index = UserNodeUtils.unsubscribe(localinputStream,localoutputStream,request_socket,topic_name,this.cons)) == null){
                            cancel(true);
                        }else if(index == -1) {
                            success = GeneralUtils.waitForNodePrompt(localinputStream, request_socket);
                            if (success == null) {
                                cancel(true);
                            } else if (success == Messages.NO_SUCH_TOPIC.ordinal()) {
                                System.out.println(ConsoleColors.RED + "Broker couldn't unsubscribe you from the topic" + ConsoleColors.RESET);
                                cancel(true);
                            } else if (success == Messages.FINISHED_OPERATION.ordinal()) {
                                cons.removeSubscription(topic_name);
                                if (GeneralUtils.FinishedOperation(localoutputStream) == null) {
                                    shutdownConnection();
                                    break;
                                }
                            }
                            break;
                        }else{
                            Tuple<String, int[]> brk = cons.getBrokerList().get(index);
                            startNewConnection(brk,operation);
                            break;
                        }
                    case 6:
                        if((index = UserNodeUtils.receiveConversationData(localoutputStream,localinputStream,request_socket,topic_name)) == null){
                            cancel(true);
                            continue;
                        }else if(index == -1){
                            if(GeneralUtils.FinishedOperation(localoutputStream) == null){
                                cancel(true);
                                continue;
                            }
                            break;
                        }else{
                            Tuple<String, int[]> brk = cons.getBrokerList().get(index);
                            startNewConnection(brk,operation);
                            break;
                        }
                    default:
                        System.out.println("Invalid Request... Try again");
                        shutdownConnection();
                }
            }
        } catch (IOException e) {
            cancel(true);
            e.printStackTrace();
        }

        return null;
    }

    private void startNewConnection(Tuple<String,int[]> new_broker, int i) {
        String IP = new_broker.getValue1();
        System.out.println("New connection IP: " + IP);
        //port for connecting to broker for consumer traffic
        int port = new_broker.getValue2()[0];
        System.out.println("New broker port: " + port);
        NetworkingForConsumer new_connection = new NetworkingForConsumer(weakReference,topic_name,cons);
    }


    @Override
    protected void onPostExecute(Integer integer) {
        super.onPostExecute(integer);
    }

    /**
     * If an exception is caught the async task cancels execution
     * @param integer
     */
    @Override
    protected void onCancelled(Integer integer) {
        shutdownConnection();
    }

    private void shutdownConnection(){
        try{
            if(request_socket != null){
                request_socket.close();
            }
            if(localinputStream != null){
                localinputStream.close();
            }
            if(localoutputStream != null){
                localoutputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
