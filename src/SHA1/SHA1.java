package SHA1;
import java.math.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class SHA1 {
    public static String encrypt(String input) {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            String ready_sha;
            sha.reset();
            sha.update(input.getBytes(StandardCharsets.UTF_8));
            byte[] messageDigest = sha.digest();
            BigInteger no = new BigInteger(1, messageDigest);
            ready_sha = String.format("%040x", no);

            return ready_sha;
        }
        catch(NoSuchAlgorithmException e){
            throw new RuntimeException(e);
        }
    }
    public static int hextoInt(String input, int n){

        BigInteger value = new BigInteger(input, 16);
        BigInteger modulo_op = value.mod(BigInteger.valueOf(n));
        return modulo_op.intValue();

    }
    public static void main(String[] args) {
        System.out.println(SHA1.encrypt("this is a test"));
        // works fine fa26be19de6bff93f70bc2308434e4a440bbad02 should be the result
        System.out.println(hextoInt(SHA1.encrypt("this is a test"),3));
    }
}
