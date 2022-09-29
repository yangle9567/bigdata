package com.bcld.service;

import java.util.List;

import org.apache.commons.configuration.ConfigurationException;

public interface AuthorityCertificationService {
    
    public boolean authority(String module, String userId);

    public List<String> reportType() throws ConfigurationException;
}