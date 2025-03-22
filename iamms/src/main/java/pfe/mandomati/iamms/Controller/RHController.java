package pfe.mandomati.iamms.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import pfe.mandomati.iamms.Dto.RHDto;
import pfe.mandomati.iamms.Service.RHService;

@RestController
@RequiredArgsConstructor
@RequestMapping("auth/rh")
public class RHController {

    private final RHService rhService;

    @PostMapping("/register")
    public ResponseEntity<String> registerRH(@RequestBody RHDto rhDto) {
        return rhService.register(rhDto);
    }
}
