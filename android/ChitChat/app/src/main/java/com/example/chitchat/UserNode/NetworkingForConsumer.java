
package com.example.chitchat.UserNode;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import Logging.ConsoleColors;

import com.example.chitchat.Activities.Central_Screen_Activity;
import com.example.chitchat.Activities.Message_List_Activity;
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

public class NetworkingForConsumer extends AsyncTask<Integer,Void,Integer>{

    private WeakReference<Activity> weakReference;
    //default broker ip address to connect to
    private String default_ip_address = "192.168.1.5";
    //port that listens to consumer services for that broker
    private int default_port = 1234;

    private String topic_name;
    private Android_User_Node cons;
    private boolean start_new_connection = false;
    private Tuple<String,int[]> new_broker;

    private Socket request_socket;
    private ObjectInputStream localinputStream;
    private ObjectOutputStream localoutputStream;

    private int request_type;

    public NetworkingForConsumer(Activity activity, Android_User_Node cons){
        this.weakReference = new WeakReference<>(activity);
        this.cons = cons;
    }

    //Copy Constructor
    public NetworkingForConsumer(WeakReference<Activity> weakReference, String topic_name, Android_User_Node cons){
        this.weakReference = weakReference;
        this.topic_name = topic_name;
        this.cons = cons;
    }

    public NetworkingForConsumer(Activity activity, String topic_name, Android_User_Node cons){
        this.weakReference = new WeakReference<>(activity);
        this.topic_name = topic_name;
        this.cons = cons;
    }

    public void setDefault_ip_address(String default_ip_address) {
        this.default_ip_address = default_ip_address;
    }

    public void setDefault_port(int default_port) {
        this.default_port = default_port;
    }

    /**
     * we need a weak reference because the Networking For consumer class will be called from multiple activities
     * @param weakReference The activity that has initiated the async task
     */
    public void setWeakReference(WeakReference<Activity> weakReference) {
        this.weakReference = weakReference;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();


        Activity activity = weakReference.get();
        if (activity == null || activity.isFinishing()) {
            return;
        }
        if(activity instanceof Central_Screen_Activity) {
            Central_Screen_Activity central_screen_activity = (Central_Screen_Activity) activity;
            central_screen_activity.getProgressBar().setVisibility(View.VISIBLE);
        }
        Log.e("NetworkingForConsumer","onPreExecute");
    }


    /**
     * integers is the operation type we want to perform
     * @param integers accept a single request type or an array of request types
     * @return
     */
    @Override
    protected Integer doInBackground(Integer... integers) {
        Integer index;
        Integer success;
        try{
            request_socket = new Socket(default_ip_address,default_port);
            localinputStream = new ObjectInputStream(request_socket.getInputStream());
            localoutputStream = new ObjectOutputStream(request_socket.getOutputStream());
            this.request_type = integers[0];
            for (int i = 0; i < integers.length ; i++){
                Log.v("NetworkingForConsumer","doInBackground");
                if(isCancelled()){
                    break;
                }
                int operation = integers[i];
                switch (operation){
                    case 1:
                        if(UserNodeUtils.getBrokerList(localoutputStream) == null){
                            System.out.println("Error occured");
                            cancel(true);
                            continue;
                        };
                        if(UserNodeUtils.receiveBrokerList(localinputStream,localoutputStream,request_socket,cons) == null){
                            System.out.println("Error occured");
                            cancel(true);
                            continue;
                        }
                        if(GeneralUtils.FinishedOperation(localoutputStream) == null){
                            System.out.println("Error occured");
                            cancel(true);
                            continue;

                        }
                        break;
                    case 2:
                        if(UserNodeUtils.getIDList(localoutputStream) == null){
                            cancel(true);
                            continue;

                        }
                        if(UserNodeUtils.receiveIDList(localinputStream,localoutputStream,request_socket,cons) == null){
                            cancel(true);
                            continue;

                        }
                        if(GeneralUtils.FinishedOperation(localoutputStream) == null){
                            cancel(true);
                            continue;

                        }
                        break;
                    case 3:
                        if(UserNodeUtils.sendNickname(localoutputStream,cons) == null){
                            cancel(true);
                            continue;

                        }
                        if(GeneralUtils.FinishedOperation(localoutputStream) == null){
                            cancel(true);
                            continue;

                        }
                        System.out.println("I'm the client: " + cons.getName() + " and i have connected to the server");
                        break;
                    case 4:
                        if((index = UserNodeUtils.register(localinputStream,localoutputStream,request_socket,topic_name,cons)) == null){
                            cancel(true);
                            continue;
                        }else if(index == -1) {
                            success = GeneralUtils.waitForNodePrompt(localinputStream, request_socket);
                            if (success == null) {
                                cancel(true);
                                continue;
                            } else if (success == Messages.NO_SUCH_TOPIC.ordinal()) {
                                System.out.println(ConsoleColors.RED + "Broker couldn't subscribe you to the topic so it will create a new one" + ConsoleColors.RESET);
                                cons.addNewSubscription(topic_name);
                                if (GeneralUtils.FinishedOperation(localoutputStream) == null) {
                                    cancel(true);
                                    continue;

                                }
                            } else if (success == Messages.FINISHED_OPERATION.ordinal()) {
                                cons.addNewSubscription(topic_name);
                                if (GeneralUtils.FinishedOperation(localoutputStream) == null) {
                                    cancel(true);
                                    continue;

                                }
                            }
                        }else{
                            Tuple<String, int[]> brk = cons.getBrokerList().get(index);
                            this.new_broker = brk;
                            this.start_new_connection = true;
                            cancel(true);
                            continue;
                        }
                        break;
                    case 5:
                        if((index = UserNodeUtils.unsubscribe(localinputStream,localoutputStream,request_socket,topic_name,this.cons)) == null){
                            cancel(true);
                            continue;
                        }else if(index == -1) {
                            success = GeneralUtils.waitForNodePrompt(localinputStream, request_socket);
                            if (success == null) {
                                cancel(true);
                                continue;
                            } else if (success == Messages.NO_SUCH_TOPIC.ordinal()) {
                                System.out.println(ConsoleColors.RED + "Broker couldn't unsubscribe you from the topic" + ConsoleColors.RESET);
                                cancel(true);
                                continue;
                            } else if (success == Messages.FINISHED_OPERATION.ordinal()) {
                                cons.removeSubscription(topic_name);
                                if (GeneralUtils.FinishedOperation(localoutputStream) == null) {
                                   cancel(true);
                                   continue;
                                }
                            }
                            break;
                        }else{
                            Tuple<String, int[]> brk = cons.getBrokerList().get(index);
                            this.new_broker = brk;
                            this.start_new_connection = true;
                            cancel(true);
                            continue;
                        }
                    case 6:
                        if((index = UserNodeUtils.receiveConversationData(localoutputStream,localinputStream,request_socket,topic_name,cons)) == null){
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
                            this.new_broker = brk;
                            this.start_new_connection = true;
                            cancel(true);
                            continue;
                        }
                    default:
                        System.out.println("Invalid Request... Try again");
                        cancel(true);
                        continue;
                }
            }
        }catch (ConnectException connectException){
            cancel(true);
            connectException.printStackTrace();
        } catch (IOException e) {
            cancel(true);
            e.printStackTrace();
        }

        return null;
    }

