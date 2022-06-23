

package com.example.chitchat.UserNode;


import android.app.Activity;
import android.os.AsyncTask;
import android.os.Build;

import androidx.annotation.RequiresApi;

import Logging.ConsoleColors;

import com.example.chitchat.Activities.Message_List_Activity;
import com.example.chitchat.NetworkUtilities.UserNodeUtils;
import com.example.chitchat.Tools.Tuple;

import java.io.*;
import java.lang.ref.WeakReference;
import java.net.Socket;

public class NetworkingForPublisher extends AsyncTask<Integer,Void,Integer> {

    private WeakReference<Activity> weakReference;
    //default broker ip address to connect to
    private final String default_ip_address = "192.168.1.5";
    //port that listens to consumer services for that broker
    private final int default_port = 1234;

    private String topic_name;
    private Android_User_Node pub;

    private Socket connection;
    private ObjectInputStream localinputStream;
    private ObjectOutputStream localoutputStream;

    private int push_type;
    private String contents_or_file_name;

    /**
     * we need a weak reference because the Networking For consumer class will be called from multiple activities
     *
     * @param weakReference The activity that has initiated the async task
     */
    public void setWeakReference(WeakReference<Activity> weakReference) {
        this.weakReference = weakReference;
    }


    public NetworkingForPublisher(WeakReference<Activity> weakReference, String topic_name, Android_User_Node pub, String contents_or_file_name) {
        this.weakReference = weakReference;
        this.topic_name = topic_name;
        this.pub = pub;
        this.contents_or_file_name = contents_or_file_name;
    }

    public NetworkingForPublisher(Activity activity, String topic_name, Android_User_Node pub, String contents_or_file_name) {
        this.weakReference = new WeakReference<>(activity);
        this.topic_name = topic_name;
        this.pub = pub;
        this.contents_or_file_name = contents_or_file_name;
    }


    /**
     * initiates new async task meaning a new connection with the broker that is responsible for the topic name
     *
     * @param new_broker a Tools.tuple containing the ip of the broker and the port number to connect to. THe consumer ports are in the second place in the array
     */
    private void startNewConnection(Tuple<String, int[]> new_broker, int operation, String contents_or_file_name) {
        String IP = new_broker.getValue1();
        System.out.println("New connection IP: " + IP);
        //port for publisher services
        int port = new_broker.getValue2()[1];
        System.out.println("New broker port: " + port);
        NetworkingForPublisher new_connection = new NetworkingForPublisher(weakReference, topic_name, pub, contents_or_file_name);
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
    protected void onPreExecute() {
        super.onPreExecute();

        Activity activity = weakReference.get();
        if (activity == null || activity.isFinishing()) {
            return;
        }

        if(activity instanceof Message_List_Activity){
            //TODO implement progress bar for each case
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected Integer doInBackground(Integer... integers) {
        Integer index;
        try {
            connection = new Socket(default_ip_address, default_port);
            localinputStream = new ObjectInputStream(connection.getInputStream());
            localoutputStream = new ObjectOutputStream(connection.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
            cancel(true);
        }
        if ((index = UserNodeUtils.push(localinputStream, localoutputStream, connection, topic_name, pub, push_type, contents_or_file_name)) == null) {
            System.out.println(ConsoleColors.RED + "Error while trying to push" + ConsoleColors.RESET);
            cancel(true);
        } else if (index == -1) {
            System.out.println("Successful push");
        } else {
            Tuple<String, int[]> brk = pub.getBrokerList().get(index);
            startNewConnection(brk, push_type, contents_or_file_name);
            cancel(true);

        }
        return -1;
    }

    @Override
    protected void onPostExecute(Integer integer) {
        super.onPostExecute(integer);

        Activity activity = weakReference.get();
        if (activity == null || activity.isFinishing()) {
            return;
        }
        if(activity instanceof Message_List_Activity) {
            Message_List_Activity message_list_activity = (Message_List_Activity) activity;
            //if push text message
            if (push_type == 0) {
                message_list_activity.getMessage_list_adapter().addMessage(pub.getTemp_message());
            }else if(push_type == 1){
                message_list_activity.getMessage_list_adapter().addMessage(pub.getTemp_multimedia_file_android());
            }else if(push_type == 2){
                message_list_activity.getMessage_list_adapter().addMessage(pub.getTemp_story());
            }
        }
    }

    @Override
    protected void onCancelled(Integer integer) {
        super.onCancelled(integer);
        shutdownConnection();
    }


}

