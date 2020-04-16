/**
 *
 */
package vn.com.sky.sys.auth;

import static org.springframework.web.reactive.function.BodyExtractors.toMono;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;
import vn.com.sky.SkyplusApplication;
import vn.com.sky.security.PBKDF2Encoder;
import vn.com.sky.sys.model.HasuraClaims;
import vn.com.sky.sys.model.HumanOrOrg;
import vn.com.sky.sys.usersettings.CustomUserSettingsRepo;
import vn.com.sky.util.SDate;

/**
 * @author roboticscm2018@gmail.com (khai.lv) Created date: Apr 17, 2019
 */

@Service
public class LoginService {
    @Value("${springbootwebfluxjjwt.jjwt.expiration}")
    private String expirationTime = "7776000";

    @Value("${app.name: Skyone}")
    private String appName;

    @Autowired
    private HumanOrOrgRepo humanOrOrgRepo;

    @Autowired
    private PBKDF2Encoder passwordEncoder;

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private CustomUserSettingsRepo customUserSettingsRepo;

    public Mono<SimpleAuthResponse> loginWithoutGenToken(ServerRequest request) throws Exception {
        ObjectMapper mapperObj = new ObjectMapper();

        var wrongPasswordException = new Exception(
            mapperObj.writeValueAsString(new SimpleAuthResponse("COMMON.MSG." + LoginResult.WRONG_PASSWORD))
        );
        var wrongUsernameException = new Exception(
            mapperObj.writeValueAsString(new SimpleAuthResponse("COMMON.MSG." + LoginResult.WRONG_USERNAME))
        );

        return request
            .bodyToMono(AuthRequest.class)
            .flatMap(
                ar -> {
                    return humanOrOrgRepo
                        .findByUsername(ar.getUsername())
                        .flatMap(
                            u -> {
                                if (passwordEncoder.encode(ar.getPassword()).equals(u.getPassword())) {
                                    return Mono.just(new SimpleAuthResponse("COMMON.MSG." + LoginResult.SUCCESS));
                                } else {
                                    return Mono.error(wrongPasswordException);
                                }
                            }
                        )
                        .switchIfEmpty(Mono.error(wrongUsernameException));
                }
            );
    }

    public Mono<AuthResponse> login(ServerRequest request) throws Exception {
        ObjectMapper mapperObj = new ObjectMapper();

        var wrongPasswordException = new Exception(
            mapperObj.writeValueAsString(new AuthResponse(LoginResult.WRONG_PASSWORD))
        );
        var wrongUsernameException = new Exception(
            mapperObj.writeValueAsString(new AuthResponse(LoginResult.WRONG_USERNAME))
        );
        var notSetCompanyException = new Exception(
            mapperObj.writeValueAsString(new AuthResponse(LoginResult.NOT_SET_COMPANY))
        );

        return request
            .bodyToMono(AuthRequest.class)
            .flatMap(
                ar -> {
                    return humanOrOrgRepo
                        .findByUsername(ar.getUsername())
                        .flatMap(
                            u -> {
                                if (passwordEncoder.encode(ar.getPassword()).equals(u.getPassword())) {
                                    String token = generateToken(ar.getUsername(), u.getId());
                                    System.out.println(token);
                                    if (u.getDefaultOwnerOrgId() == null) {
                                        return Mono.error(notSetCompanyException);
                                    }

                                    return customUserSettingsRepo
                                        .sysGetUserSettings(u.getId(), u.getDefaultOwnerOrgId())
                                        .flatMap(
                                            depIdAndMenuPath -> {
                                                var split = depIdAndMenuPath.split("#");

                                                String lang = null;
                                                String theme = null;
                                                Float alpha = null;
                                                Long companyId = null;
                                                System.out.println("xxxxx " + split[0]);
                                                //						-- 	column must be in order bellow
                                                //						-- 	companyId, -> 0
                                                //						-- 	depId,
                                                //						-- 	menuPath,
                                                //						-- 	lang, -> 3
                                                //						-- 	theme, -> 4
                                                //						-- 	alpha, -> 5
                                                //						-- 	headerHeight,

                                                if (split.length >= 6) {
                                                    companyId = Long.parseLong(split[0]);
                                                    lang = split[3];
                                                    theme = split[4];
                                                    alpha = Float.parseFloat(split[5]);
                                                }

                                                return Mono.just(
                                                    new AuthResponse(
                                                        u.getId(),
                                                        ar.getUsername(),
                                                        u.getLastName(),
                                                        u.getFirstName(),
                                                        LoginResult.SUCCESS,
                                                        token,
                                                        lang,
                                                        theme,
                                                        alpha,
                                                        companyId
                                                    )
                                                );
                                            }
                                        );
                                } else {
                                    return Mono.error(wrongPasswordException);
                                }
                            }
                        )
                        .switchIfEmpty(Mono.error(wrongUsernameException));
                }
            );
    }

