# Black Hole

Application Kotlin/Quarkus autoritative de Black Hole. Le socle courant utilise
Java 21, Kotlin 2.3.21 et Quarkus 3.35.1.

## Registre minimal de contenu

Le premier vertical slice durable expose :

```http
PUT /v1/content-registry/{content_hash}
Idempotency-Key: <UUID>
```

`content_hash` est exactement le SHA-256 hexadécimal minuscule, sur 64
caractères, du contenu canonique. Le registre connaît uniquement la présence de
ce hash : il ne stocke ni article, ni occurrence, ni artefact, ni état
d'analyse.

Le header `Idempotency-Key` est obligatoire. Black Hole le valide comme UUID
canonique puis le traite comme une clé opaque, sans dépendance au modèle
Acquisition. Le premier appel d'une opération répond `201 Created` avec `NEW`.
Le replay du même couple clé/hash répond `200 OK` avec `NEW`, le même
`content_id` et le même `registered_at`. Une autre clé sur ce hash reçoit
`200 / KNOWN`; la réutilisation de la même clé pour un autre hash reçoit
`409 Conflict` :

```json
{
  "content_id": "550e8400-e29b-41d4-a716-446655440000",
  "content_hash": "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
  "result": "NEW",
  "registered_at": "2026-09-11T15:00:00Z"
}
```

Un hash ou une clé invalide ou absente reçoit `400 Bad Request`. Une
indisponibilité PostgreSQL reçoit une erreur `503` sûre, sans détail interne.
Le contrat OpenAPI versionné
se trouve dans `src/main/resources/META-INF/openapi.yaml` et est servi par
Quarkus sous `/q/openapi`.

## Atomicité et persistance

La migration Flyway `V1__create_content_registry.sql` crée le schéma
`blackhole` et son unique table métier. La migration append-only V2 ajoute la
clé d'idempotence unique :

```text
content_registry(id UUID, content_hash CHAR(64), idempotency_key UUID, registered_at TIMESTAMPTZ)
```

L'API ouvre une transaction courte en isolation `READ COMMITTED`. Le repository
exécute `INSERT ... ON CONFLICT DO NOTHING RETURNING`; lorsque rien n'est
inséré, une nouvelle instruction lit d'abord l'association de la clé, puis le
gagnant du hash concurrent, avant le commit.
Il n'effectue ni `SELECT` préalable, ni faux `UPDATE`, ni verrou consultatif.

Flyway a été choisi parce qu'il est directement intégré au stack Quarkus/JDBC
de l'application et versionne les migrations SQL sans introduire de second
modèle de persistance. `migrate-at-start` reste désactivé : les migrations sont
exécutées séparément avec le rôle migrateur. Le rôle runtime de l'application
reçoit uniquement `USAGE` sur le schéma ainsi que `SELECT` et `INSERT` sur
`content_registry`.

n8n ne reçoit aucun credential PostgreSQL Black Hole, n'appelle aucune fonction
PostgreSQL Black Hole et utilise uniquement l'API bornée.

## Construction et tests

```shell
./mvnw test
./mvnw package
docker build -f src/main/docker/Dockerfile.jvm -t labia/blackhole-dev:0.2.0 .
docker build -f src/main/docker/Dockerfile.migration -t labia/blackhole-migrations:0.2.0 .
```

La suite lance un PostgreSQL 18.6 isolé avec Testcontainers, applique la vraie
migration avec un rôle migrateur distinct, démarre l'API Quarkus avec le rôle
runtime puis couvre les appels séquentiels et concurrents, les rejets, les
contraintes PostgreSQL, les droits et l'OpenAPI. Le conteneur de test est détruit
à la fin, avec toutes ses données.

Les scénarios BDD durables sont dans
`src/test/resources/features/content-registry.feature`. Le déploiement et la
preuve DEV Lab-IA sont versionnés dans `Lovegiver/lab-ia`, sous
`infrastructure/blackhole/`.

## Configuration runtime

L'application attend les paramètres Quarkus standards suivants :

- `QUARKUS_DATASOURCE_JDBC_URL` ;
- `QUARKUS_DATASOURCE_USERNAME` ;
- `BLACKHOLE_DB_PASSWORD_FILE`, monté comme secret et lu par l'entrypoint ;
- `QUARKUS_HTTP_PORT`, optionnel, `8080` par défaut.

L'image n'expose aucun port hôte par elle-même. Le déploiement Lab-IA la place
uniquement sur le réseau interne DEV.
