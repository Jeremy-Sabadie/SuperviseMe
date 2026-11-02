package fr.superviseme.api.entreprise;

import java.util.UUID;

/**
 * Génère le nom de la base PostgreSQL dédiée à une entreprise.
 *
 * Le nom est dérivé exclusivement de l'UUID de l'entreprise :
 * - il est stable pour une même entreprise ;
 * - il est distinct pour deux UUID différents ;
 * - il ne contient que des caractères compatibles avec PostgreSQL.
 *
 * Cette classe ne crée pas la base et ne gère pas les connexions.
 */
public class EntrepriseDatabaseNameFactory {

    private static final String PREFIXE = "superviseme_";

    /**
     * Génère un nom de base à partir de l'identifiant de l'entreprise.
     *
     * @param entrepriseId identifiant unique de l'entreprise
     * @return nom de base au format superviseme_ suivi de 32 caractères hexadécimaux
     * @throws IllegalArgumentException si l'identifiant est absent
     */
    public String generer(UUID entrepriseId) {
        if (entrepriseId == null) {
            throw new IllegalArgumentException(
                    "L'identifiant de l'entreprise est obligatoire"
            );
        }

        return PREFIXE + entrepriseId.toString().replace("-", "");
    }
}
