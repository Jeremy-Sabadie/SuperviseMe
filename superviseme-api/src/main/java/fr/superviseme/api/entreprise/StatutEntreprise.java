package fr.superviseme.api.entreprise;

/**
 * État d'une entreprise dans le registre central SuperviseMe.
 *
 * EN_ATTENTE : entreprise enregistrée, instance non activée.
 * ACTIVE : instance provisionnée et opérationnelle.
 * ERREUR : échec du provisionnement ou de l'activation.
 */
public enum StatutEntreprise {
    EN_ATTENTE,
    ACTIVE,
    ERREUR
}
