package com.example.backend.service;

import com.example.backend.model.Job;
import com.example.backend.model.Pipeline;
import com.example.backend.repository.PipelineRepository;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class PipelineService {
    private final PipelineRepository pipelineRepository;

    public PipelineService(PipelineRepository pipelineRepository) {
        this.pipelineRepository = pipelineRepository;
    }

    public List<Pipeline> getPipelines(){
        return pipelineRepository.findAll();

    }

}
