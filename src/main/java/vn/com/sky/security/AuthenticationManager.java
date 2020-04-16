package vn.com.sky.security;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Created by IntelliJ IDEA.
 *
 * @Author: khai.lv (roboticscm2018@gmail.com)
 * Date: 3/19/2019
 * Time: 6:26 PM
 */

@Component
public class AuthenticationManager implements ReactiveAuthenticationManager {
    @Autowired
    private SJwt sjwt;

    private String authToken;

    public Long getUserId() {
        return sjwt.getUserIdFromToken(authToken);
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String authToken = authentication.getCredentials().toString();
        this.authToken = authToken;
        String username;
        try {
            username = sjwt.getUsernameFromToken(authToken);
        } catch (Exception e) {
            e.printStackTrace();
            username = null;
        }
        if (username != null && sjwt.validateToken(authToken)) {
            //            Claims claims = sjwt.getAllClaimsFromToken(authToken);
            //            List<String> rolesMap = claims.get("role", List.class);
            //            if(rolesMap==null || rolesMap.size()==0){
            //                rolesMap.add(JwtRole.ROLE_USER.name());
            //            }
            List<JwtRole> role = new ArrayList<>();

            //            for (String rolemap : rolesMap) {
            //                role.add(JwtRole.valueOf(rolemap));
            //            }
            role.add(JwtRole.ROLE_USER);
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                username,
                null,
                role
                    .stream()
                    .map(authority -> new SimpleGrantedAuthority(authority.name()))
                    .collect(Collectors.toList())
            );
            return Mono.just(auth);
        } else {
            return Mono.empty();
        }
    }
}
