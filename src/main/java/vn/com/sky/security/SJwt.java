package vn.com.sky.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URL;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import vn.com.sky.SkyplusApplication;
import vn.com.sky.sys.model.HasuraClaims;
import vn.com.sky.sys.model.HumanOrOrg;

/**
 * Created by IntelliJ IDEA.
 *
 * @Author: khai.lv (roboticscm2018@gmail.com) Date: 3/19/2019 Time: 6:24 PM
 */
@Component
public class SJwt implements Serializable {
    private static final long serialVersionUID = 1L;

    @Value("${springbootwebfluxjjwt.jjwt.secret}")
    private String secret = "ThisIsSecretForJWTHS512SignatureAlgorithmThatMUSTHave512bitsKeySize";

    @Value("${springbootwebfluxjjwt.jjwt.expiration}")
    private String expirationTime = "7776000";

    public Claims getAllClaimsFromToken(String token) {
        try {
            String publicKeyPEM = readFileToString(getFullFileName("public.pem"));
            publicKeyPEM = publicKeyPEM.replace("-----BEGIN PUBLIC KEY-----\n", "");
            publicKeyPEM = publicKeyPEM.replace("-----END PUBLIC KEY-----", "");
            publicKeyPEM = publicKeyPEM.replace("\n\r", "");
            publicKeyPEM = publicKeyPEM.replace("\n", "");

            byte[] encodedKey = Base64.getDecoder().decode(publicKeyPEM);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encodedKey);

            KeyFactory kf = KeyFactory.getInstance("RSA");
            PublicKey publicKey = kf.generatePublic(keySpec);

            return Jwts.parser().setSigningKey(publicKey).parseClaimsJws(token).getBody();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getUsernameFromToken(String token) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            HasuraClaims obj = mapper.readValue(
                getAllClaimsFromToken(token).get("https://hasura.io/jwt/claims").toString(),
                HasuraClaims.class
            );
            return obj.getXHasuraUsername();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Long getUserIdFromToken(String token) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            HasuraClaims obj = mapper.readValue(
                getAllClaimsFromToken(token).get("https://hasura.io/jwt/claims").toString(),
                HasuraClaims.class
            );
            return obj.getXHasuraUserId();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Date getExpirationDateFromToken(String token) {
        return getAllClaimsFromToken(token).getExpiration();
    }

    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    public String generateToken(HumanOrOrg user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRoles());
        return doGenerateToken(claims, user.getUsername());
    }

    public String generateToken(Map<String, Object> claims, HumanOrOrg user) {
        return doGenerateToken(claims, user.getUsername());
    }

    private String doGenerateToken(Map<String, Object> claims, String username) {
        Long expirationTimeLong = Long.parseLong(expirationTime); // in second

        final Date createdDate = new Date();
        final Date expirationDate = new Date(createdDate.getTime() + expirationTimeLong * 1000);

        try {
            String privateKey = readFileToString(getFullFileName("private-pkcs8.pem"));
            byte[] encodedKey = Base64.getDecoder().decode(privateKey);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encodedKey);

            KeyFactory kf = KeyFactory.getInstance("RSA");
            PrivateKey privKey = kf.generatePrivate(keySpec);

            return Jwts
                .builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(createdDate)
                .setExpiration(expirationDate)
                .signWith(privKey, SignatureAlgorithm.RS256)
                .compact();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static String getFullFileName(String fileName) {
        URL location = SkyplusApplication.class.getProtectionDomain().getCodeSource().getLocation();

        File f = new File(location.getFile() + "/" + fileName);
        if (f.exists() && !f.isDirectory()) {
            return location.getFile() + "/" + fileName;
        } else {
            return System.getProperty("user.dir") + "/" + fileName;
        }
    }

    private static String readFileToString(String fileName) throws Exception {
        InputStream is = new FileInputStream(fileName);
        BufferedReader buf = new BufferedReader(new InputStreamReader(is));
        String line = buf.readLine();
        StringBuilder sb = new StringBuilder();
        while (line != null) {
            sb.append(line).append("\n");
            line = buf.readLine();
        }
        buf.close();
        return sb.toString();
    }

    public static PrivateKey loadPrivateKey(String fileName) throws Exception {
        String privateKeyPEM = readFileToString(fileName);

        // strip of header, footer, newlines, whitespaces
        privateKeyPEM =
            privateKeyPEM
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        // decode to get the binary DER representation
        byte[] privateKeyDER = Base64.getDecoder().decode(privateKeyPEM);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKeyDER));
    }

    public Boolean validateToken(String token) {
        return !isTokenExpired(token);
    }
}
