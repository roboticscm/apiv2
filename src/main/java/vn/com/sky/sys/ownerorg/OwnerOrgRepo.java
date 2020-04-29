package vn.com.sky.sys.ownerorg;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import vn.com.sky.sys.model.OwnerOrg;

@Repository
public interface OwnerOrgRepo extends ReactiveCrudRepository<OwnerOrg, Long> {
    @Query(
        "select id, name from owner_org where parent_id is null and deleted_by is null and disabled = false order by sort, name"
    )
    public Flux<OwnerOrg> findAllCompanyList();
    
    @Query(
            "select id from owner_org where default_org = true and parent_id is null and deleted_by is null and disabled = false limit 1"
        )
    public Mono<Long> findFirstCompanyId();
    
    
    @Query("select exists (select from owner_org where lower(name) = lower(:name) and  ((parent_id is null and :parentId is null) or (parent_id = :parentId) )) ")
    public Mono<Boolean> isNameExisted(String name, Long parentId);
    
    @Query("select exists (select from owner_org where lower(name) = lower(:name) and  (parent_id is null or parent_id=:parentId) and id != :id)")
    public Mono<Boolean> isNameDuplicated(String name, Long parentId, Long id);
}