    public Mono<String> resetPassword(ServerRequest request) {
        return request
            .body(toMono(AuthRequest.class))
            .flatMap(
                ar -> {
                    System.out.println(ar);
                    return humanOrOrgRepo
                        .findByUsername(ar.getUsername())
                        .flatMap(
                            u -> {
                                var email = u.getEmail();
                                if (email == null || email.trim().length() == 0) {
                                    return Mono.error(
                                        new Exception("Please contact to the Admin to get an Email address.")
                                    );
                                }

                                u.setResetPasswordTime(SDate.now());
                                u.setResetPasswordToken(UUID.randomUUID().toString());
                                humanOrOrgRepo.save(u).subscribe();

                                if (sendEmail(request, u)) return Mono.just(
                                    "Please check your email to complete reset password"
                                ); else return Mono.just("Reset password fail. Please contact to the Admin");
                            }
                        )
                        .switchIfEmpty(
                            Mono.error(new Exception("Maybe your Username is incorrect. Please try another one."))
                        );
                }
            );
    }

    public Mono<HumanOrOrg> doResetPassword(ServerRequest request) {
        return request
            .body(toMono(AuthRequest.class))
            .flatMap(
                ar -> {
                    return humanOrOrgRepo
                        .findByResetPasswordToken(ar.getResetToken())
                        .flatMap(
                            u -> {
                                u.setPassword(passwordEncoder.encode(ar.getPassword()));
                                u.setResetPasswordToken(null);
                                return humanOrOrgRepo.save(u);
                            }
                        )
                        .switchIfEmpty(Mono.error(new Exception("Maybe token expired. Please try reset again")));
                }
            );
    }

    private boolean sendEmail(ServerRequest request, HumanOrOrg user) {
        var msg = new SimpleMailMessage();

        var appUrl = request.uri();

        try {
            msg.setTo(user.getEmail());
            msg.setSubject("Reset your password");
            msg.setText(
                "To reset your password, click the link below:\n" + appUrl + "?token=" + user.getResetPasswordToken()
            );

            javaMailSender.send(msg);
            return true;
        } catch (MailException e) {
            e.printStackTrace();
            // LogUtil.log(e.getMessage(), this.getClass().getName());
        }

        return false;
    }

    //	public Mono<LoginDetails> save(ServerRequest request, LocaleResourcesParam param) {
    //		var loginDetails = new LoginDetails();
    //		var username = SecurityContextRepository.getRequestUsername(request);
    //		var token = SecurityContextRepository.getRequestToken(request);
    //
    //		loginDetails.setToken(token);
    //		loginDetails.setUsername(username);
    //		loginDetails.setIp(param.getClientIp());
    //		loginDetails.setCompanyId(param.getCompanyId());
    //		loginDetails.setCity(param.getClientCity());
    //		loginDetails.setCountry(param.getClientCountry());
    //		loginDetails.setLocation(param.getClientLocation());
    //
    //		String userAgent = "";
    //		if (request.headers().header("User-Agent").size() > 0)
    //			userAgent = request.headers().header("User-Agent").get(0);
    //
    //		loginDetails.setDevice(getClientDevice(userAgent));
    //		loginDetails.setOs(getClientOS(userAgent));
    //		loginDetails.setBrowser(getClientBrowser(userAgent));
    //
    //		return mainRepo.save(loginDetails);
    //	}

    //	private LoginInfo initLoginInfo(ServerRequest request, AuthRequest ar, User user, String token) {
    //		var loginInfo = new LoginInfo();
    //
    //		loginInfo.setToken(SecurityContextRepository.getPayloadFromToken(token));
    //		loginInfo.setUserId(user.getId());
    //		loginInfo.setUsername(user.getUsername());
    //		loginInfo.setFullname(user.getName());
    //		loginInfo.setComanyId(ar.getCompanyId());
    //		loginInfo.setBranchId(ar.getBranchId());
    //		loginInfo.setAppName(appName);
    //
    //		String userAgent = "";
    //		if (request.headers().header("User-Agent").size() > 0)
    //			userAgent = request.headers().header("User-Agent").get(0);
    //
    //		loginInfo.setClientDevice(getClientDevice(userAgent));
    //		loginInfo.setClientOs(getClientOS(userAgent));
    //		loginInfo.setClientBrowser(getClientBrowser(userAgent));
    //
    //		return loginInfo;
    //	}

    public static String getClientOS(String userAgent) {
        String caseBrowser = userAgent.toLowerCase();
        if (caseBrowser.contains("windows")) {
            return "Windows";
        } else if (caseBrowser.contains("mac")) {
            return "Mac OS";
        } else if (caseBrowser.contains("x11")) {
            return "Unix";
        } else if (caseBrowser.contains("android")) {
            return "Android";
        } else if (caseBrowser.contains("iphone")) {
            return "IPhone";
        } else {
            return "UnKnown, More-Info: " + userAgent;
        }
    }

