package vn.com.sky.sys.searchutil;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import vn.com.sky.sys.model.SearchField;

@Repository
public interface SearchFieldRepo extends ReactiveCrudRepository<SearchField, Long> {

}
