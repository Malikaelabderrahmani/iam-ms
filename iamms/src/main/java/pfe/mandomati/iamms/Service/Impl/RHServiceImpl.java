package pfe.mandomati.iamms.Service.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.transaction.Transactional;
import pfe.mandomati.iamms.Dto.RHDto;
import pfe.mandomati.iamms.Dto.UserDto;
import pfe.mandomati.iamms.Model.RH;
import pfe.mandomati.iamms.Repository.RHRepository;
import pfe.mandomati.iamms.Service.RHService;

@Service
@RequiredArgsConstructor
@Slf4j
public class RHServiceImpl implements RHService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final RHRepository rhRepository;

    @Override
    @Transactional
    @PreAuthorize("hasRole('RH')")
    public ResponseEntity<String> register(RHDto rhDto) {
        boolean exists = rhRepository.existsByUserIdOrCniOrCnssNumber(rhDto.getId() ,rhDto.getCni(), rhDto.getCnssNumber());
        if (exists) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("RH already exists");
        }

        try {
            // 🔹 Construire UserDto à partir de RHDto
            UserDto userDto = UserDto.builder()
                .username(rhDto.getUsername())
                .lastname(rhDto.getLastname())
                .firstname(rhDto.getFirstname())
                .email(rhDto.getEmail())
                .password(rhDto.getPassword())
                .address(rhDto.getAddress())
                .birthDate(rhDto.getBirthDate())
                .city(rhDto.getCity())
                .role(rhDto.getRole())
                .build();

            // 🔹 Envoyer UserDto à IAM-MS (AuthController)
            ResponseEntity<String> response = restTemplate.postForEntity(
                "https://iamms.mandomati.com/api/auth/register", userDto, String.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                return ResponseEntity.status(response.getStatusCode()).body("Failed to register user in IAM-MS");
            }

            // 🔹 Extraire l'ID utilisateur depuis la réponse
            Long userId = extractIdFromResponse(response.getBody());

            // 🔹 Enregistrer les infos spécifiques RH
            saveRhLocally(rhDto, userId);
            
            return ResponseEntity.ok("RH registered successfully");

        } catch (Exception e) {
            log.error("Registration failed for RH: {}", rhDto.getCni(), e);
            throw new RuntimeException("Failed to register RH", e);
        }
    }

    private Long extractIdFromResponse(String responseBody) {
        Pattern pattern = Pattern.compile("ID: (\\d+)");
        Matcher matcher = pattern.matcher(responseBody);
        if (matcher.find()) {
            return Long.parseLong(matcher.group(1));
        } else {
            throw new RuntimeException("Failed to extract ID from response: " + responseBody);
        }
    }

    private void saveRhLocally(RHDto rhDto, Long userId) {
        RH rh = new RH();
        rh.setUserId(userId);
        rh.setCni(rhDto.getCni());
        rh.setHireDate(rhDto.getHireDate());
        rh.setCnssNumber(rhDto.getCnssNumber());
        rh.setPosition(rhDto.getPosition());
        rhRepository.save(rh);
    }
}
