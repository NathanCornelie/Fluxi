package com.example.backend.enums;

/**
 * Statuts possibles pour un job run
 */
public enum JobStatus {
    /**
     * Job en attente d'exécution
     */
    PENDING,
    
    /**
     * Job en cours d'exécution
     */
    RUNNING,
    
    /**
     * Job exécuté avec succès
     */
    SUCCESS,
    
    /**
     * Job échoué
     */
    FAILED;
    
    /**
     * Vérifie si le job est dans un état final
     */
    public boolean isFinal() {
        return this == SUCCESS || this == FAILED;
    }
    
    /**
     * Vérifie si le job est en cours d'exécution
     */
    public boolean isRunning() {
        return this == RUNNING;
    }
}