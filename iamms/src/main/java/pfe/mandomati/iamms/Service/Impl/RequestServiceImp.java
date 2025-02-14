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

    @Autowired
    private RequestRepository requestRepository;

    @Override
    public void logRequest(Request request) {
        try {
            requestRepository.save(request);
        } catch (Exception e) {
            log.error("Failed to log request: " + e.getMessage(), e);
        }
    }  
}
