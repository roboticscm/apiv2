package vn.com.sky.sys.auth;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import vn.com.sky.sys.model.HumanOrOrg;

@Repository
public interface HumanOrOrgRepo extends ReactiveCrudRepository<HumanOrOrg, Long> {
    @Query("select * from human_or_org where username = $1")
    public Mono<HumanOrOrg> findByUsername(String username);

    @Query("select * from human_or_org where reset_password_token = $1")
    public Mono<HumanOrOrg> findByResetPasswordToken(String token);
}
