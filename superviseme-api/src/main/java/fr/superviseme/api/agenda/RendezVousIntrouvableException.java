package fr.superviseme.api.agenda;

public class RendezVousIntrouvableException extends RuntimeException {
    public RendezVousIntrouvableException() {
        super("Rendez-vous introuvable");
    }
}
