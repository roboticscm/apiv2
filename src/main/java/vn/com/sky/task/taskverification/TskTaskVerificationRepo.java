package vn.com.sky.task.taskverification;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import vn.com.sky.task.model.TskTaskVerification;

@Repository
public interface TskTaskVerificationRepo extends ReactiveCrudRepository<TskTaskVerification, Long> {

}
