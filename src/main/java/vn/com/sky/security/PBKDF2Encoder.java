package vn.com.sky.security;

import java.security.MessageDigest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Created by IntelliJ IDEA.
 *
 * @Author: khai.lv (roboticscm2018@gmail.com)
 * Date: 3/19/2019
 * Time: 6:15 PM
 */
@Component
public class PBKDF2Encoder implements PasswordEncoder {
    @Value("${springbootwebfluxjjwt.password.encoder.secret}")
    private String secret;

    @Value("${springbootwebfluxjjwt.password.encoder.iteration}")
    private Integer iteration;

    @Value("${springbootwebfluxjjwt.password.encoder.keylength}")
    private Integer keylength;

    /**
     * More info (https://www.owasp.org/index.php/Hashing_Java)
     * @param cs password
     * @return encoded password
     */
    //    @Override
    //    public String encode(CharSequence cs) {
    //        try {
    //            byte[] result = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512")
    //                    .generateSecret(new PBEKeySpec(cs.toString().toCharArray(), secret.getBytes(), iteration, keylength))
    //                    .getEncoded();
    //            return Base64.getEncoder().encodeToString(result);
    //        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
    //            throw new RuntimeException(ex);
    //        }
    //    }

    @Override
    public String encode(CharSequence cs) {
        try {
            String password = cs.toString();
            password = "Skyhub@010116" + password;
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest(password.getBytes("UTF-8"));
            StringBuffer hexString = new StringBuffer();

            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public boolean matches(CharSequence cs, String string) {
        return encode(cs).equals(string);
    }
}
