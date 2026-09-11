Feature: Registre minimal des content_hash Black Hole

  Scenario: premier enregistrement d'un hash
    Given un content_hash SHA-256 minuscule absent du registre
    When un client appelle PUT /v1/content-registry/{content_hash}
    Then l'API répond 201 avec le résultat NEW
    And PostgreSQL contient exactement un enregistrement pour ce hash

  Scenario: rejeu durable du même hash
    Given un content_hash déjà enregistré
    When un client répète le même appel
    Then l'API répond 200 avec le résultat KNOWN
    And content_id et registered_at sont identiques à la première réponse

  Scenario: concurrence sur le même hash
    Given un content_hash absent du registre
    When plusieurs clients l'enregistrent simultanément
    Then exactement un appel reçoit NEW
    And tous les autres appels reçoivent KNOWN
    And toutes les réponses portent le même content_id et le même registered_at

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

  Scenario: moindre privilège du runtime
    Given le rôle runtime Black Hole
    Then il peut lire et insérer dans content_registry
    But PostgreSQL lui refuse UPDATE, DELETE et DDL

  Scenario: persistance après redémarrage
    Given un hash enregistré par l'API DEV
    When l'API Black Hole redémarre
    And le client répète le même appel
    Then l'API répond KNOWN avec le même content_id et le même registered_at

  Scenario: frontière avec n8n
    Given la base privée Black Hole
    Then n8n ne possède aucun droit de connexion à cette base
    And n8n utilise uniquement PUT /v1/content-registry/{content_hash}
