package vn.com.sky.sys.language;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import vn.com.sky.sys.model.Language;

@Repository
public interface LanguageRepo extends ReactiveCrudRepository<Language, Long> {
    @Query("select exists (select from language where lower(name) = lower(:name))")
    public Mono<Boolean> isNameExisted(String name);

    @Query("select exists (select from language where lower(locale) = lower(:locale))")
    public Mono<Boolean> isLocaleExisted(String locale);

    @Query("select exists (select from language where lower(name) = lower(:name) and id != :id)")
    public Mono<Boolean> isNameDuplicated(String name, Long id);

    @Query("select exists (select from language where lower(locale) = lower(:locale) and id != :id)")
    public Mono<Boolean> isLocaleDuplicated(String locale, Long id);
}
