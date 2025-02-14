package pfe.mandomati.iamms.Service.Impl;

import org.springframework.beans.factory.annotation.Autowired;

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
            log.debug("Attempting to save request: {}", request);
            requestRepository.save(request);
            log.debug("Successfully saved request");
        } catch (Exception e) {
            log.error("Failed to log request: " + e.getMessage(), e);
            throw e; 
        }
    }  
}
