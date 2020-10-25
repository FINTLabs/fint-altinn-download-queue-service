package no.fint.downloadqueue.service;

import lombok.extern.slf4j.Slf4j;
import no.altinn.downloadqueue.wsdl.AltinnFault;
import no.altinn.downloadqueue.wsdl.ArchivedFormTaskDQBE;
import no.altinn.downloadqueue.wsdl.DownloadQueueItemBE;
import no.fint.downloadqueue.client.DownloadQueueClient;
import no.fint.downloadqueue.exception.AltinnFaultException;
import no.fint.downloadqueue.model.AltinnApplicationFactory;
import no.fint.downloadqueue.model.AltinnApplication;
import no.fint.downloadqueue.model.AltinnApplicationStatus;
import no.fint.downloadqueue.repository.AltinnApplicationRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class AltinnApplicationService {
    private final DownloadQueueClient downloadQueueClient;
    private final AltinnApplicationRepository altinnApplicationRepository;

    public AltinnApplicationService(DownloadQueueClient downloadQueueClient, AltinnApplicationRepository altinnApplicationRepository) {
        this.downloadQueueClient = downloadQueueClient;
        this.altinnApplicationRepository = altinnApplicationRepository;
    }

    @Scheduled(initialDelayString = "${scheduling.initial-delay}", fixedDelayString = "${scheduling.fixed-delay}")
    public void run() {
        log.info("Create...");
        create();

        log.info("Purge...");
        purge();
    }

    public void create() {
        List<DownloadQueueItemBE> downloadQueueItems;

        try {
            downloadQueueItems = downloadQueueClient.getDownloadQueueItems();
        } catch (AltinnFaultException ex) {
            log.error(altinnFaultToString(ex.getAltinnFault()));
            return;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return;
        }

        log.info("{} items in DownloadQueue", downloadQueueItems.size());

        downloadQueueItems.forEach(downloadQueueItem -> {
            String archiveReference = downloadQueueItem.getArchiveReference().getValue();

            if (altinnApplicationRepository.existsById(archiveReference)) {
                return;
            }

            Optional<ArchivedFormTaskDQBE> archivedFormTask;

            try {
                archivedFormTask = downloadQueueClient.getArchivedFormTask(archiveReference);

                archivedFormTask.ifPresent(formTask -> {
                    AltinnApplication application = AltinnApplicationFactory.of(downloadQueueItem, formTask);

                    if (application.getRequestor() == null) {
                        log.warn("Requestor not found for archive reference: {}", archiveReference);
                        return;
                    }

                    altinnApplicationRepository.save(application);

                    log.info("Created from archive reference: {}", archiveReference);
                });
            } catch (AltinnFaultException ex) {
                log.error(altinnFaultToString(ex.getAltinnFault()));
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        });
    }

    public void purge() {
        List<AltinnApplication> altinnApplications = altinnApplicationRepository.findByStatus(AltinnApplicationStatus.ARCHIVED);

        log.info("{} items in DownloadQueue to be purged", altinnApplications.size());

        altinnApplications.forEach(altinnApplication -> {
            try {
                altinnApplicationRepository.findById(altinnApplication.getArchiveReference()).ifPresent(application -> {
                    Optional<String> response = downloadQueueClient.purgeItem(application.getArchiveReference());

                    if (response.isPresent()) {
                        application.setStatus(AltinnApplicationStatus.PURGED);

                        altinnApplicationRepository.save(application);

                        log.info("Purged from archive reference: {}", application.getArchiveReference());
                    }
                });
            } catch (AltinnFaultException ex) {
                log.error(altinnFaultToString(ex.getAltinnFault()));
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        });
    }

    private String altinnFaultToString(AltinnFault altinnFault) {
        return Optional.ofNullable(altinnFault)
                .map(fault -> '\n' +
                        "AltinnErrorMessage: " + fault.getAltinnErrorMessage().getValue() + '\n' +
                        "AltinnExtendedErrorMessage: " + fault.getAltinnExtendedErrorMessage().getValue() + '\n' +
                        "AltinnLocalizedErrorMessage: " + fault.getAltinnLocalizedErrorMessage().getValue() + '\n' +
                        "ErrorGuid: " + fault.getErrorGuid().getValue() + '\n' +
                        "ErrorID: " + fault.getErrorID() + '\n' +
                        "UserGuid: " + fault.getUserGuid().getValue() + '\n' +
                        "UserId: " + fault.getUserId().getValue())
                .orElse("An error occurred");
    }
}
