# Fluxi Backend - Documentation Base de Données

## 🏗️ Architecture

Fluxi utilise PostgreSQL comme base de données principale avec Flyway pour la gestion des migrations et Spring Boot JPA pour l'ORM.

### Stack Technique
- **Base de données** : PostgreSQL 15+
- **ORM** : Spring Data JPA + Hibernate 6.x  
- **Migrations** : Flyway
- **Connection Pool** : HikariCP
- **Containerisation** : Docker Compose

---

## 🚀 Démarrage Rapide

### 1. Prérequis
- Docker & Docker Compose installés
- Java 25+ (ou version définie dans le projet)
- Maven 3.9+

### 2. Lancement de la base de données
```bash
# Démarrer PostgreSQL + services auxiliaires
docker-compose -f docker-compose.dev.yml up -d postgres

# Vérifier que PostgreSQL est prêt
docker-compose -f docker-compose.dev.yml logs postgres

# Démarrer tous les services (postgres, pgadmin, rabbitmq, redis)
docker-compose -f docker-compose.dev.yml up -d
```

### 3. Lancement de l'application Spring Boot
```bash
# Compilation et démarrage (Flyway exécutera automatiquement les migrations)
./mvnw spring-boot:run

# Ou avec profil spécifique
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### 4. Vérification
- **Application** : http://localhost:8080
- **Base de données** : `psql -h localhost -p 5432 -U fluxi -d fluxi_dev`
- **PgAdmin** : http://localhost:8080 (dev@fluxi.com / fluxi123)
- **RabbitMQ Management** : http://localhost:15672 (fluxi / fluxi)

---

## 📊 Schéma de Base de Données

### Entités Principales

#### Pipeline
Représente un pipeline de CI/CD composé de plusieurs jobs.
```sql
CREATE TABLE pipeline (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

#### Job  
Jobs individuels exécutés dans des containers Docker.
```sql
CREATE TABLE job (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    image VARCHAR(255) NOT NULL,
    command TEXT NOT NULL,
    env JSONB DEFAULT '{}',
    order_index INTEGER NOT NULL,
    timeout_sec INTEGER NOT NULL DEFAULT 3600,
    pipeline_id UUID NOT NULL REFERENCES pipeline(id) ON DELETE CASCADE
);
```

#### PipelineRun
Historique d'exécution des pipelines.
```sql
CREATE TABLE pipeline_run (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pipeline_id UUID NOT NULL REFERENCES pipeline(id) ON DELETE CASCADE,
    status pipeline_status_enum NOT NULL DEFAULT 'PENDING',
    started_at TIMESTAMPTZ,
    ended_at TIMESTAMPTZ
);
```

#### JobRun
Historique détaillé d'exécution des jobs avec logs.
```sql
CREATE TABLE job_run (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    job_id UUID NOT NULL REFERENCES job(id) ON DELETE CASCADE,
    pipeline_run_id UUID NOT NULL REFERENCES pipeline_run(id) ON DELETE CASCADE,
    status job_status_enum NOT NULL DEFAULT 'PENDING',
    logs TEXT DEFAULT '',
    started_at TIMESTAMPTZ,
    ended_at TIMESTAMPTZ
);
```

### Types ENUM
```sql
CREATE TYPE pipeline_status_enum AS ENUM ('PENDING','RUNNING','SUCCESS','FAILED','CANCELLED');
CREATE TYPE job_status_enum AS ENUM ('PENDING','RUNNING','SUCCESS','FAILED');
```

---

## 🔧 Gestion et Maintenance

### Migrations Flyway

#### Exécuter les migrations manuellement
```bash
# Informations sur les migrations
./mvnw flyway:info

# Validation des migrations
./mvnw flyway:validate

# Exécution des migrations pending
./mvnw flyway:migrate

# Nettoyage complet (DÉVELOPPEMENT UNIQUEMENT)
./mvnw flyway:clean
```

#### Créer une nouvelle migration
1. Créer un fichier : `src/main/resources/db/migration/V{VERSION}__{description}.sql`
2. Exemple : `V2__add_user_table.sql`

### Réinitialisation de la Base de Données

#### Méthode 1 : Reset via Docker (recommandée)
```bash
# Arrêter et supprimer les containers + volumes
docker-compose -f docker-compose.dev.yml down -v

# Redémarrer tout
docker-compose -f docker-compose.dev.yml up -d
./mvnw spring-boot:run
```

#### Méthode 2 : Reset via Flyway (développement uniquement)
```bash
./mvnw flyway:clean  # ⚠️ Supprime TOUTES les données
./mvnw flyway:migrate
```

### Accès Direct à PostgreSQL

#### Via psql
```bash
# Accès direct
psql -h localhost -p 5432 -U fluxi -d fluxi_dev

# Via Docker
docker exec -it fluxi-postgres-dev psql -U fluxi -d fluxi_dev

# Commandes utiles
\dt         # Lister les tables
\d pipeline # Décrire la table pipeline
\q          # Quitter
```

#### Via PgAdmin
- **URL** : http://localhost:8080
- **Email** : dev@fluxi.com  
- **Password** : fluxi123
- Serveur pré-configuré : "Fluxi Development"

---

## 🧪 Tests

### Tests d'Intégration avec Testcontainers

```bash
# Exécuter tous les tests (lance automatiquement Testcontainers)
./mvnw test

# Tests spécifiques à la couche repository
./mvnw test -Dtest="*Repository*"

# Tests avec profil test 
./mvnw test -Dspring.profiles.active=test
```

### Configuration des Tests
Les tests utilisent :
- **Testcontainers** pour les tests d'intégration PostgreSQL
- **H2 en mémoire** pour les tests unitaires rapides  
- **Profil `test`** avec configuration dédiée dans `application-test.yml`

---

## 📝 Configuration par Environnement

### Développement (`application-dev.yml`)
- Base : `jdbc:postgresql://localhost:5432/fluxi_dev`
- Logs SQL activés
- Flyway clean autorisé

### Test (`application-test.yml`)  
- H2 en mémoire ou Testcontainers
- Flyway désactivé (create-drop)
- Logs minimaux

### Production (`application-prod.yml`)
- Variables d'environnement
- Flyway clean interdit
- Pool de connexions optimisé
- Logs vers fichier

---

## 🚨 Résolution de Problèmes

### PostgreSQL ne démarre pas
```bash
# Vérifier les logs Docker
docker-compose -f docker-compose.dev.yml logs postgres

# Nettoyer les volumes corrompus  
docker-compose -f docker-compose.dev.yml down -v
docker system prune -a
```

### Erreurs de Migration Flyway
```bash
# Voir l'état des migrations
./mvnw flyway:info

# Réparer une migration corrompue (dev uniquement)
./mvnw flyway:repair

# Reset complet si nécessaire
./mvnw flyway:clean && ./mvnw flyway:migrate
```

### Problèmes de Connexion
1. Vérifier que PostgreSQL est démarré : `docker ps`
2. Tester la connexion : `psql -h localhost -p 5432 -U fluxi -d fluxi_dev`
3. Vérifier les configurations dans `application-dev.yml`
4. Consulter les logs de l'application : `./mvnw spring-boot:run`

---

## 📚 Ressources Utiles

- [Documentation Flyway](https://flywaydb.org/documentation/)
- [Spring Data JPA Reference](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Testcontainers Guide](https://www.testcontainers.org/quickstart/junit_5_quickstart/)

---

## 🤝 Contribution

### Conventions de Nommage
- **Tables** : snake_case (ex: `pipeline_run`)
- **Colonnes** : snake_case (ex: `created_at`)  
- **Migrations** : `V{VERSION}__{description}.sql`
- **Entités JPA** : PascalCase (ex: `PipelineRun`)

### Bonnes Pratiques
- Toujours créer un index sur les clés étrangères
- Utiliser `TIMESTAMPTZ` pour les dates
- Documenter les colonnes complexes (JSONB, etc.)
- Tester les migrations sur un dataset réaliste
- Préférer les UUID pour les identifiants