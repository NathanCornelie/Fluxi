package com.example.backend.controller;

import com.example.backend.model.Job;
import com.example.backend.model.JobRun;
import com.example.backend.service.JobService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class JobController {

    private final JobService jobService;


    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping("/jobs")
    public List<Job> getJobs() {
        return this.jobService.getJobs();
    }

    @GetMapping("/jobs/{id}")
    public Job getJob(@PathVariable UUID id) {
        return this.jobService.getJobById(id);
    }

    @GetMapping("/jobs/{jobId}/runs")
    public List<JobRun> getJobRuns(@PathVariable UUID jobId) {
        return this.jobService.getJobRunsByJobId(jobId);
    }
}
