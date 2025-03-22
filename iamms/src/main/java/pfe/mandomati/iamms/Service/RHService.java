package pfe.mandomati.iamms.Service;

import org.springframework.http.ResponseEntity;

import pfe.mandomati.iamms.Dto.RHDto;

public interface RHService {

    ResponseEntity<String> register(RHDto rhDto);
    ResponseEntity<String> update(Long rhId, String username, RHDto rhDto);
    ResponseEntity<String> delete(Long rhId, String username);
    ResponseEntity<?> getAllRH();

}
