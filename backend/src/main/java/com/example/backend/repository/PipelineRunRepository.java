package com.example.backend.repository;

import com.example.backend.model.PipelineRun;
import org.springframework.data.jpa.repository.JpaRepository;

interface PipelineRunRepository extends JpaRepository<PipelineRun, Long> {
}
