package pfe.mandomati.iamms.Service;

import org.springframework.http.ResponseEntity;

import pfe.mandomati.iamms.Dto.RHDto;

public interface RHService {

    ResponseEntity<String> register(RHDto rhDto);

}
