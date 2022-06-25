package com.example.chitchat.UserNode;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import com.example.chitchat.Activities.Message_List_Activity;
import com.example.chitchat.NetworkUtilities.UserNodeUtils;
import com.example.chitchat.Tools.Tuple;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ref.WeakReference;
import java.net.Socket;

public class Pull_request extends AsyncTask<Void,Void,Void> {

    private WeakReference<Activity> weakReference;
    //default broker ip address to connect to
    private String default_ip_address = "192.168.1.5";
    //port that listens to consumer services for that broker
    private int default_port = 1234;

    public void setDefault_ip_address(String default_ip_address) {
        this.default_ip_address = default_ip_address;
    }

    public void setDefault_port(int default_port) {
        this.default_port = default_port;
    }

    private String topic_name;
    private Android_User_Node pull_requester;


    private Socket connection;
    private ObjectInputStream localinputStream;
    private ObjectOutputStream localoutputStream;

    public Pull_request(Activity activity,String topic_name,Android_User_Node pull_requester){
        weakReference = new WeakReference<>(activity);
        this.topic_name = topic_name;
        this.pull_requester = pull_requester;
    }

    //copy constructor
    public Pull_request(WeakReference<Activity> weakReference,String topic_name,Android_User_Node pull_requester){
        this.weakReference = weakReference;
        this.topic_name = topic_name;
        this.pull_requester = pull_requester;
    }

    private void startNewConnection(Tuple<String,int[]> new_broker){
        String IP = new_broker.getValue1();
        System.out.println("New broker IP: " + IP);
        Integer port = new_broker.getValue2()[0];
        System.out.println("New broker port: " + port);
        Pull_request pull_request = new Pull_request(weakReference,topic_name,pull_requester);
        pull_request.setDefault_ip_address(IP);
        pull_request.setDefault_port(port);
        pull_request.execute();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
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
        Log.e("PUll request","onPreExecute");
    }

    @Override
    protected Void doInBackground(Void... voids) {

        Integer index;
        try {
            Log.v("NetworkingForPublisher","doInBackground");
            connection = new Socket(default_ip_address,default_port);
            localoutputStream = new ObjectOutputStream(connection.getOutputStream());
            localinputStream = new ObjectInputStream(connection.getInputStream());
            if ((index = UserNodeUtils.pull(localoutputStream, localinputStream, connection, topic_name, pull_requester)) == null) {
                System.out.println(Logging.ConsoleColors.RED + "Error while trying to push" + Logging.ConsoleColors.RESET);
                cancel(true);
            } else if (index == -1) {
                System.out.println("Successful pull");
            } else {
                Tuple<String, int[]> brk = pull_requester.getBrokerList().get(index);
                startNewConnection(brk);
                cancel(true);

            }
        } catch (IOException e) {
            e.printStackTrace();
            cancel(true);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void unused) {
        super.onPostExecute(unused);
        Log.v("NetworkingForConsumer","onPostExecute");
        Activity activity = weakReference.get();
        if (activity == null || activity.isFinishing()) {
            return;
        }
        if(activity instanceof Message_List_Activity) {
            Message_List_Activity message_list_activity = (Message_List_Activity) activity;
            message_list_activity.getProgressBar().setVisibility(View.INVISIBLE);
            //if push text message
            message_list_activity.getMessage_list_adapter().addMessages(pull_requester.getTemp_message_list());
        }
        shutdownConnection();
    }

    /**
     * Terminates the local socket along with it's corresponding input and output streams.It throws a IO exception if something goes wrong.
     */
    private void shutdownConnection() {
        System.out.println("Terminating pull request: " + pull_requester.getName());
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
}
