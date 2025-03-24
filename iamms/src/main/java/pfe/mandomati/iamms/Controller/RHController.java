package pfe.mandomati.iamms.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
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

    @GetMapping("/profile")
    public ResponseEntity<?> getRHFromToken(@RequestHeader("Authorization") String authorizationHeader) {
            // Extraire le token "Bearer <token>" et récupérer le token
            String token = authorizationHeader.replace("Bearer ", "");

            // Appeler le service avec le token extrait
            return rhService.getRHByToken(token);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getRHById(@PathVariable Long id) {
        return rhService.getRHById(id);
    }

}
