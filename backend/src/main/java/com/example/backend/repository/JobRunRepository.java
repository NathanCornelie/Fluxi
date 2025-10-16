package com.example.backend.repository;

import com.example.backend.model.JobRun;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JobRunRepository extends JpaRepository<JobRun, UUID> {
    List<JobRun> findByJobId(UUID jobId);
}
