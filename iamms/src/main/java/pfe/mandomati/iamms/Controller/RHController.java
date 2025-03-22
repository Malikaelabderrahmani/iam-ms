package pfe.mandomati.iamms.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;

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

    @DeleteMapping("/delete/{rhId}/{username}")
    public ResponseEntity<String> deleteRH(@PathVariable Long rhId, @PathVariable String username) {
        return rhService.delete(rhId, username);
    }

    @PutMapping("/update/{rhId}/{username}")
    public ResponseEntity<String> updateRH(@PathVariable Long rhId, @PathVariable String username, @RequestBody RHDto rhDto) {
        return rhService.update(rhId, username, rhDto);
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllRH() {
        return rhService.getAllRH();
    }

    @GetMapping("profile/{username}")
    public ResponseEntity<?> getRHByUsername(@PathVariable String username) {
        return rhService.getRHByUsername(username);
    }

}
