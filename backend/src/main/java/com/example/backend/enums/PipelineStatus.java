package com.example.backend.enums;

/**
 * Statuts possibles pour un pipeline run
 */
public enum PipelineStatus {
    /**
     * Pipeline en attente d'exécution
     */
    PENDING,
    
    /**
     * Pipeline en cours d'exécution
     */
    RUNNING,
    
    /**
     * Pipeline exécuté avec succès
     */
    SUCCESS,
    
    /**
     * Pipeline échoué
     */
    FAILED,
    
    /**
     * Pipeline annulé par l'utilisateur
     */
    CANCELLED;
    
    /**
     * Vérifie si le pipeline est dans un état final
     */
    public boolean isFinal() {
        return this == SUCCESS || this == FAILED || this == CANCELLED;
    }
    
    /**
     * Vérifie si le pipeline est en cours d'exécution
     */
    public boolean isRunning() {
        return this == RUNNING;
    }
}