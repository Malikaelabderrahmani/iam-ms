package pfe.mandomati.iamms.Repository;

import pfe.mandomati.iamms.Model.User; 
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
