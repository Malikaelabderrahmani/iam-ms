package pfe.mandomati.iamms.Service.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.Optional;
import java.util.List;
import java.util.Arrays;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpEntity;

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
    public ResponseEntity<String> register(RHDto rhDto) {
        boolean exists = rhRepository.existsByIdOrCniOrCnssNumber(rhDto.getId() ,rhDto.getCni(), rhDto.getCnssNumber());
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

            //  Envoyer UserDto à IAM-MS (AuthController)
            ResponseEntity<String> response = restTemplate.postForEntity(
                "https://iamms.mandomati.com/api/auth/register", userDto, String.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                return ResponseEntity.status(response.getStatusCode()).body("Failed to register user in IAM-MS");
            }

            //  Extraire l'ID utilisateur depuis la réponse
            Long userId = extractIdFromResponse(response.getBody());

            //  Enregistrer les infos spécifiques RH
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
        rh.setId(userId);
        rh.setCni(rhDto.getCni());
        rh.setHireDate(rhDto.getHireDate());
        rh.setCnssNumber(rhDto.getCnssNumber());
        rh.setPosition(rhDto.getPosition());
        rhRepository.save(rh);
    }

    @Override
    @Transactional
    public ResponseEntity<String> delete(Long rhId, String username) {
        try {
            // 1️ Supprimer l'utilisateur de IAM-MS
            String deleteUrl = "https://iamms.mandomati.com/api/auth/user/delete/" + username;
            ResponseEntity<String> deleteResponse = restTemplate.exchange(
                deleteUrl, HttpMethod.DELETE, null, String.class
            );

            if (!deleteResponse.getStatusCode().is2xxSuccessful()) {
                return ResponseEntity.status(deleteResponse.getStatusCode())
                        .body("Failed to delete user from IAM-MS");
            }

            log.info("User {} deleted successfully from IAM-MS", username);

            // 2️ Supprimer le RH de la base locale
            rhRepository.deleteById(rhId);
            log.info("RH with ID {} deleted from local database", rhId);

            return ResponseEntity.ok("RH deleted successfully from IAM-MS and local database");

        } catch (Exception e) {
            log.error("Error occurred while deleting RH", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error occurred while processing request");
        }
    }

    @Override
    @Transactional
    public ResponseEntity<String> update(Long rhId, String username, RHDto rhDto) {
        // Vérifier si le RH existe en base locale
        Optional<RH> optionalRH = rhRepository.findById(rhId);
        if (optionalRH.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("RH not found");
        }

        RH rh = optionalRH.get();

        try {
            // 1️ Construire le UserDto pour IAM-MS
            UserDto userDto = UserDto.builder()
                .username(username)
                .lastname(rhDto.getLastname())
                .firstname(rhDto.getFirstname())
                .email(rhDto.getEmail())
                .address(rhDto.getAddress())
                .birthDate(rhDto.getBirthDate())
                .city(rhDto.getCity())
                .role(rhDto.getRole())  // Associer le rôle s'il est fourni
                .build();

        // 2️ Mettre à jour IAM-MS
        String editUrl = "https://iamms.mandomati.com/api/auth/user/edit/" + username;
        ResponseEntity<String> editResponse = restTemplate.exchange(
                editUrl, HttpMethod.PUT, new HttpEntity<>(userDto), String.class
        );

        if (!editResponse.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.status(editResponse.getStatusCode())
                    .body("Failed to update RH in IAM-MS");
        }

        log.info("RH {} updated successfully in IAM-MS", username);

        // 3️⃣ Mettre à jour la base locale (uniquement les champs spécifiques)
        rh.setCni(rhDto.getCni());
        rh.setHireDate(rhDto.getHireDate());
        rh.setCnssNumber(rhDto.getCnssNumber());
        rh.setPosition(rhDto.getPosition());

        rhRepository.save(rh);
        log.info("RH with ID {} updated locally", rhId);

        return ResponseEntity.ok("RH updated successfully in IAM-MS and local database");

        } catch (Exception e) {
            log.error("Error occurred while updating RH", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error while processing request");
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> getAllRH() {
        try {
            // 1️ Récupérer les RH depuis IAM-MS
            String iamMsUrl = "https://iamms.mandomati.com/api/auth/user/role/rh";
            ResponseEntity<UserDto[]> iamResponse = restTemplate.getForEntity(iamMsUrl, UserDto[].class);

            if (!iamResponse.getStatusCode().is2xxSuccessful() || iamResponse.getBody() == null) {
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("Failed to retrieve RH from IAM-MS");
            }

            List<UserDto> iamRHList = Arrays.asList(iamResponse.getBody());

            // 2️ Récupérer les RH depuis la base locale
            List<RH> localRHList = rhRepository.findAll();

            // 3️ Mapper et fusionner les données IAM-MS et locales
            List<RHDto> rhDtos = iamRHList.stream().map(userDto -> {
                Optional<RH> rhOptional = localRHList.stream()
                    .filter(rh -> rh.getId().equals(userDto.getId()))
                    .findFirst();

            return RHDto.builder()
                    .id(rhOptional.map(RH::getId).orElse(null))
                    .username(userDto.getUsername())
                    .lastname(userDto.getLastname())
                    .firstname(userDto.getFirstname())
                    .email(userDto.getEmail())
                    // .role(userDto.getRole())
                    .address(userDto.getAddress())
                    .birthDate(userDto.getBirthDate())
                    .city(userDto.getCity())
                    // .createdAt(userDto.getCreatedAt())
                    .cni(rhOptional.map(RH::getCni).orElse(null))
                    .hireDate(rhOptional.map(RH::getHireDate).orElse(null))
                    .cnssNumber(rhOptional.map(RH::getCnssNumber).orElse(null))
                    .position(rhOptional.map(RH::getPosition).orElse(null))
                    .build();
            }).collect(Collectors.toList());

        return ResponseEntity.ok(rhDtos);

        } catch (Exception e) {
            log.error("Error occurred while retrieving RH list", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error while processing request");
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> getRHByUsername(String username) {
        try {
            // 1️ Récupérer les infos de l'utilisateur depuis IAM-MS
            String iamMsUrl = "https://iamms.mandomati.com/api/auth/user/profile/" + username;
            ResponseEntity<UserDto> iamResponse = restTemplate.getForEntity(iamMsUrl, UserDto.class);

            if (!iamResponse.getStatusCode().is2xxSuccessful() || iamResponse.getBody() == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("RH not found in IAM-MS");
            }

            UserDto iamRH = iamResponse.getBody();
            Long userId = iamRH.getId(); // Récupérer l'ID de l'utilisateur

            // 2️ Récupérer les infos du RH dans la base locale en utilisant l'ID
            Optional<RH> localRHOptional = rhRepository.findById(userId);

            // 3️ Fusionner les données
            RHDto rhDto = RHDto.builder()
                .id(userId) // Utiliser l'ID récupéré de IAM-MS
                .username(iamRH.getUsername())
                .lastname(iamRH.getLastname())
                .firstname(iamRH.getFirstname())
                .email(iamRH.getEmail())
                .address(iamRH.getAddress())
                .birthDate(iamRH.getBirthDate())
                .city(iamRH.getCity())
                .cni(localRHOptional.map(RH::getCni).orElse(null))
                .hireDate(localRHOptional.map(RH::getHireDate).orElse(null))
                .cnssNumber(localRHOptional.map(RH::getCnssNumber).orElse(null))
                .position(localRHOptional.map(RH::getPosition).orElse(null))
                .build();

            return ResponseEntity.ok(rhDto);

        } catch (Exception e) {
            log.error("Error occurred while retrieving RH by username: {}", username, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error while processing request");
        }
    }









}
