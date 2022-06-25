

package com.example.chitchat.UserNode;


import android.app.Activity;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.view.View;

import androidx.annotation.RequiresApi;

import Logging.ConsoleColors;

import com.example.chitchat.Activities.Message_List_Activity;
import com.example.chitchat.NetworkUtilities.UserNodeUtils;
import com.example.chitchat.Tools.Multimedia_File_Android;
import com.example.chitchat.Tools.Tuple;

import java.io.*;
import java.lang.ref.WeakReference;
import java.net.Socket;

public class NetworkingForPublisher extends AsyncTask<Integer, Void, Void> {

    private WeakReference<Activity> weakReference;
    //default broker ip address to connect to
    private String default_ip_address = "192.168.1.5";
    //port that listens to consumer services for that broker
    private int default_port = 1235;

    private String topic_name;
    private Android_User_Node pub;

    private Socket connection;
    private ObjectInputStream localinputStream;
    private ObjectOutputStream localoutputStream;
    private Multimedia_File_Android multimedia_file_android;

    private int push_type;
    private String contents;

    public NetworkingForPublisher(Message_List_Activity message_list_activity, String topic_name, Android_User_Node androidUserNode, Multimedia_File_Android multimedia_file_android) {
        this.weakReference = new WeakReference<>(message_list_activity);
        this.topic_name = topic_name;
        this.pub = androidUserNode;
        this.multimedia_file_android = multimedia_file_android;
    }

    public NetworkingForPublisher(WeakReference<Activity> weakReference, String topic_name, Android_User_Node androidUserNode, Multimedia_File_Android multimedia_file_android) {
        this.weakReference = weakReference;
        this.topic_name = topic_name;
        this.pub = androidUserNode;
        this.multimedia_file_android = multimedia_file_android;
    }

    /**
     * we need a weak reference because the Networking For consumer class will be called from multiple activities
     *
     * @param weakReference The activity that has initiated the async task
     */
    public void setWeakReference(WeakReference<Activity> weakReference) {
        this.weakReference = weakReference;
    }

    public void setDefault_ip_address(String default_ip_address) {
        this.default_ip_address = default_ip_address;
    }

    public void setDefault_port(int default_port) {
        this.default_port = default_port;
    }

    public NetworkingForPublisher(WeakReference<Activity> weakReference, String topic_name, Android_User_Node pub, String contents) {
        this.weakReference = weakReference;
        this.topic_name = topic_name;
        this.pub = pub;
        this.contents = contents;
    }

    public NetworkingForPublisher(Activity activity, String topic_name, Android_User_Node pub, String contents) {
        this.weakReference = new WeakReference<>(activity);
        this.topic_name = topic_name;
        this.pub = pub;
        this.contents = contents;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Activity activity = weakReference.get();
        if (activity == null || activity.isFinishing()) {
            System.out.println("Ending async task");
            return;
        }

        if(activity instanceof Message_List_Activity){
            System.out.println("Instance of message list activity");
            Message_List_Activity message_list_activity = (Message_List_Activity) activity;
            message_list_activity.getProgressBar().setVisibility(View.VISIBLE);

        }
        Log.e("NetworkingForPublisher","onPreExecute");


    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected Void doInBackground(Integer... integers) {
        Integer index;
        try {
            Log.v("NetworkingForPublisher","doInBackground");
            connection = new Socket(default_ip_address,default_port);
            localoutputStream = new ObjectOutputStream(connection.getOutputStream());
            localinputStream = new ObjectInputStream(connection.getInputStream());
            this.push_type = integers[0];
            if(push_type == 0) {
                if ((index = UserNodeUtils.push(localinputStream, localoutputStream, connection, topic_name, pub, contents)) == null) {
                    System.out.println(ConsoleColors.RED + "Error while trying to push" + ConsoleColors.RESET);
                    cancel(true);
                } else if (index == -1) {
                    System.out.println("Successful push");
                } else {
                    Tuple<String, int[]> brk = pub.getBrokerList().get(index);
                    startNewConnection(brk, push_type, contents);
                    cancel(true);

                }
            }else if(push_type == 1 || push_type == 2){
                if ((index = UserNodeUtils.push(localinputStream, localoutputStream, connection, topic_name, pub, multimedia_file_android)) == null) {
                    System.out.println(ConsoleColors.RED + "Error while trying to push" + ConsoleColors.RESET);
                    cancel(true);
                } else if (index == -1) {
                    System.out.println("Successful push");
                } else {
                    Tuple<String, int[]> brk = pub.getBrokerList().get(index);
                    startNewConnection(brk, push_type, contents);
                    cancel(true);

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            cancel(true);
        }
        return null;
    }

    /**
     * initiates new async task meaning a new connection with the broker that is responsible for the topic name
     *
     * @param new_broker a Tools.tuple containing the ip of the broker and the port number to connect to. THe consumer ports are in the second place in the array
     */
    private void startNewConnection(Tuple<String, int[]> new_broker, int operation, String contents) {
        String IP = new_broker.getValue1();
        System.out.println("New connection IP: " + IP);
        //port for publisher services
        int port = new_broker.getValue2()[1];
        System.out.println("New broker port: " + port);
        NetworkingForPublisher new_connection;
        if(multimedia_file_android == null) {
            new_connection = new NetworkingForPublisher(weakReference, topic_name, pub, contents);
        }else{
            new_connection = new NetworkingForPublisher(weakReference,topic_name,pub,multimedia_file_android);
        }
        new_connection.setDefault_ip_address(IP);
        new_connection.setDefault_port(port);
        new_connection.execute(operation);
    }


    /**
     * Terminates the local socket along with it's corresponding input and output streams.It throws a IO exception if something goes wrong.
     */
    private void shutdownConnection() {
        System.out.println("Terminating publisher: " + pub.getName());
        try {
            if (connection != null) {
                connection.close();
            }

            if (localoutputStream != null) {
                localoutputStream.close();
            }
            if (localinputStream != null) {
                localinputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    @Override
    protected void onPostExecute(Void v) {
        super.onPostExecute(v);
        Log.v("NetworkingForConsumer","onPostExecute");
        Activity activity = weakReference.get();
        if (activity == null || activity.isFinishing()) {
            return;
        }
        if(activity instanceof Message_List_Activity) {
            Message_List_Activity message_list_activity = (Message_List_Activity) activity;
            message_list_activity.getProgressBar().setVisibility(View.INVISIBLE);
            //if push text message
            if (push_type == 0) {
                message_list_activity.getMessage_list_adapter().addMessage(pub.getTemp_message());
            }else if(push_type == 1){
                message_list_activity.getMessage_list_adapter().addMessage(pub.getTemp_multimedia_file_android());
            }else if(push_type == 2){
                message_list_activity.getMessage_list_adapter().addMessage(pub.getTemp_story());
            }
        }
        shutdownConnection();
    }

    @Override
    protected void onCancelled(Void v) {
        super.onCancelled(v);
        shutdownConnection();
    }


}

