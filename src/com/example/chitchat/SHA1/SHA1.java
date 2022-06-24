package com.example.chitchat.SHA1;


import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA1 {

    /**
     * Hashes the input using the SHA1 algorithm. Used for broker's ID, user node nickname etc.
     * @param input Accepts any string input.
     * @return Returns the hashed version of the input string in hexadecimal form.
     */
    public static String encrypt(String input) {
        if(input == null){
            System.out.println("\033[0;31m" + "Error a null string was provided as a parameter in the encrypt function" + "\033[0m");
            return null;
        }
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            String ready_sha;
            sha.reset();
            sha.update(input.getBytes(StandardCharsets.UTF_8));
            byte[] messageDigest = sha.digest();
            BigInteger no = new BigInteger(1, messageDigest);
            ready_sha = String.format("%040x", no);
            if(ready_sha == null){
                System.out.println("\033[0;31m" + "Error a null string was returned while formatting the string to hexadecimal form" + "\033[0m");
                return null;
            }
            return ready_sha;
        }
        catch(NoSuchAlgorithmException e){
            throw new RuntimeException(e);
        }
    }
    public static Integer hextoInt(String input, int n){

        BigInteger value = new BigInteger(input, 16);
        BigInteger modulo_op = value.mod(BigInteger.valueOf(n));
        if(modulo_op == null){
            System.out.println("\033[0;31m" + "Error a null string was returned with the modulo OP" + "\033[0m");
            return null;
        }
        return modulo_op.intValue();

    }
    public static void main(String[] args) {
        System.out.println(SHA1.encrypt("this is a test"));
        // works fine fa26be19de6bff93f70bc2308434e4a440bbad02 should be the result
        System.out.println(hextoInt(SHA1.encrypt("this is a test"),3));
    }
}
