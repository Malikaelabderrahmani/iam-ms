package pfe.mandomati.iamms.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import pfe.mandomati.iamms.Model.Request;

public interface RequestRepository extends JpaRepository<Request, Long> {
    
}
