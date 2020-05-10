package vn.com.sky.task.project;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import vn.com.sky.task.model.TskProject;

@Repository
public interface TskProjectRepo extends ReactiveCrudRepository<TskProject, Long> {

}
