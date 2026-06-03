# CareerTrack Backend

API REST Spring Boot avec PostgreSQL.

## Installation
```bash
mvn spring-boot:run
```

## Configuration
Fichier: `src/main/resources/application.properties`

## Création de la base de données PostgreSQL
```sql
CREATE DATABASE careertrack_db;
CREATE USER postgres WITH PASSWORD 'postgres';
GRANT ALL PRIVILEGES ON DATABASE careertrack_db TO postgres;
```

## Structure
```
├── src/
│   ├── main/
│   │   ├── java/com/careertrack/
│   │   │   └── CareerTrackBackendApplication.java
│   │   └── resources/
│   │       └── application.properties
└── pom.xml
```

## API Endpoints
- `POST /api/auth/login` - Authentification
- `GET /api/users` - Liste des utilisateurs
- `POST /api/users` - Créer un utilisateur


cd "/home/nithadiene/Bureau/nithadiene/COUR ECOLE/DEUXIEME ANNEE/developement mobile FMK/CareerTrack aplication/CarreerTrack frondend" && npm run server