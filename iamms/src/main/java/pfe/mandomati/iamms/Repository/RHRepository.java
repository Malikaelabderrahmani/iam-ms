package pfe.mandomati.iamms.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import pfe.mandomati.iamms.Model.RH;

public interface RHRepository extends JpaRepository<RH, Long> {

    boolean existsByUserIdOrCniOrCnssNumber(Long id, String cni, String cnssNumber);
}
