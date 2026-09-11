# Validation du registre de contenu

## Contrat

La source BDD est
`src/test/resources/features/content-registry.feature`. Les tests JUnit
`ContentRegistryResourceTest` exercent la ressource HTTP réelle et vérifient les
effets par des lectures JDBC indépendantes.

## Preuve reproductible

```shell
./mvnw test
```

Le test démarre l'image PostgreSQL 18.6 épinglée utilisée par Lab-IA DEV. Il
crée des rôles migrateur et runtime séparés, applique la migration Flyway puis
vérifie :

- `NEW / 201`, puis `KNOWN / 200` avec identifiant et date stables ;
- exactement un `NEW` sous concurrence sur le même hash ;
- des identifiants distincts sous concurrence sur des hashes distincts ;
- le rejet d'un hash invalide sans insertion ;
- l'unicité PostgreSQL et le refus effectif de `UPDATE`, `DELETE` et DDL ;
- la présence du contrat dans l'OpenAPI servi par l'application.

Le scénario de redémarrage et l'absence de droit DB pour n8n sont validés par
la preuve DEV de `Lovegiver/lab-ia`, car ils relèvent de la topologie réelle et
non du seul processus applicatif.
