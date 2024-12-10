package pfe.mandomati.iamms.repository;


import pfe.mandomati.iamms.model.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}