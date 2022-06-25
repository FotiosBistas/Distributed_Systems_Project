package com.example.chitchat.NetworkUtilities;





import com.example.chitchat.Logging.ConsoleColors;
import com.example.chitchat.Tools.Messages;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

/**
 * This is a class that contains static network methods that are used by the user node and the brokers.
 * These are basic operations that are required by both the brokers and the users.
 */
public class GeneralUtils {


    /**
     * Reads data into the parameter buffer. The number of data read is the actual size parameter.
     * @param localinputStream  Accepts the local input stream.
     * @param buffer Accepts a byte buffer and writes the data from the input stream to the buffer.
     * @param offset Accepts the offset start reading the local input stream.
     * @param actual_size Accepts the amount of data to be read to the buffer. The actual size name here refers to actual size of the chunk being sent.
     * @return Returns a byte array. This byte array is the array that the data will be read to. Remember because passes by value when you return the byte array assign it to the local byte array. If an error occurs it returns null.
     */
    public static byte[] readBuffer(ObjectInputStream localinputStream, byte[] buffer, int offset, Integer actual_size){
        try {
            localinputStream.readFully(buffer,0,actual_size);
            return buffer;
        } catch (SocketException socketException) {
            System.out.println( ConsoleColors.RED + "Socket error in read buffer..." + ConsoleColors.RESET);
            return null;
        } catch (IOException e) {
            System.out.println( ConsoleColors.RED + "Error in read buffer..." + ConsoleColors.RESET);
            return null;
        }
    }