    public static String getClientDevice(String userAgent) {
        String caseBrowser = userAgent.toLowerCase();
        if (caseBrowser.contains("windows")) {
            return "Laptop/PC";
        } else if (caseBrowser.contains("mac")) {
            return "Mac";
        } else if (caseBrowser.contains("x11")) {
            return "Laptop/PC";
        } else if (caseBrowser.contains("android")) {
            return "Android";
        } else if (caseBrowser.contains("iphone")) {
            return "IPhone";
        } else {
            return "UnKnown, More-Info: " + userAgent;
        }
    }

    public static String getClientBrowser(String userAgent) {
        String user = userAgent.toLowerCase();
        String browser = "";
        if (user.contains("msie")) {
            String substring = userAgent.substring(userAgent.indexOf("MSIE")).split(";")[0];
            browser = substring.split(" ")[0].replace("MSIE", "IE") + "-" + substring.split(" ")[1];
        } else if (user.contains("safari") && user.contains("version")) {
            browser =
                (userAgent.substring(userAgent.indexOf("Safari")).split(" ")[0]).split("/")[0] +
                "-" +
                (userAgent.substring(userAgent.indexOf("Version")).split(" ")[0]).split("/")[1];
        } else if (user.contains("opr") || user.contains("opera")) {
            if (user.contains("opera")) browser =
                (userAgent.substring(userAgent.indexOf("Opera")).split(" ")[0]).split("/")[0] +
                "-" +
                (userAgent.substring(userAgent.indexOf("Version")).split(" ")[0]).split("/")[1]; else if (
                user.contains("opr")
            ) browser =
                ((userAgent.substring(userAgent.indexOf("OPR")).split(" ")[0]).replace("/", "-")).replace(
                        "OPR",
                        "Opera"
                    );
        } else if (user.contains("coc_coc_browser")) {
            browser = (userAgent.substring(userAgent.indexOf("coc_coc")).split(" ")[0]).replace("/", "-");
        } else if (user.contains("chrome")) {
            browser = (userAgent.substring(userAgent.indexOf("Chrome")).split(" ")[0]).replace("/", "-");
        } else if (
            (user.indexOf("mozilla/7.0") > -1) ||
            (user.indexOf("netscape6") != -1) ||
            (user.indexOf("mozilla/4.7") != -1) ||
            (user.indexOf("mozilla/4.78") != -1) ||
            (user.indexOf("mozilla/4.08") != -1) ||
            (user.indexOf("mozilla/3") != -1)
        ) {
            browser = "Netscape-?";
        } else if (user.contains("firefox")) {
            browser = (userAgent.substring(userAgent.indexOf("Firefox")).split(" ")[0]).replace("/", "-");
        } else if (user.contains("rv")) {
            browser = "IE";
        } else {
            browser = "UnKnown, More-Info: " + userAgent;
        }

        return browser;
    }

    private PrivateKey getPrivateKey(String privateKey) throws Exception {
        // Read in the key into a String
        StringBuilder pkcs8Lines = new StringBuilder();
        BufferedReader rdr = new BufferedReader(new StringReader(privateKey));
        String line;
        while ((line = rdr.readLine()) != null) {
            pkcs8Lines.append(line);
        }
        // Remove the "BEGIN" and "END" lines, as well as any whitespace
        String pkcs8Pem = pkcs8Lines.toString();
        pkcs8Pem = pkcs8Pem.replace("-----BEGIN PRIVATE KEY-----", "");
        pkcs8Pem = pkcs8Pem.replace("-----END PRIVATE KEY-----", "");
        pkcs8Pem = pkcs8Pem.replaceAll("\\s+", "");
        // Base64 decode the result
        byte[] pkcs8EncodedBytes = Base64.getDecoder().decode(pkcs8Pem);
        // extract the private key
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(pkcs8EncodedBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PrivateKey privKey = kf.generatePrivate(keySpec);
        return privKey;
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

    public String generateToken(String username, Long userId) {
        Long expirationTimeLong = Long.parseLong(expirationTime); // in second
        final Date createdDate = new Date();
        final Date expirationDate = new Date(createdDate.getTime() + expirationTimeLong * 1000);

        String token = null;
        String privateKey;

        Map<String, Object> payload = new HashMap<String, Object>();
        Map<String, Object> header = new HashMap<String, Object>();
        header.put("type", "JWT");
        //        payload.put("https://hasura.io/jwt/claims", new HasuraClaims("user", new String[] {"user"}, userId));

        try {
            ObjectMapper mapper = new ObjectMapper();
            String jsonString = mapper.writeValueAsString(
                new HasuraClaims("user", new String[] { "user" }, username, userId)
            );
            payload.put("https://hasura.io/jwt/claims", jsonString);
            privateKey = readFileToString(getFullFileName("private-pkcs8.pem"));
            PrivateKey pvtKey = getPrivateKey(privateKey);
            token =
                Jwts
                    .builder()
                    .setClaims(payload)
                    .setSubject(username)
                    .setIssuedAt(createdDate)
                    .setExpiration(expirationDate)
                    .setHeader(header)
                    .signWith(pvtKey, SignatureAlgorithm.RS256)
                    .compact();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return token;
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
}
