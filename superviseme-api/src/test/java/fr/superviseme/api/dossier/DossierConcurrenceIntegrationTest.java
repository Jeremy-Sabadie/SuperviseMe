package fr.superviseme.api.dossier;

import fr.superviseme.api.client.Client;
import fr.superviseme.api.client.ClientRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DossierConcurrenceIntegrationTest {

    @Autowired DossierService service;
    @Autowired DossierRepository dossiers;
    @Autowired ClientRepository clients;
    @Autowired org.springframework.transaction.PlatformTransactionManager transactions;

    @Test
    void deuxAssociationsConcurrentesUneSeuleReussit() throws Exception {
        TransactionTemplate tx = new TransactionTemplate(transactions);

        // Préparation des données validée en base avant la concurrence.
        UUID[] ids = tx.execute(status -> {
            Client a = clients.save(new Client("Client concurrence A", null, null, null, null));
            Client b = clients.save(new Client("Client concurrence B", null, null, null, null));
            Dossier dossier = dossiers.save(new Dossier("Dossier concurrence", null));
            return new UUID[]{dossier.getId(), a.getId(), b.getId()};
        });

        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch depart = new CountDownLatch(1);

        try {
            Callable<Boolean> tentativeA = () -> associer(depart, ids[0], ids[1]);
            Callable<Boolean> tentativeB = () -> associer(depart, ids[0], ids[2]);

            Future<Boolean> a = pool.submit(tentativeA);
            Future<Boolean> b = pool.submit(tentativeB);
            depart.countDown();

            boolean resultatA = a.get(15, TimeUnit.SECONDS);
            boolean resultatB = b.get(15, TimeUnit.SECONDS);

            assertNotEquals(resultatA, resultatB,
                    "Une seule association doit réussir");

            UUID clientFinal = tx.execute(status ->
                    dossiers.findById(ids[0]).orElseThrow().getClientId());

            assertEquals(resultatA ? ids[1] : ids[2], clientFinal);
        } finally {
            pool.shutdownNow();
            tx.execute(status -> {
                dossiers.deleteById(ids[0]);
                clients.deleteById(ids[1]);
                clients.deleteById(ids[2]);
                return null;
            });
        }
    }

    private boolean associer(CountDownLatch depart, UUID dossierId, UUID clientId)
            throws InterruptedException {
        depart.await();
        try {
            SecurityContextHolder.getContext().setAuthentication(
                    new TestingAuthenticationToken(
                            "admin-test", null, "ROLE_ADMIN"));
            service.associerClientLibre(dossierId, clientId);
            return true;
        } catch (DossierDejaAssocieException e) {
            return false;
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
