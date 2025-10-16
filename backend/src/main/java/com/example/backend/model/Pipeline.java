package com.example.backend.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entité représentant un pipeline de CI/CD composé de plusieurs jobs séquentiels
 */
@Entity
@Table(name = "pipeline")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"jobs", "pipelineRuns"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Pipeline {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp  
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "pipeline",fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private List<Job> jobs = new ArrayList<>();

    @OneToMany(mappedBy = "pipeline",fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("startedAt DESC")
    private List<PipelineRun> pipelineRuns = new ArrayList<>();

    // Méthodes utilitaires
    public void addJob(Job job) {
        jobs.add(job);
        job.setPipeline(this);
    }

    public void removeJob(Job job) {
        jobs.remove(job);
        job.setPipeline(null);
    }

    public void addPipelineRun(PipelineRun pipelineRun) {
        pipelineRuns.add(pipelineRun);
        pipelineRun.setPipeline(this);
    }

    public void removePipelineRun(PipelineRun pipelineRun) {
        pipelineRuns.remove(pipelineRun);
        pipelineRun.setPipeline(null);
    }

}