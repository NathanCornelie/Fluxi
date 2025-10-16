-- Fluxi - Migration initiale du schéma de base de données
-- Version: V1
-- Description: Création des tables principales : pipeline, job, pipeline_run, job_run

-- Activation de l'extension pgcrypto pour la génération d'UUID
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Types ENUM pour les statuts
CREATE TYPE pipeline_status_enum AS ENUM (
    'PENDING',
    'RUNNING', 
    'SUCCESS',
    'FAILED',
    'CANCELLED'
);

CREATE TYPE job_status_enum AS ENUM (
    'PENDING',
    'RUNNING',
    'SUCCESS', 
    'FAILED'
);

-- =============================================================================
-- Table : pipeline
-- Description : Définit un pipeline composé de plusieurs jobs
-- =============================================================================
CREATE TABLE pipeline (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Contraintes
    CONSTRAINT pipeline_name_not_empty CHECK (LENGTH(TRIM(name)) > 0)
);

-- Index pour optimiser les recherches par nom
CREATE INDEX idx_pipeline_name ON pipeline(name);
CREATE INDEX idx_pipeline_created_at ON pipeline(created_at DESC);

-- =============================================================================
-- Table : job
-- Description : Définit un job individuel au sein d'un pipeline
-- =============================================================================
CREATE TABLE job (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    image VARCHAR(255) NOT NULL,
    command TEXT NOT NULL,
    env JSONB DEFAULT '{}',
    order_index INTEGER NOT NULL,
    timeout_sec INTEGER NOT NULL DEFAULT 3600,
    pipeline_id UUID NOT NULL,
    
    -- Contraintes de clé étrangère
    CONSTRAINT fk_job_pipeline FOREIGN KEY (pipeline_id) 
        REFERENCES pipeline(id) ON DELETE CASCADE,
    
    -- Contraintes de validation
    CONSTRAINT job_name_not_empty CHECK (LENGTH(TRIM(name)) > 0),
    CONSTRAINT job_image_not_empty CHECK (LENGTH(TRIM(image)) > 0),
    CONSTRAINT job_command_not_empty CHECK (LENGTH(TRIM(command)) > 0),
    CONSTRAINT job_order_index_positive CHECK (order_index >= 0),
    CONSTRAINT job_timeout_positive CHECK (timeout_sec > 0),
    
    -- Contrainte d'unicité : un seul job par ordre dans un pipeline
    CONSTRAINT unique_pipeline_order UNIQUE (pipeline_id, order_index)
);

-- Index pour optimiser les requêtes
CREATE INDEX idx_job_pipeline_id ON job(pipeline_id);
CREATE INDEX idx_job_pipeline_order ON job(pipeline_id, order_index);
CREATE INDEX idx_job_name ON job(name);

-- =============================================================================
-- Table : pipeline_run
-- Description : Enregistre une exécution d'un pipeline
-- =============================================================================
CREATE TABLE pipeline_run (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pipeline_id UUID NOT NULL,
    status pipeline_status_enum NOT NULL DEFAULT 'PENDING',
    started_at TIMESTAMPTZ,
    ended_at TIMESTAMPTZ,
    
    -- Contraintes de clé étrangère
    CONSTRAINT fk_pipeline_run_pipeline FOREIGN KEY (pipeline_id) 
        REFERENCES pipeline(id) ON DELETE CASCADE,
    
    -- Contraintes de validation
    CONSTRAINT pipeline_run_end_after_start CHECK (
        ended_at IS NULL OR started_at IS NULL OR ended_at >= started_at
    )
);

-- Index pour optimiser les requêtes de monitoring et d'historique
CREATE INDEX idx_pipeline_run_pipeline_id ON pipeline_run(pipeline_id);
CREATE INDEX idx_pipeline_run_status ON pipeline_run(status);
CREATE INDEX idx_pipeline_run_started_at ON pipeline_run(started_at DESC);
CREATE INDEX idx_pipeline_run_ended_at ON pipeline_run(ended_at DESC);

