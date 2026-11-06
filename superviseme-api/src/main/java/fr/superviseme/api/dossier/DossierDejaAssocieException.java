package fr.superviseme.api.dossier;

public class DossierDejaAssocieException extends RuntimeException {
    public DossierDejaAssocieException() {
        super("Ce dossier est déjà associé à un client");
    }
}
