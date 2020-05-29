package vn.com.sky.sys.searchutil;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Mono;
import vn.com.sky.sys.model.SearchFieldMenu;

@Repository
public interface SearchFieldMenuRepo extends ReactiveCrudRepository<SearchFieldMenu, Long> {
	@Query("SELECT sfm.* " + 
			"FROM search_field_menu sfm " + 
			"WHERE EXISTS (SELECT FROM search_field WHERE id = sfm.search_field_id AND field=:field) " + 
			"	AND EXISTS (SELECT FROM menu WHERE id = sfm.menu_id AND path=:menuPath)")
	public Mono<SearchFieldMenu> findByFieldAndMenuPath(String field, String menuPath);
}