-- =============================================================================
-- Table : job_run
-- Description : Enregistre l'exécution d'un job individuel
-- =============================================================================
CREATE TABLE job_run (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    job_id UUID NOT NULL,
    pipeline_run_id UUID NOT NULL,
    status job_status_enum NOT NULL DEFAULT 'PENDING',
    logs TEXT DEFAULT '',
    started_at TIMESTAMPTZ,
    ended_at TIMESTAMPTZ,
    
    -- Contraintes de clé étrangère
    CONSTRAINT fk_job_run_job FOREIGN KEY (job_id) 
        REFERENCES job(id) ON DELETE CASCADE,
    CONSTRAINT fk_job_run_pipeline_run FOREIGN KEY (pipeline_run_id) 
        REFERENCES pipeline_run(id) ON DELETE CASCADE,
    
    -- Contraintes de validation
    CONSTRAINT job_run_end_after_start CHECK (
        ended_at IS NULL OR started_at IS NULL OR ended_at >= started_at
    ),
    
    -- Contrainte d'unicité : un seul job_run par job et pipeline_run
    CONSTRAINT unique_job_pipeline_run UNIQUE (job_id, pipeline_run_id)
);

-- Index pour optimiser les requêtes
CREATE INDEX idx_job_run_job_id ON job_run(job_id);
CREATE INDEX idx_job_run_pipeline_run_id ON job_run(pipeline_run_id);
CREATE INDEX idx_job_run_status ON job_run(status);
CREATE INDEX idx_job_run_started_at ON job_run(started_at DESC);
CREATE INDEX idx_job_run_ended_at ON job_run(ended_at DESC);

-- =============================================================================
-- Triggers pour la mise à jour automatique des timestamps
-- =============================================================================

-- Fonction générique pour mettre à jour updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Trigger pour la table pipeline
CREATE TRIGGER trigger_pipeline_updated_at
    BEFORE UPDATE ON pipeline
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- =============================================================================
-- Données de test/exemple (optionnel pour développement)
-- =============================================================================

-- Pipeline d'exemple pour le développement
INSERT INTO pipeline (id, name, description) VALUES 
(
    'b0f7c8e4-1a2b-4c5d-8e9f-123456789abc',
    'Build & Deploy Example', 
    'Pipeline d''exemple avec build, tests et déploiement'
);

-- Jobs d'exemple
INSERT INTO job (name, image, command, order_index, timeout_sec, pipeline_id) VALUES
(
    'Build Application',
    'maven:3.9-openjdk-17',
    'mvn clean compile',
    0,
    600,
    'b0f7c8e4-1a2b-4c5d-8e9f-123456789abc'
),
(
    'Run Tests',
    'maven:3.9-openjdk-17', 
    'mvn test',
    1,
    900,
    'b0f7c8e4-1a2b-4c5d-8e9f-123456789abc'
),
(
    'Package Application',
    'maven:3.9-openjdk-17',
    'mvn package -DskipTests',
    2,
    300,
    'b0f7c8e4-1a2b-4c5d-8e9f-123456789abc'
);

-- =============================================================================
-- Commentaires sur les tables pour la documentation
-- =============================================================================

COMMENT ON TABLE pipeline IS 'Définition d''un pipeline de CI/CD composé de plusieurs jobs séquentiels';
COMMENT ON TABLE job IS 'Job individuel exécuté dans un container Docker avec paramètres et ordre d''exécution';
COMMENT ON TABLE pipeline_run IS 'Historique d''exécution d''un pipeline avec statut et horodatage';
COMMENT ON TABLE job_run IS 'Historique d''exécution d''un job avec logs et statut détaillé';

COMMENT ON COLUMN job.env IS 'Variables d''environnement au format JSON pour l''exécution du job';
COMMENT ON COLUMN job.order_index IS 'Ordre d''exécution du job dans le pipeline (0 = premier)';
COMMENT ON COLUMN job.timeout_sec IS 'Timeout d''exécution en secondes (défaut: 1 heure)';
COMMENT ON COLUMN job_run.logs IS 'Logs capturés en temps réel pendant l''exécution du job';