package pfe.mandomati.iamms.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pfe.mandomati.iamms.Model.Role;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);  // Recherche par nom de rôle
}
