Feature: Registre minimal des content_hash Black Hole

  Scenario: premier enregistrement d'un hash
    Given un content_hash SHA-256 minuscule absent du registre
    And une nouvelle Idempotency-Key UUID
    When un client appelle PUT /v1/content-registry/{content_hash} avec cette clé
    Then l'API répond 201 avec le résultat NEW
    And PostgreSQL contient exactement un enregistrement pour ce hash

  Scenario: rejeu durable de la même opération
    Given un content_hash enregistré avec une Idempotency-Key
    When un client répète le même appel avec la même clé
    Then l'API répond 200 avec le résultat NEW
    And content_id et registered_at sont identiques à la première réponse

  Scenario: autre opération pour un hash connu
    Given un content_hash enregistré avec une Idempotency-Key
    When un client répète l'appel avec une autre clé
    Then l'API répond 200 avec le résultat KNOWN

  Scenario: conflit de réutilisation d'une clé
    Given une Idempotency-Key déjà associée à un content_hash
    When un client l'utilise avec un autre hash
    Then l'API répond 409
    And aucune seconde association n'est créée

  Scenario: concurrence sur le même hash et la même opération
    Given un content_hash absent du registre
    And une Idempotency-Key UUID
    When plusieurs clients l'enregistrent simultanément avec cette même clé
    Then exactement un appel reçoit 201
    And tous les appels reçoivent NEW
    And toutes les réponses portent le même content_id et le même registered_at

  Scenario: concurrence sur le même hash avec des opérations distinctes
    Given un content_hash absent du registre
    When plusieurs clients l'enregistrent avec des Idempotency-Key distinctes
    Then exactement une opération reçoit NEW
    And toutes les autres reçoivent KNOWN

  Scenario: concurrence sur la même clé avec des hashes distincts
    Given une Idempotency-Key encore inconnue
    When plusieurs clients l'utilisent simultanément avec des hashes distincts
    Then exactement une association reçoit NEW
    And toutes les autres reçoivent 409

  Scenario: concurrence sur des hashes distincts
    Given plusieurs content_hash valides et absents
    When les clients les enregistrent simultanément
    Then chaque appel reçoit NEW
    And chaque hash possède un content_id distinct

  Scenario: rejet d'un hash invalide
    Given un hash qui n'est pas un SHA-256 hexadécimal minuscule
    When un client appelle l'endpoint borné
    Then l'API répond 400
    And aucun enregistrement n'est créé

  Scenario: rejet d'une clé absente ou invalide
    Given un content_hash valide
    When le header Idempotency-Key est absent ou n'est pas un UUID canonique
    Then l'API répond 400
    And aucun enregistrement n'est créé

  Scenario: moindre privilège du runtime
    Given le rôle runtime Black Hole
    Then il peut lire et insérer dans content_registry
    But PostgreSQL lui refuse UPDATE, DELETE et DDL

  Scenario: persistance après redémarrage
    Given un hash enregistré par l'API DEV
    When l'API Black Hole redémarre
    And le client répète le même appel avec la même Idempotency-Key
    Then l'API répond NEW avec le même content_id et le même registered_at

  Scenario: frontière avec n8n
    Given la base privée Black Hole
    Then n8n ne possède aucun droit de connexion à cette base
    And n8n utilise uniquement PUT /v1/content-registry/{content_hash} avec une Idempotency-Key
