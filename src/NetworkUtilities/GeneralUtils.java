package NetworkUtilities;
import Tools.Messages;
import Logging.ConsoleColors;

import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;

public class GeneralUtils {

    /**
     * Reads data into the parameter buffer. The number of data read is the actual size parameter.
     * @param localinputStream  Accepts the local input stream.
     * @param buffer Accepts a byte buffer and writes the data from the input stream to the buffer.
     * @param offset Accepts the offset start reading the local input stream.
     * @param actual_size Accepts the amount of data to be read to the buffer. The actual size name here refers to actual size of the chunk being sent.
     * @return Returns a byte array. This byte array is the array that the data will be read to. Remember because passes by value when you return the byte array assign it to the local byte array.
     */
    public static byte[] readBuffer(ObjectInputStream localinputStream, byte[] buffer, int offset, Integer actual_size){
        try {
            localinputStream.readFully(buffer,0,actual_size);
            return buffer;
        } catch (SocketException socketException) {
            System.out.println( "\033[1;31m" + "Socket error in read buffer..." + "\033[0m");
            return null;
        } catch (IOException e) {
            System.out.println( "\033[1;31m" + "Error in read buffer..." + "\033[0m");
            return null;
        }
    }

    /**
     * Reads a UTF string from the input stream using the inputStream.readUTF() method.
     * @param localinputStream  accepts the local input stream.
     * @param socket            accepts the corresponding socket of the streams.
     * @return returns the input read from the local input stream. If it catches an exception it returns null as an error string.
     */
    public static String readUTFString(ObjectInputStream localinputStream, Socket socket) {
        try {
            System.out.println("\033[0;32m" + "Waiting to read UTF type" + "\033[0m");
            String message = localinputStream.readUTF();
            System.out.println("\033[0;32m" + "Received message: " + message + " from node: " + socket.getInetAddress() + "\033[0m");
            return message;
        } catch (SocketException socketException) {
            System.out.println( "\033[1;31m" + "Socket error in read UTF string..." + "\033[0m");
            return null;
        } catch (IOException e) {
            System.out.println( "\033[1;31m" + "Error in read UTF string..." + "\033[0m");
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
            System.out.println("\033[0;32m" + "Waiting for node prompt" + "\033[0m");
            int message = localinputStream.readInt();
            System.out.println("\033[0;32m" + "Received message: " + message + " from node: " + socket.getInetAddress() + "\033[0m");
            return message;
        } catch (SocketException socketException) {
            System.out.println( "\033[1;31m" + "Socket error in wait for node prompt..." + "\033[0m");
            return null;
        } catch (IOException e) {
            System.out.println( "\033[1;31m" + "Error in wait for node prompt..." + "\033[0m");
            return null;
        }
    }

    /**
     * Waits until an object input is read from the input stream using the inputStream.readObject() method.
     * @param localinputStream Accepts the local input stream.
     * @param socket Accepts the local socket.
     * @return Returns the object read if everything goes well. If an error occurs it returns nulls.
     */
    public static Object readObject(ObjectInputStream localinputStream,Socket socket){
        try {
            System.out.println("\033[0;32m" + "Waiting to receive object from input stream" + "\033[0m");
            Object message = localinputStream.readObject();
            System.out.println("\033[0;32m" + "Received message: " + message + " from node: " + socket.getInetAddress() + "\033[0m");
            return message;
        } catch (SocketException socketException) {
            System.out.println( "\033[1;31m" + "Socket error in read object..." + "\033[0m");
            return null;
        } catch (IOException | ClassNotFoundException e) {
            System.out.println( "\033[1;31m" + "Error in read object..." + "\033[0m");
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
            System.out.println( "\033[0;32m" + "Sending Message: " + message + "\033[0m");
            localoutputStream.writeUTF(message);
            localoutputStream.flush();
            return -1;
        } catch (SocketException socketException) {
            System.out.println( "\033[1;31m" + "Socket error in send message of string type..." + "\033[0m");
            return null;
        } catch (IOException e) {
            System.out.println( "\033[1;31m" + "Error in send message of string type..." + "\033[0m");
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
            System.out.println( "\033[0;32m" + "Sending Message: " + message + "\033[0m");
            localoutputStream.writeObject(message);
            localoutputStream.flush();
            return -1;
        } catch (SocketException socketException) {
            System.out.println( "\033[1;31m" + "Socket error in send message of Object type..." + "\033[0m");
            return null;

        } catch (IOException e) {
            System.out.println( "\033[1;31m" + "Error in send message of Object type..." + "\033[0m");
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
            System.out.println( "\033[0;32m" + "Sending Message: " + message + "\033[0m");
            localoutputStream.writeInt(message);
            localoutputStream.flush();
            return -1;
        } catch (SocketException socketException) {
            System.out.println( "\033[1;31m" + "Socket error in send message of int type..." + "\033[0m");
            return null;
        } catch (IOException e) {
            System.out.println( "\033[1;31m" + "Error in send message of int type..." + "\033[0m");
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
            System.out.println( "\033[0;32m" + "Sending buffer: " + buffer + "\033[0m");
            localoutputStream.write(buffer);
            localoutputStream.flush();
            return -1;
        } catch (SocketException socketException) {
            System.out.println( "\033[1;31m" + "Socket error in send message of Message ENUM type..." + "\033[0m");
            return null;
        } catch (IOException e) {
            System.out.println( "\033[1;31m" + "Error in send message of Message ENUM type..." + "\033[0m");
            return null;
        }
    }

    /**
     * Sends any messages from the Message ENUM found in the tools package.
     * @param message_type      Accepts any message type from the Messages ENUM found in the tools package.
     * @param localoutputStream accepts the local output stream.
     * @return returns the exit value of the program -1 indicating success and null indicating error.
     */
    public static Integer sendMessage(Messages message_type,ObjectOutputStream localoutputStream) {
        try {
            System.out.println( "\033[0;32m" + "Sending Message: " + message_type + "\033[0m");
            localoutputStream.writeInt(message_type.ordinal());
            localoutputStream.flush();
            return -1;
        } catch (SocketException socketException) {
            System.out.println( "\033[1;31m" + "Socket error in send message of Message ENUM type..." + "\033[0m");
            return null;
        } catch (IOException e) {
            System.out.println( "\033[1;31m" + "Error in send message of Message ENUM type..." + "\033[0m");
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
