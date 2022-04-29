package NetworkUtilities;
import Tools.Messages;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;

public class GeneralUtils {

    /**
     *
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
            System.out.println("Socket error");
            return null;
        } catch (IOException e) {
            System.out.println("Shutting down connection in read UTF string...");
            return null;
        }
    }

    /**
     * Reads a UTF string from the input stream using the inputStream.readUTF() method.Shutdowns the connection if a exception is thrown.
     * @param localinputStream  accepts the local input stream.
     * @param socket            accepts the corresponding socket of the streams.
     * @return returns the input read from the local input stream. If it catches an exception it returns null as an error string.
     */
    public static String readUTFString(ObjectInputStream localinputStream, Socket socket) {
        try {
            System.out.println("Waiting to read UTF type");
            String message = localinputStream.readUTF();
            System.out.println("Received message: " + message + " from node: " + socket.getInetAddress());
            return message;
        } catch (SocketException socketException) {
            System.out.println("Socket error");
            return null;
        } catch (IOException e) {
            System.out.println("Shutting down connection in read UTF string...");
            return null;
        }
    }

    /**
     * Waits until an int input is read by the input stream using the inputStream.readInt() method.Shutdowns the connection if a exception is thrown.
     *
     * @param localinputStream  accepts the local input stream.
     * @param socket accepts the corresponding socket of the streams.
     * @return returns the input read from the local input stream. If it catches an exception it returns -1 as an error int.
     */
    public static Integer waitForNodePrompt(ObjectInputStream localinputStream, Socket socket) {
        try {
            System.out.println("Waiting for node prompt");
            int message = localinputStream.readInt();
            System.out.println("Received message: " + message + " from node: " + socket.getInetAddress());
            return message;
        } catch (SocketException socketException) {
            System.out.println("Socket error");
            return null;
        } catch (IOException e) {
            System.out.println("...");
            return null;
        }
    }

    public static Object readObject(ObjectInputStream localinputStream,Socket socket){
        try {
            System.out.println("Waiting to receive object from input stream");
            Object message = localinputStream.readObject();
            System.out.println("Received message: " + message + " from node: " + socket.getInetAddress());
            return message;
        } catch (SocketException socketException) {
            System.out.println("Socket error");
            return null;
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error in read object...");
            return null;
        }
    }

    /**
     * Sends a string object through the output stream. Shutdowns the connection if a exception is thrown.
     * @param message Accepts any int and sends it as a message.
     * @param localoutputStream Accepts the local output stream.
     */
    public static Integer sendMessage(String message, ObjectOutputStream localoutputStream) {
        try {
            System.out.println("Sending Message: " + message);
            localoutputStream.writeUTF(message);
            localoutputStream.flush();
            return -1;
        } catch (SocketException socketException) {
            System.out.println("Socket error");
            return null;

        } catch (IOException e) {
            System.out.println("Error in send message");
            return null;
        }
    }

    /**
     * Sends a serializable object through the output stream. Shutdowns the connection if a exception is thrown.
     *
     * @param message           Accepts any int and sends it as a message.
     * @param localoutputStream accepts the local output stream.
     */
    public static Integer sendMessage(Object message,ObjectOutputStream localoutputStream) {
        try {
            System.out.println("Sending Message: " + message);
            localoutputStream.writeObject(message);
            localoutputStream.flush();
            return -1;
        } catch (SocketException socketException) {
            System.out.println("Socket error");
            return null;

        } catch (IOException e) {
            System.out.println("Error in send message");
            //shutdownConnection(localinputStream, localoutputStream, socket);
            return null;
        }
    }

    /**
     * Sends int message type. Shutdowns the connection if a exception is thrown.
     *
     * @param message           Accepts any int and sends it as a message.
     * @param localoutputStream accepts the local output stream.
     */
    public static Integer sendMessage(int message,ObjectOutputStream localoutputStream) {
        try {
            System.out.println("Sending Message: " + message);
            localoutputStream.writeInt(message);
            localoutputStream.flush();
            return -1;
        } catch (SocketException socketException) {
            System.out.println("Socket error");
            return null;
        } catch (IOException e) {
            System.out.println("Error in send message");
            return null;
        }
    }

    /**
     * Sends any messages from the Message ENUM found in the tools package. Shutdowns the connection if a exception is thrown.
     *
     * @param message_type      Accepts any message type from the Messages ENUM found in the tools package.
     * @param localoutputStream accepts the local output stream.
     * @return returns the exit value of the program -1 indicating success and null indicating error.
     */
    public static Integer sendMessage(Messages message_type,ObjectOutputStream localoutputStream) {
        try {
            System.out.println("Sending Message: " + message_type);
            localoutputStream.writeInt(message_type.ordinal());
            localoutputStream.flush();
            return -1;
        } catch (SocketException socketException) {
            System.out.println("Socket error");
            return null;
        } catch (IOException e) {
            System.out.println("Error in send message");
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
