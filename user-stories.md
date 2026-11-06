
# SuperviseMe — User Stories (mise à jour au 6 novembre 2026)

> Source : cadrage initial transmis dans la conversation, enrichi des besoins apparus pendant le développement. Les statuts sont fondés sur les travaux connus, **sans audit exhaustif du dépôt**. « Partiel » ne signifie pas que tous les critères sont satisfaits.

## Vision

SuperviseMe est un SaaS de pilotage d'activité pour TPE/PME. Le terme générique **dossier** permet une utilisation dans différents secteurs.

**Rôles :** ADMIN (dirigeant), UTILISATEUR (collaborateur), VISITEUR (formulaire public).

## P0 — Fondations et sécurité

### US-001 — Gestion des entreprises — EN COURS

**En tant qu'** administrateur, je veux un espace propre à mon entreprise pour isoler mon activité.

Critères : UUID unique ; nom obligatoire ; coordonnées facultatives ; dates de création et modification ; isolation des données entre entreprises.

**À vérifier :** modèle entreprise, rattachement des données et tests d'isolation interentreprises.

### US-002 — Authentification — PARTIEL

**En tant qu'** utilisateur, je veux me connecter de manière sécurisée.

Critères : connexion/déconnexion ; mots de passe hachés ; réinitialisation du mot de passe ; routes privées protégées.

**Acquis connu :** authentification et contrôle JWT utilisés. **À vérifier :** déconnexion, réinitialisation, couverture complète des protections.

### US-003 — Gestion des utilisateurs et rôles — PARTIEL, largement réalisé

**En tant qu'** ADMIN, je veux inviter et gérer mes collaborateurs.

Critères : rôles ADMIN/UTILISATEUR ; aucune inscription publique ADMIN ; invitation réservée à ADMIN ; autorisations côté serveur.

#### US-003.1 [AJOUT] — Création et recherche de collaborateurs — RÉALISÉ

Création d'un collaborateur par ADMIN, liste et recherche en interface ; vérification exhaustive des autorisations encore à auditer.

#### US-003.2 [AJOUT] — Désactivation et réactivation — VALIDÉ

ADMIN peut désactiver/réactiver ; compte désactivé bloqué à la connexion et avec un ancien JWT ; nouvelles affectations refusées ; historique et affectations existantes conservés.

**PR #13 fusionnée ; migration Flyway V8 ; test IHM concluant le 06/11.**

#### US-003.3 [AJOUT] — Confirmation de désactivation SweetAlert2 — VALIDÉ

Confirmation explicite, annulation sans effet, message de conséquences, interface adaptée au mobile et libellé « Compte désactivé ».

**PR #14 fusionnée ; 67 tests Angular et build validés ; vérification IHM le 06/11.**

## P1 — Fonctionnalités principales

### US-004 — Tableau de bord — À FAIRE / À VÉRIFIER

Indicateurs d'activité ; dossiers et tâches prioritaires ; activité récente ; responsive.

Maquette préparée, réalisation complète non confirmée.

### US-005 — Gestion des dossiers — PARTIEL

Création, consultation, modification ; client, description, statut et responsable ; photos/commentaires ; historique des modifications.

Socle développé, photos/commentaires/historique complet à vérifier.

#### US-005.1 [AJOUT] — Responsable et participants — RÉALISÉ, À AUDITER

Affecter responsable et participants ; contrôler les accès ; préserver l'accès ADMIN aux dossiers historiques sans responsable.

#### US-005.2 [AJOUT] — Changement de statut via API — RÉALISÉ

Déplacement de dossier ; vérification des droits ; rejet des statuts inconnus et accès non authentifiés ; tests d'intégration développés.

#### US-005.3 [AJOUT] — Kanban frontend — RÉALISÉ

Affichage en colonnes et déplacement des dossiers avec persistance.

Statuts configurables exclus du périmètre actuel.

### US-006 — Tableau Kanban — PARTIEL / FONCTIONNEL

Colonnes de statut, déplacement, enregistrement des changements ; statuts configurables à terme.

**Recoupe US-005.2 et 005.3 : ne pas redévelopper.** Configurabilité à faire.

### US-007 — Gestion des tâches — À FAIRE

Création et modification ; affectation collaborateur ; priorité, échéance et statut ; tâche autonome ou liée à un dossier.

**Prochaine US fonctionnelle proposée**, après contrôle des points P0.

### US-008 — Gestion des clients — PARTIEL / À VÉRIFIER

Coordonnées ; dossiers associés ; recherche ; accès limité à l'entreprise.

Des écrans et fonctions clients existent, mais couverture intégrale non auditée.

