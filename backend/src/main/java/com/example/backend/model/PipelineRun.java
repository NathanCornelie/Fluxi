package com.example.backend.model;

import com.example.backend.enums.PipelineStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entité représentant une exécution d'un pipeline
 */
@Entity
@Table(name = "pipeline_run")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"pipeline", "jobRuns"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PipelineRun {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pipeline_id", nullable = false)
    private Pipeline pipeline;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private PipelineStatus status = PipelineStatus.PENDING;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @OneToMany(mappedBy = "pipelineRun",fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("startedAt ASC")
    @Builder.Default
    private List<JobRun> jobRuns = new ArrayList<>();

    // Méthodes utilitaires
    public void addJobRun(JobRun jobRun) {
        jobRuns.add(jobRun);
        jobRun.setPipelineRun(this);
    }

    public void removeJobRun(JobRun jobRun) {
        jobRuns.remove(jobRun);
        jobRun.setPipelineRun(null);
    }

    /**
     * Démarre l'exécution du pipeline
     */
    public void start() {
        this.status = PipelineStatus.RUNNING;
        this.startedAt = LocalDateTime.now();
    }

    /**
     * Termine l'exécution du pipeline avec le statut donné
     */
    public void finish(PipelineStatus finalStatus) {
        this.status = finalStatus;
        this.endedAt = LocalDateTime.now();
    }

    /**
     * Annule l'exécution du pipeline
     */
    public void cancel() {
        this.status = PipelineStatus.CANCELLED;
        this.endedAt = LocalDateTime.now();
    }

    /**
     * Calcule la durée d'exécution en secondes
     */
    public Long getDurationInSeconds() {
        if (startedAt == null || endedAt == null) {
            return null;
        }
        return java.time.Duration.between(startedAt, endedAt).getSeconds();
    }

    /**
     * Vérifie si l'exécution est en cours
     */
    public boolean isRunning() {
        return status == PipelineStatus.RUNNING;
    }

    /**
     * Vérifie si l'exécution est terminée
     */
    public boolean isFinished() {
        return status.isFinal();
    }

}