package pfe.mandomati.iamms.Service.Impl;

import pfe.mandomati.iamms.Model.Request;
import pfe.mandomati.iamms.Repository.RequestRepository;
import pfe.mandomati.iamms.Service.RequestService;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j 
@Service
@Transactional
@RequiredArgsConstructor
public class RequestServiceImp implements RequestService {

    private final RequestRepository requestRepository;

    @Override
    public void logRequest(Request request) {
        try {
            log.info("Starting to save request for endpoint: {}", request.getEndpoint());
            log.info("Request details: {}", request);
            
            Request savedRequest = requestRepository.save(request);
            log.info("Request saved successfully with ID: {}", savedRequest.getId());
        } catch (Exception e) {
            log.error("Error while saving request: ", e);
            // Print the full stack trace
            e.printStackTrace();
            throw e;
        }
    }  
}