### US-009 — Calendrier — À FAIRE

Vue calendrier ; tâches et échéances ; accès direct au dossier ou à la tâche.

### US-010 — Formulaire de contact public — À FAIRE

Formulaire propre à chaque entreprise ; création d'une demande à qualifier ; validation et anti-abus ; information RGPD adaptée.

#### US-010.1 [AJOUT] — Création automatique d'une fiche prospect — À FAIRE

Une soumission du formulaire public crée automatiquement une fiche prospect/demande dans **la bonne entreprise**, sans ressaisie, avec information RGPD et protections adaptées.

## P2 — Pilotage commercial

### US-011 — Pipeline commercial — À FAIRE

Suivre une demande depuis sa qualification jusqu'au paiement.

### US-012 — Documents commerciaux — À FAIRE

Gérer devis, factures et documents associés aux dossiers.

### US-013 — Relances — À FAIRE

Identifier les dossiers et paiements à relancer.

### US-014 — Historique et traçabilité — À FAIRE / À VÉRIFIER

Conserver les événements importants, leur date et leur auteur.

#### US-014.1 [AJOUT] — Commentaires et événements horodatés — À FAIRE / À VÉRIFIER

Commentaires sur dossiers et changements de statut, avec auteur, date et accès contrôlé.

## P3 — Administration SaaS

### US-015 — Gestion des abonnements — À FAIRE

Offres, abonnements et statuts de paiement des entreprises clientes.

### US-016 — Administration de la plateforme — À FAIRE

Espace exploitant pour superviser les entreprises clientes et abonnements.

## Besoins identifiés après le cadrage initial

### US-017 [AJOUT] — Planning des équipes — À CADRER

**En tant qu'** ADMIN, je veux organiser les interventions et la charge de mes collaborateurs.

Critères envisagés :
- Planning par personne et période.
- Affectation à des dossiers/tâches.
- Disponibilités.
- Interface mobile.

**Distinct de l'US-009**, qui affiche les échéances.

### US-018 [AJOUT] — Commandes fournisseurs — À CADRER

**En tant qu'** ADMIN, je veux gérer les approvisionnements liés ou non aux dossiers.

Critères envisagés :
- États : à commander / commandée / en attente / reçue / à payer / payée.
- Montants et échéances.
- Alertes sur les commandes à lancer après acceptation d'un devis.

### US-019 [AJOUT] — Automatisations internes sans licence facturée par workflow — À CADRER

**En tant qu'** exploitant, je veux automatiser les processus sans coût de licence ou d'abonnement supplémentaire par workflow.

Critères envisagés :
- Automatisations invisibles pour le client.
- Solutions auto-hébergées privilégiées.
- Licences, ressources serveur et coûts indirects à vérifier.
- Sécurité et traçabilité.

### US-020 [AJOUT] — Pièces jointes et documents des dossiers — À CADRER

**En tant qu'** utilisateur, je veux déposer et retrouver PDF, photos et documents dans un dossier ou une fiche client.

Critères envisagés :
- Dépôt de fichiers.
- Contrôle des droits.
- Stockage sécurisé.
- Limites et conservation.

À articuler avec US-005 et US-012.

## Exigences transversales

- Isolation stricte entre entreprises et contrôle des autorisations côté serveur.
- Sécurité, RGPD, interface responsive.
- Tests automatisés et build avant fusion ; migrations Flyway.
- Sauvegardes et restauration documentées.
- Historique préservé lors de la désactivation d'un collaborateur.

## Méthode de développement (pratique observée)

1. Cadrer les critères de l'US.
2. Créer une branche `feature/...`.
3. Développer et tester, idéalement tests en amont.
4. Commit, push, Pull Request, vérification et fusion.
5. Synchroniser `main` en local et supprimer la branche terminée.
6. Mettre à jour les statuts de ce fichier.

**Écart à arbitrer :** le cadrage initial prévoyait une PR vers `develop`, mais les dernières PR (#13 et #14) ont été fusionnées vers **`main`**. Ne pas changer le workflow sans décision explicite.

## Reprise proposée

1. Vérifier les critères P0 non prouvés (isolation, réinitialisation, permissions).
2. Cadrer et développer **US-007 — Gestion des tâches**.
3. Mettre à jour les statuts après tests et validation IHM.

## Questions ouvertes

- Création initiale de l'entreprise et de son ADMIN.
- Statuts configurables.
- Devis/factures.
- Tarification.
- Conservation/suppression RGPD.
- Articulation calendrier/planning.
- Règles fournisseurs.
- Stockage des documents.
