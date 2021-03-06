package no.fint.downloadqueue.service;

import lombok.extern.slf4j.Slf4j;
import no.altinn.downloadqueue.wsdl.ArchivedFormTaskDQBE;
import no.altinn.downloadqueue.wsdl.DownloadQueueItemBE;
import no.fint.altinn.model.AltinnApplication;
import no.fint.altinn.model.AltinnApplicationStatus;
import no.fint.downloadqueue.client.DownloadQueueClient;
import no.fint.downloadqueue.exception.AltinnFaultException;
import no.fint.downloadqueue.factory.AltinnApplicationFactory;
import no.fint.downloadqueue.util.AltinnFaultPretty;
import no.fint.downloadqueue.repository.AltinnApplicationRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class AltinnApplicationService {
    private final DownloadQueueClient downloadQueueClient;
    private final AltinnApplicationRepository repository;

    public AltinnApplicationService(DownloadQueueClient downloadQueueClient, AltinnApplicationRepository repository) {
        this.downloadQueueClient = downloadQueueClient;
        this.repository = repository;
    }

    @Scheduled(initialDelayString = "${scheduling.initial-delay}", fixedDelayString = "${scheduling.fixed-delay}")
    public void run() {
        create();

        purge();
    }

    public void create() {
        List<DownloadQueueItemBE> downloadQueueItems;

        try {
            downloadQueueItems = downloadQueueClient.getDownloadQueueItems();
        } catch (AltinnFaultException ex) {
            log.error("{}", AltinnFaultPretty.of(ex.getAltinnFault()));
            return;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return;
        }

        log.info("{} items in DownloadQueue", downloadQueueItems.size());

        downloadQueueItems.forEach(downloadQueueItem -> {
            String archiveReference = downloadQueueItem.getArchiveReference().getValue();

            if (repository.existsById(archiveReference)) {
                return;
            }

            Optional<ArchivedFormTaskDQBE> archivedFormTask;

            try {
                archivedFormTask = downloadQueueClient.getArchivedFormTask(archiveReference);

                archivedFormTask.ifPresent(formTask -> {
                    AltinnApplication application = AltinnApplicationFactory.of(downloadQueueItem, formTask);

                    repository.save(application);

                    log.info("Created from archive reference: {}", archiveReference);
                });
            } catch (AltinnFaultException ex) {
                log.error(archiveReference + " - {}", AltinnFaultPretty.of(ex.getAltinnFault()));
            } catch (Exception ex) {
                log.error(archiveReference, ex);
            }
        });
    }

    public void purge() {
        List<AltinnApplication> applications = repository.findAllByStatus(AltinnApplicationStatus.ARCHIVED);

        log.info("{} items in DownloadQueue to be purged", applications.size());

        applications.forEach(application -> {
            try {
                Optional<String> response = downloadQueueClient.purgeItem(application.getArchiveReference());

                if (response.map("OK"::equals).isPresent()) {
                    application.setStatus(AltinnApplicationStatus.PURGED);

                    repository.save(application);

                    log.info("Purged from archive reference: {}", application.getArchiveReference());
                }
            } catch (AltinnFaultException ex) {
                log.error(application.getArchiveReference() + "- {}", AltinnFaultPretty.of(ex.getAltinnFault()));
            } catch (Exception ex) {
                log.error(application.getArchiveReference(), ex);
            }
        });
    }
}
