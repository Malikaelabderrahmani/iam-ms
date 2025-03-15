package pfe.mandomati.iamms.Repository;

import pfe.mandomati.iamms.Model.User;

import java.util.List;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    List<User> findAllByRoleName(String roleName);
    User findByRoleNameAndId(String roleName, Long id);
}
