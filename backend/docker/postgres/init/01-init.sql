-- Script d'initialisation PostgreSQL pour Fluxi
-- Exécuté automatiquement lors du premier démarrage du container

-- Création de la base de données principale (normalement créée par POSTGRES_DB)
-- Mais on s'assure qu'elle existe
SELECT 'CREATE DATABASE fluxi_dev' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'fluxi_dev')\gexec

-- Connexion à la base de données fluxi_dev
\c fluxi_dev

-- Activation des extensions nécessaires
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Création d'un utilisateur spécifique pour les tests (optionnel)
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'fluxi_test') THEN
        CREATE ROLE fluxi_test WITH LOGIN PASSWORD 'fluxi_test';
        GRANT CONNECT ON DATABASE fluxi_dev TO fluxi_test;
        GRANT CREATE ON SCHEMA public TO fluxi_test;
    END IF;
END
$$;

-- Message de confirmation
SELECT 'Base de données Fluxi initialisée avec succès!' as status;