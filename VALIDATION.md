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

Le rejeu du 12 septembre 2026 est `PASS` : 68 tests, 0 échec, avec la
migration V2 appliquée sur PostgreSQL 18.6 Testcontainers.

Le test démarre l'image PostgreSQL 18.6 épinglée utilisée par Lab-IA DEV. Il
crée des rôles migrateur et runtime séparés, applique la migration Flyway puis
vérifie :

- `NEW / 201`, puis `NEW / 200` au replay de la même clé et du même hash,
  avec identifiant et date stables ;
- `KNOWN / 200` pour une autre clé sur le même hash ;
- `409` lorsque la même clé est présentée avec un autre hash ;
- exactement une création sous concurrence même clé/hash, avec uniquement des
  résultats métier `NEW` ;
- exactement un `NEW` sous concurrence de clés distinctes sur le même hash ;
- exactement une association sous concurrence d'une même clé avec des hashes
  distincts, les perdants recevant `409` ;
- des identifiants distincts sous concurrence sur des hashes distincts ;
- le rejet d'un hash ou d'une clé invalide ou absente sans insertion ;
- l'unicité PostgreSQL et le refus effectif de `UPDATE`, `DELETE` et DDL ;
- la présence du contrat dans l'OpenAPI servi par l'application.

Le scénario de redémarrage et l'absence de droit DB pour n8n sont validés par
la preuve DEV de `Lovegiver/lab-ia`, car ils relèvent de la topologie réelle et
non du seul processus applicatif.
