package com.example.backend.service;

import com.example.backend.model.Job;
import com.example.backend.model.JobRun;
import com.example.backend.repository.JobRepository;
import com.example.backend.repository.JobRunRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class JobService {
    private final JobRepository jobRepository;
    private final JobRunRepository jobRunRepository;
    public JobService(JobRepository jobRepository, JobRunRepository jobRunRepository){

        this.jobRepository = jobRepository;
        this.jobRunRepository = jobRunRepository;
    }

    public List<Job> getJobs(){
        return jobRepository.findAll();
    }

    public Job getJobById(UUID id){
        return this.jobRepository.findById(id).orElseThrow();
    }

    public List<JobRun> getJobRuns(){
        return this.jobRunRepository.findAll();
    }

    public JobRun getJobRunById(UUID id){
        return this.jobRunRepository.getReferenceById(id);
    }

    public List<JobRun> getJobRunsByJobId(UUID jobId){
        return this.jobRunRepository.findByJobId(jobId);
    }
}
