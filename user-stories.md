# SuperviseMe — User Stories

## Vision du produit

SuperviseMe est une application SaaS permettant aux dirigeants de TPE et PME de piloter leur activité depuis une interface centralisée.

L'application utilise le terme générique « dossier » pour s'adapter à différents secteurs professionnels.

## Rôles

- ADMIN : dirigeant ou administrateur de l'entreprise.
- UTILISATEUR : collaborateur invité par un administrateur.
- VISITEUR : personne non authentifiée utilisant le formulaire public.

## P0 — Fondations et sécurité

### US-001 — Gestion des entreprises
**En tant qu'** administrateur,
**je veux** disposer d'un espace propre à mon entreprise,
**afin de** gérer mon activité indépendamment des autres entreprises.

Critères d'acceptation :
- Chaque entreprise possède un identifiant UUID unique.
- Une entreprise possède un nom obligatoire.
- Les coordonnées de contact sont facultatives.
- Les dates de création et de modification sont conservées.
- Les données de deux entreprises sont isolées.

Statut : EN COURS

### US-002 — Authentification
**En tant qu'** utilisateur,
**je veux** me connecter de manière sécurisée,
**afin d'** accéder uniquement aux données autorisées.

Critères d'acceptation :
- Connexion et déconnexion.
- Mot de passe stocké sous forme de hachage sécurisé.
- Réinitialisation du mot de passe.
- Protection des routes privées.

Statut : À FAIRE

### US-003 — Gestion des utilisateurs et des rôles
**En tant qu'** administrateur,
**je veux** inviter et gérer mes collaborateurs,
**afin de** contrôler leurs accès.

Critères d'acceptation :
- Deux rôles initiaux : ADMIN et UTILISATEUR.
- L'inscription publique ne donne jamais le rôle ADMIN.
- Seul un ADMIN peut inviter un collaborateur.
- Les autorisations sont vérifiées côté serveur.

Statut : À FAIRE

## P1 — Fonctionnalités principales

### US-004 — Tableau de bord
**En tant qu'** utilisateur,
**je veux** visualiser l'activité de mon entreprise,
**afin de** identifier rapidement les priorités.

Critères d'acceptation :
- Indicateurs d'activité.
- Dossiers et tâches prioritaires.
- Activité récente.
- Interface adaptée aux ordinateurs et mobiles.

Statut : À FAIRE

### US-005 — Gestion des dossiers
**En tant qu'** utilisateur,
**je veux** créer et suivre des dossiers,
**afin de** centraliser le travail de mon entreprise.

Critères d'acceptation :
- Création, consultation et modification.
- Client, description, statut et responsable.
- Photos et commentaires associés.
- Historique des modifications importantes.

Statut : À FAIRE

### US-006 — Tableau Kanban
**En tant qu'** utilisateur,
**je veux** organiser mes dossiers par statut,
**afin de** suivre leur avancement visuellement.

Critères d'acceptation :
- Colonnes représentant les statuts.
- Déplacement des dossiers entre colonnes.
- Enregistrement des changements de statut.
- Statuts configurables à terme.

Statut : À FAIRE

### US-007 — Gestion des tâches
**En tant qu'** utilisateur,
**je veux** gérer des tâches indépendantes ou liées à un dossier,
**afin de** organiser le travail quotidien.

Critères d'acceptation :
- Création et modification d'une tâche.
- Assignation à un collaborateur.
- Priorité, échéance et statut.
- Tâche associée ou non à un dossier.

Statut : À FAIRE

### US-008 — Gestion des clients
**En tant qu'** utilisateur,
**je veux** centraliser mes fiches clients,
**afin de** retrouver facilement leurs informations et dossiers.

Critères d'acceptation :
- Coordonnées du client.
- Historique des dossiers associés.
- Recherche de clients.
- Accès limité à l'entreprise concernée.

Statut : À FAIRE

### US-009 — Calendrier
**En tant qu'** utilisateur,
**je veux** consulter les échéances et rendez-vous,
**afin de** planifier mon activité.

Critères d'acceptation :
- Vue calendrier.
- Affichage des tâches et échéances.
- Accès au dossier ou à la tâche depuis le calendrier.

Statut : À FAIRE

### US-010 — Formulaire de contact public
**En tant que** visiteur,
**je veux** transmettre une demande à une entreprise,
**afin d'** être recontacté.

Critères d'acceptation :
- Formulaire propre à chaque entreprise.
- Création d'une demande à qualifier.
- Validation des données et protection contre les abus.
- Information RGPD adaptée au traitement.

Statut : À FAIRE

## P2 — Pilotage commercial

### US-011 — Pipeline commercial
Suivre une demande depuis sa qualification jusqu'au paiement.

### US-012 — Documents commerciaux
Gérer les devis, factures et documents associés aux dossiers.

### US-013 — Relances
Identifier les dossiers et paiements nécessitant une relance.

### US-014 — Historique et traçabilité
Conserver les événements importants, leur date et leur auteur.

## P3 — Administration SaaS

### US-015 — Gestion des abonnements
Gérer les offres, abonnements et statuts de paiement des entreprises clientes.

### US-016 — Administration de la plateforme
Disposer d'un espace réservé à l'exploitant de SuperviseMe pour superviser les entreprises clientes et les abonnements.

## Exigences transversales

- Isolation stricte des données entre entreprises.
- Contrôle des autorisations côté serveur.
- Sécurité des données et des accès.
- Respect du RGPD.
- Interface responsive.
- Tests automatisés avant fusion.
- Migrations de base de données avec Flyway.
- Sauvegardes et restauration documentées.

## Méthode de développement

1. Une User Story par branche `feature/...`.
2. Développement et tests.
3. Commit et push.
4. Pull Request vers `develop`.
5. Fusion uniquement après vérifications.
6. Synchronisation du dépôt local.
7. `main` reste la branche stable.

## Points à préciser

- Conditions de création de la première entreprise et de son ADMIN.
- Paramétrage des statuts métier.
- Règles exactes de gestion des devis et factures.
- Tarification et fonctionnement des abonnements.
- Politique de conservation et suppression des données.
