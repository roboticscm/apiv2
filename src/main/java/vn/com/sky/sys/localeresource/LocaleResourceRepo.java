package vn.com.sky.sys.localeresource;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import vn.com.sky.sys.model.LocaleResource;

@Repository
public interface LocaleResourceRepo extends ReactiveCrudRepository<LocaleResource, Long> {
    @Query(
        "select * from locale_resource where company_id=:companyId and category=:category and type_group=:typeGroup and key=:key and locale=:locale and deleted_by is null limit 1"
    )
    Mono<LocaleResource> findByComanyIdCategoryTypeGroupKeyAndLocale(
        Long companyId,
        String category,
        String typeGroup,
        String key,
        String locale
    );
}
