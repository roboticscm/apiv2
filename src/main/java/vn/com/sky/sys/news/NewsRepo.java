package vn.com.sky.sys.news;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Flux;
import vn.com.sky.sys.model.News;

@Repository
public interface NewsRepo extends ReactiveCrudRepository<News, Long> {
    @Query("select * from news where start_date <= :now and end_date >= :now and disabled = false order by pin_on_top, start_date, view_counter, title limit 8")
    public Flux<News> findTop(Long now);
}
