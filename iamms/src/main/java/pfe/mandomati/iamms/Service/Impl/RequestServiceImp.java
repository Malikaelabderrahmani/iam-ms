package pfe.mandomati.iamms.Service.Impl;

import org.springframework.beans.factory.annotation.Autowired;

import pfe.mandomati.iamms.Model.Request;
import pfe.mandomati.iamms.Repository.RequestRepository;
import pfe.mandomati.iamms.Service.RequestService;

import org.springframework.stereotype.Service;

@Service
public class RequestServiceImp implements RequestService {

    @Autowired
    private RequestRepository requestRepository;

    @Override
    public void logRequest(Request request) {
        requestRepository.save(request);
    }
    
}
