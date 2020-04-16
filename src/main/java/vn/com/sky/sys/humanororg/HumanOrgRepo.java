package vn.com.sky.sys.humanororg;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import vn.com.sky.sys.model.HumanOrg;
import vn.com.sky.util.OneToOneRepo;

@Repository
public interface HumanOrgRepo extends OneToOneRepo<HumanOrg> {
    @Query("select * from human_org where deleted_by is null and human_id=:humanId and org_id=:orgId")
    public Mono<HumanOrg> findRelation(Long humanId, Long orgId);

    @Query("delete from human_org where human_id=:humanId and org_id=:orgId")
    public Mono<Void> deleteRelation(Long humanId, Long orgId);
}
