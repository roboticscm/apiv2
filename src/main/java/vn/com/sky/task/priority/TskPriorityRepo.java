package vn.com.sky.task.priority;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import vn.com.sky.task.model.TskPriority;

@Repository
public interface TskPriorityRepo extends ReactiveCrudRepository<TskPriority, Long> {

}
