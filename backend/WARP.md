# Fluxi - Orchestrateur de Jobs CI/CD

## 1. Description du projet

**Fluxi** est une plateforme web qui permet de créer, orchestrer et monitorer des pipelines de jobs automatisés, à la manière d'un mini Jenkins ou GitLab CI/CD. L'application cible à la fois le côté développement et DevOps, avec un accent sur la modularité, le streaming en temps réel des logs et l'exécution sécurisée des jobs dans des conteneurs Docker.

### Objectifs clés
- Permettre aux utilisateurs de créer et gérer des pipelines composés de plusieurs jobs.
- Lancer des pipelines et suivre l'exécution en temps réel.
- Stocker l'historique des exécutions et des logs.
- Supporter l'exécution de jobs dans des containers isolés.
- Préparer la plateforme pour une future évolution vers Kubernetes et une planification de jobs récurrents.

### Stack technique
- **Front-end** : Angular 16+
- **Back-end** : Spring Boot 3.x
- **Base de données** : PostgreSQL 15+
- **Orchestration des jobs** : Docker containers
- **Communication temps réel** : WebSocket ou Server-Sent Events (SSE)
- **Messaging / Queue** : RabbitMQ (pour gérer la file de jobs)

---

## 2. Schéma de données

Le schéma de données permet de gérer les pipelines, les jobs et les exécutions avec les logs associés.

### Entités principales

#### 2.1 Pipeline
| Colonne       | Type         | Description |
|---------------|-------------|-------------|
| id            | UUID        | Identifiant unique |
| name          | VARCHAR(255)| Nom lisible du pipeline |
| description   | TEXT        | Description du pipeline |
| created_at    | TIMESTAMP   | Date de création |
| updated_at    | TIMESTAMP   | Date de dernière modification |

#### 2.2 Job
| Colonne       | Type        | Description |
|---------------|------------|-------------|
| id            | UUID       | Identifiant unique |
| name          | VARCHAR(255)| Nom du job |
| image         | VARCHAR(255)| Image Docker à utiliser |
| command       | TEXT       | Commande à exécuter dans le container |
| env           | JSONB      | Variables d'environnement |
| order_index   | INT        | Ordre d'exécution dans le pipeline |
| timeout_sec   | INT        | Temps maximum d'exécution en secondes |
| pipeline_id   | FK → pipeline.id | Lien avec le pipeline parent |

#### 2.3 PipelineRun
| Colonne       | Type        | Description |
|---------------|------------|-------------|
| id            | UUID       | Identifiant de l'exécution du pipeline |
| pipeline_id   | FK → pipeline.id | Pipeline exécuté |
| status        | ENUM       | PENDING, RUNNING, SUCCESS, FAILED, CANCELLED |
| started_at    | TIMESTAMP  | Début de l'exécution |
| ended_at      | TIMESTAMP  | Fin de l'exécution |

#### 2.4 JobRun
| Colonne           | Type        | Description |
|------------------|------------|-------------|
| id                | UUID       | Identifiant unique de l'exécution du job |
| job_id            | FK → job.id | Job exécuté |
| pipeline_run_id   | FK → pipeline_run.id | PipelineRun parent |
| status            | ENUM       | PENDING, RUNNING, SUCCESS, FAILED |
| logs              | TEXT / CLOB | Logs collectés en temps réel |
| started_at        | TIMESTAMP  | Début d'exécution |
| ended_at          | TIMESTAMP  | Fin d'exécution |

### Relations principales
- Un **Pipeline** contient plusieurs **Jobs**.
- Chaque exécution d’un pipeline crée un **PipelineRun**.
- Chaque job dans le pipeline crée un ou plusieurs **JobRun**.
- Les logs de chaque JobRun sont transmis en temps réel via WebSocket au front.

---

Ce document sert de référence pour le développement backend et frontend, ainsi que pour générer des modèles, services et API REST dans le projet PipeForge.