package vn.com.sky.task.status;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import vn.com.sky.task.model.TskStatus;

@Repository
public interface TskStatusRepo extends ReactiveCrudRepository<TskStatus, Long> {

}
