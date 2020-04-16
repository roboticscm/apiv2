package vn.com.sky.sys.auth;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import vn.com.sky.sys.model.AuthToken;

@Repository
public interface AuthTokenRepo extends ReactiveCrudRepository<AuthToken, Long> {}
