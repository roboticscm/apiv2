package vn.com.sky.sys.ownerorg;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import vn.com.sky.sys.model.OwnerOrg;

@Repository
public interface OwnerOrgRepo extends ReactiveCrudRepository<OwnerOrg, Long> {
    @Query(
        "select id, name from owner_org where parent_id is null and deleted_by is null and disabled = false order by sort, name"
    )
    public Flux<OwnerOrg> findAllCompanyList();
}
