package vn.com.sky.sys.role;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import vn.com.sky.sys.model.Role;

@Repository
public interface RoleRepo extends ReactiveCrudRepository<Role, Long> {}