    /**
     * initiates new async task meaning a new connection with the broker that is responsible for the topic name
     * @param new_broker a Tools.tuple containing the ip of the broker and the port number to connect to. THe consumer ports are in the second place in the array
     * @param i
     */
    private void startNewConnection(Tuple<String,int[]> new_broker, int i) {
        String IP = new_broker.getValue1();
        System.out.println("New connection IP: " + IP);
        //port for connecting to broker for consumer traffic
        int port = new_broker.getValue2()[0];
        System.out.println("New broker port: " + port);
        NetworkingForConsumer new_connection = new NetworkingForConsumer(weakReference,topic_name,cons);
        new_connection.setDefault_ip_address(IP);
        new_connection.setDefault_port(port);
        new_connection.execute(i);
    }


    @Override
    protected void onPostExecute(Integer integer) {
        super.onPostExecute(integer);
        Log.v("NetworkingForConsumer","onPostExecute");
        Activity activity = weakReference.get();
        if (activity == null || activity.isFinishing()) {
            return;
        }
        if (request_type == 1 || request_type == 2 || request_type == 3) {
            if (activity instanceof Central_Screen_Activity) {
                Central_Screen_Activity central_screen_activity = (Central_Screen_Activity) activity;
                Toast.makeText(activity, "Received broker and id list successfully", Toast.LENGTH_SHORT).show();
                central_screen_activity.getProgressBar().setVisibility(View.INVISIBLE);
            }
        } else if (request_type == 4) {
            if (activity instanceof Central_Screen_Activity) {
                Central_Screen_Activity central_screen_activity = (Central_Screen_Activity) activity;
                Toast.makeText(activity, "Subscribed to topic: " + topic_name + " successfully", Toast.LENGTH_SHORT).show();
                central_screen_activity.getProgressBar().setVisibility(View.INVISIBLE);
                central_screen_activity.getTopicsAdapter().addTopic(topic_name);
            }
        } else if (request_type == 5) {
            if (activity instanceof Central_Screen_Activity) {
                Central_Screen_Activity central_screen_activity = (Central_Screen_Activity) activity;
                Toast.makeText(activity, "Unsubscribed from topic: " + topic_name + " successfully", Toast.LENGTH_SHORT).show();
                central_screen_activity.getProgressBar().setVisibility(View.INVISIBLE);
                central_screen_activity.getTopicsAdapter().removeTopic(topic_name);
            }
        } else if (request_type == 6) {
            if (activity instanceof Message_List_Activity) {
                Message_List_Activity message_list_activity = (Message_List_Activity) activity;
                message_list_activity.getMessage_list_adapter().addMessages(cons.getTemp_message_list());
            }
        }

        shutdownConnection();
    }

    /**
     * If an exception is caught the async task cancels execution
     * @param integer
     */
    @Override
    protected void onCancelled(Integer integer) {
        super.onCancelled(integer);
        if(start_new_connection){
            System.out.println("Starting new connection");
            startNewConnection(new_broker,request_type);
        }
        shutdownConnection();
    }

    private void shutdownConnection(){
        System.out.println("Shutting down connection");
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