    /**
     * Reads a UTF string from the input stream using the inputStream.readUTF() method.
     * @param localinputStream  accepts the local input stream.
     * @param socket  accepts the corresponding socket of the streams.
     * @return returns the input read from the local input stream. If it catches an exception it returns null as an error string.
     */
    public static String readUTFString(ObjectInputStream localinputStream, Socket socket) {
        try {
            //System.out.println(ConsoleColors.PURPLE + "Waiting to read UTF type" + ConsoleColors.RESET);
            String message = localinputStream.readUTF();
            //System.out.println(ConsoleColors.GREEN + "Received message: " + message + " from node: " + socket.getInetAddress() + ConsoleColors.RESET);
            return message;
        } catch (SocketException socketException) {
            System.out.println( ConsoleColors.RED + "Socket error in read UTF string..." + ConsoleColors.RESET);
            socketException.printStackTrace();
            return null;
        } catch (IOException e) {
            System.out.println( ConsoleColors.RED + "Error in read UTF string..." + ConsoleColors.RESET);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Waits until an int input is read by the input stream using the inputStream.readInt() method.
     * @param localinputStream  accepts the local input stream.
     * @param socket accepts the corresponding socket of the streams.
     * @return returns the input read from the local input stream. If it catches an exception it returns null.
     */
    public static Integer waitForNodePrompt(ObjectInputStream localinputStream, Socket socket) {
        try {
            //System.out.println(ConsoleColors.PURPLE + "Waiting for node prompt (waiting for integer type)" + ConsoleColors.RESET);
            int message = localinputStream.readInt();
            //System.out.println(ConsoleColors.GREEN + "Received message: " + message + " from node: " + socket.getInetAddress() + ConsoleColors.GREEN);
            return message;
        } catch (SocketException socketException) {
            System.out.println( "\033[1;31m" + "Socket error in wait for node prompt..." + ConsoleColors.RESET);
            socketException.printStackTrace();
            return null;
        } catch (IOException e) {
            System.out.println( ConsoleColors.RED + "Error in wait for node prompt..." + ConsoleColors.RESET);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Waits until an long input is read by the input stream using the inputStream.readInt() method.
     * @param localinputStream  accepts the local input stream.
     * @param socket accepts the corresponding socket of the streams.
     * @return returns the input read from the local input stream. If it catches an exception it returns null.
     */
    public static Long readLong(ObjectInputStream localinputStream, Socket socket) {
        try {
            //System.out.println(ConsoleColors.PURPLE + "Waiting for node prompt (waiting to read long type)" + ConsoleColors.RESET);
            Long message = localinputStream.readLong();
            //System.out.println(ConsoleColors.GREEN + "Received message: " + message + " from node: " + socket.getInetAddress() + ConsoleColors.RESET);
            return message;
        } catch (SocketException socketException) {
            System.out.println( ConsoleColors.RED + "Socket error in wait for node prompt..." + ConsoleColors.RESET);
            socketException.printStackTrace();
            return null;
        } catch (IOException e) {
            System.out.println( ConsoleColors.RED + "Error in wait for node prompt..." + ConsoleColors.RESET);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Waits until an object input is read from the input stream using the inputStream.readObject() method.
     * @param localinputStream Accepts the local input stream.
     * @param socket Accepts the local socket.
     * @return Returns the object read if everything goes well. If an error occurs it returns nulls.
     */
    public static Object readObject(ObjectInputStream localinputStream,Socket socket) {
        try {
            //System.out.println(ConsoleColors.PURPLE + "Waiting to receive object from input stream" + ConsoleColors.RESET);
            Object message = localinputStream.readObject();
            //System.out.println(ConsoleColors.GREEN + "Received message: " + message + " from node: " + socket.getInetAddress() + ConsoleColors.RESET);
            return message;
        }catch (NotSerializableException notSerializableException){
            System.out.println( ConsoleColors.RED + "Not serializable error in read object..." + ConsoleColors.RESET);
            return null;
        }catch (SocketException socketException) {
            System.out.println( ConsoleColors.RED + "Socket error in read object..." + ConsoleColors.RESET);
            return null;
        } catch (IOException | ClassNotFoundException e) {
            System.out.println( ConsoleColors.RED + "Error in read object..." + ConsoleColors.RESET);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Sends a string object through the output stream.
     * @param message Accepts any int and sends it as a message.
     * @param localoutputStream Accepts the local output stream.
     * @return Returns -1 if everything goes well. Returns null if an error occurs.
     */
    public static Integer sendMessage(String message, ObjectOutputStream localoutputStream) {
        try {
            //System.out.println( ConsoleColors.GREEN + "Sending Message: " + message + ConsoleColors.RESET);
            localoutputStream.writeUTF(message);
            localoutputStream.flush();
            return -1;
        } catch (SocketException socketException) {
            System.out.println( ConsoleColors.RED + "Socket error in send message of string type..." + ConsoleColors.RESET);
            return null;
        } catch (IOException e) {
            System.out.println( ConsoleColors.RED + "Error in send message of string type..." + ConsoleColors.RESET);
            return null;
        }
    }

    /**
     * Sends a serializable object through the output stream.
     * @param message Accepts any int and sends it as a message.
     * @param localoutputStream accepts the local output stream.
     * @return Returns -1 if everything goes well. Returns null if an error occurs.
     */
    public static Integer sendMessage(Object message,ObjectOutputStream localoutputStream) {

        try {
            if(!(message instanceof Serializable)){
                throw new NotSerializableException("The object is not serializable");
            }
            //System.out.println( ConsoleColors.GREEN + "Sending Message: " + message + ConsoleColors.RESET);
            localoutputStream.writeObject(message);
            localoutputStream.flush();
            return -1;
        } catch(NotSerializableException notSerializableException){
            System.out.println( ConsoleColors.RED + "Not serializable object error in send message of Object type..." + ConsoleColors.RESET);
            return null;
        } catch (SocketException socketException) {
            System.out.println( ConsoleColors.RED + "Socket error in send message of Object type..." + ConsoleColors.RESET);
            return null;

        } catch (IOException e) {
            System.out.println( ConsoleColors.RED + "Error in send message of Object type..." + ConsoleColors.RESET);
            return null;
        }
    }

    /**
     * Sends int message type.
     * @param message Accepts any int and sends it as a message.
     * @param localoutputStream accepts the local output stream.
     * @return Returns -1 if everything goes well. If an error occurs it returns null.
     */
    public static Integer sendMessage(int message,ObjectOutputStream localoutputStream) {
        try {
            //System.out.println( ConsoleColors.GREEN + "Sending Message: " + message + ConsoleColors.RESET);
            localoutputStream.writeInt(message);
            localoutputStream.flush();
            return -1;
        } catch (SocketException socketException) {
            System.out.println( ConsoleColors.RED + "Socket error in send message of int type..." + ConsoleColors.RESET);
            return null;
        } catch (IOException e) {
            System.out.println( ConsoleColors.RED + "Error in send message of int type..." + ConsoleColors.RESET);
            return null;
        }
    }

    /**
     * Sends long message type.
     * @param message Accepts any int and sends it as a message.
     * @param localoutputStream accepts the local output stream.
     * @return Returns -1 if everything goes well. If an error occurs it returns null.
     */
    public static Integer sendMessage(long message,ObjectOutputStream localoutputStream) {
        try {
            //System.out.println( ConsoleColors.GREEN + "Sending Message: " + message + ConsoleColors.RESET);
            localoutputStream.writeLong(message);
            localoutputStream.flush();
            return -1;
        } catch (SocketException socketException) {
            System.out.println( ConsoleColors.RED + "Socket error in send message of int type..." + ConsoleColors.RESET);
            return null;
        } catch (IOException e) {
            System.out.println( ConsoleColors.RED + "Error in send message of int type..." + ConsoleColors.RESET);
            return null;
        }
    }

    /**
     * Write a buffer array to the output stream.
     * @param buffer Accepts the buffer that will be written onto the output stream.
     * @param localoutputStream Accepts the local output stream.
     * @return Returns -1 if everything goes well. Returns null if an error occurs.
     */
    public static Integer sendMessage(byte[] buffer,ObjectOutputStream localoutputStream){
        try {
            //System.out.println( ConsoleColors.GREEN + "Sending buffer: " + buffer + ConsoleColors.RESET);
            localoutputStream.write(buffer);
            localoutputStream.flush();
            return -1;
        } catch (SocketException socketException) {
            System.out.println( ConsoleColors.RED + "Socket error in send message of Message ENUM type..." + ConsoleColors.RESET);
            return null;
        } catch (IOException e) {
            System.out.println( ConsoleColors.RED + "Error in send message of Message ENUM type..." + ConsoleColors.RESET);
            return null;
        }
    }

    /**
     * Sends any messages from the Message ENUM found in the tools package.
     * @param message_type      Accepts any message type from the Messages ENUM found in the tools package.
     * @param localoutputStream accepts the local output stream.
     * @return returns the exit value of the program -1 indicating success and null indicating error.
     */
    public static Integer sendMessage(Messages message_type, ObjectOutputStream localoutputStream) {
        try {
            //System.out.println( ConsoleColors.GREEN + "Sending Message: " + message_type + " with ordinal number: " + message_type.ordinal() + ConsoleColors.RESET);
            localoutputStream.writeInt(message_type.ordinal());
            localoutputStream.flush();
            return -1;
        } catch (SocketException socketException) {
            System.out.println( ConsoleColors.RED + "Socket error in send message of Message ENUM type..." + ConsoleColors.RESET);
            return null;
        } catch (IOException e) {
            System.out.println( ConsoleColors.RED + "Error in send message of Message ENUM type..." + ConsoleColors.RESET);
            return null;
        }
    }



    /**
     * Sends a Message type FINISHED_OPERATION from the Messages ENUM found in the tools package.
     * @param localoutputStream accepts the local output stream.
     * @return returns the exit value of the program -1 indicating success and null indicating error.
     */
    public static Integer FinishedOperation(ObjectOutputStream localoutputStream) {
        return sendMessage(Messages.FINISHED_OPERATION,localoutputStream);
    }

}
