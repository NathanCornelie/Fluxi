package com.example.backend.model;

import com.example.backend.enums.JobStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entité représentant l'exécution d'un job individuel avec logs et statut
 */
@Entity
@Table(name = "job_run")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"job", "pipelineRun"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class JobRun {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pipeline_run_id", nullable = false)
    @JsonBackReference
    private PipelineRun pipelineRun;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private JobStatus status = JobStatus.PENDING;

    @Lob
    @Column(name = "logs", columnDefinition = "TEXT")
    @Builder.Default
    private String logs = "";

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    // Méthodes utilitaires

    /**
     * Démarre l'exécution du job
     */
    public void start() {
        this.status = JobStatus.RUNNING;
        this.startedAt = LocalDateTime.now();
    }

    /**
     * Termine l'exécution du job avec le statut donné
     */
    public void finish(JobStatus finalStatus) {
        this.status = finalStatus;
        this.endedAt = LocalDateTime.now();
    }

    /**
     * Ajoute une ligne de log
     */
    public void appendLog(String logLine) {
        if (logs == null) {
            logs = "";
        }
        if (!logs.isEmpty() && !logs.endsWith("\n")) {
            logs += "\n";
        }
        logs += logLine;
    }

    /**
     * Ajoute plusieurs lignes de logs
     */
    public void appendLogs(String newLogs) {
        if (newLogs == null || newLogs.trim().isEmpty()) {
            return;
        }
        
        if (logs == null) {
            logs = "";
        }
        
        if (!logs.isEmpty() && !logs.endsWith("\n") && !newLogs.startsWith("\n")) {
            logs += "\n";
        }
        logs += newLogs;
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
        return status == JobStatus.RUNNING;
    }

    /**
     * Vérifie si l'exécution est terminée
     */
    public boolean isFinished() {
        return status.isFinal();
    }

    /**
     * Vérifie si l'exécution a réussi
     */
    public boolean isSuccessful() {
        return status == JobStatus.SUCCESS;
    }

    /**
     * Vérifie si l'exécution a échoué
     */
    public boolean isFailed() {
        return status == JobStatus.FAILED;
    }

    // equals, hashCode et toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JobRun jobRun = (JobRun) o;
        return Objects.equals(id, jobRun.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "JobRun{" +
                "id=" + id +
                ", jobId=" + (job != null ? job.getId() : null) +
                ", pipelineRunId=" + (pipelineRun != null ? pipelineRun.getId() : null) +
                ", status=" + status +
                ", startedAt=" + startedAt +
                ", endedAt=" + endedAt +
                ", logsLength=" + (logs != null ? logs.length() : 0) +
                '}';
    }
}