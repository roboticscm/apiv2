package vn.com.sky.security;

import Decoder.BASE64Decoder;
import Decoder.BASE64Encoder;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public final class SecurityUtil {
    public static final String WEB_TOKEN_KEY = "sessionId";
    public static final String BLOWFISH_ENCRYPTION_KEY = "12345678910";

    public static String blowfishBase64Encrypt(String data) {
        byte[] keyData = BLOWFISH_ENCRYPTION_KEY.getBytes();
        SecretKeySpec secretKeySpec = new SecretKeySpec(keyData, "Blowfish");
        try {
            Cipher cipher = Cipher.getInstance("Blowfish");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            byte[] hasil = cipher.doFinal(data.getBytes());
            return new BASE64Encoder().encode(hasil);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String blowfishBase64Decrypt(String data) {
        byte[] keyData = BLOWFISH_ENCRYPTION_KEY.getBytes();
        SecretKeySpec secretKeySpec = new SecretKeySpec(keyData, "Blowfish");
        try {
            Cipher cipher = Cipher.getInstance("Blowfish");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            byte[] hasil = cipher.doFinal(new BASE64Decoder().decodeBuffer(data));
            return new String(hasil);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    //test
    public static void main(String args[]) {
        //LogUtil.print(blowfishBase64Encrypt("eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJub2lzb2kiLCJpYXQiOjE1NTMyMTU0MjksImV4cCI6MTU1MzI0NDIyOX0.JGAYFxoVAMCfZUq_aMP6W5Lo6lf4ca-dSVZH7nGWwsOAe_XEt3TIvE7zLZMMJuZHRMqmWRMYDe5GBmmf0D0DqA"));
    }
}
