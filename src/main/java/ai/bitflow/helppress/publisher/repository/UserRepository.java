package ai.bitflow.helppress.publisher.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ai.bitflow.helppress.publisher.domain.User;


@Repository
public interface UserRepository extends JpaRepository <User, String> {

	Optional<User> findOneByUsernameAndPassword(String id, String password);
	
}
