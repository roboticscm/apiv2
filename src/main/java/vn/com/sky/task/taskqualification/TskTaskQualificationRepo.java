package vn.com.sky.task.taskqualification;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import vn.com.sky.task.model.TskTaskQualification;

@Repository
public interface TskTaskQualificationRepo extends ReactiveCrudRepository<TskTaskQualification, Long> {

}
