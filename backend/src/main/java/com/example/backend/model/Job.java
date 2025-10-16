package com.example.backend.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Entité représentant un job individuel exécuté dans un container Docker
 */
@Entity
@Table(name = "job")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"pipeline", "jobRuns"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "image", nullable = false, length = 255)
    private String image;

    @Column(name = "command", nullable = false, columnDefinition = "TEXT")
    private String command;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "env", columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, String> env = new HashMap<>();

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Column(name = "timeout_sec", nullable = false)
    @Builder.Default
    private Integer timeoutSec = 3600; // 1 heure par défaut

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pipeline_id", nullable = false)
    @JsonBackReference
    private Pipeline pipeline;

    @OneToMany(mappedBy = "job", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<JobRun> jobRuns = new ArrayList<>();


    // Méthodes utilitaires
    public void addJobRun(JobRun jobRun) {
        jobRuns.add(jobRun);
        jobRun.setJob(this);
    }

    public void removeJobRun(JobRun jobRun) {
        jobRuns.remove(jobRun);
        jobRun.setJob(null);
    }

    public void addEnvironmentVariable(String key, String value) {
        if (env == null) {
            env = new HashMap<>();
        }
        env.put(key, value);
    }

    public void removeEnvironmentVariable(String key) {
        if (env != null) {
            env.remove(key);
        }
    }

    public String getEnvironmentVariable(String key) {
        return env != null ? env.get(key) : null;
    }

}