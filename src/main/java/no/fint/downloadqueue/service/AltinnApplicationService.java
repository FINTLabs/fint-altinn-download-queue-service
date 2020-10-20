package no.fint.downloadqueue.service;

import lombok.extern.slf4j.Slf4j;
import no.altinn.downloadqueue.wsdl.AltinnFault;
import no.altinn.downloadqueue.wsdl.ArchivedFormTaskDQBE;
import no.altinn.downloadqueue.wsdl.DownloadQueueItemBE;
import no.fint.downloadqueue.client.DownloadQueueClient;
import no.fint.downloadqueue.exception.AltinnFaultException;
import no.fint.downloadqueue.model.AltinnApplicationFactory;
import no.fint.downloadqueue.model.AltinnApplication;
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
    private final AltinnApplicationFactory altinnApplicationFactory;

    public AltinnApplicationService(DownloadQueueClient downloadQueueClient, AltinnApplicationRepository altinnApplicationRepository, AltinnApplicationFactory altinnApplicationFactory) {
        this.downloadQueueClient = downloadQueueClient;
        this.altinnApplicationRepository = altinnApplicationRepository;
        this.altinnApplicationFactory = altinnApplicationFactory;
    }

    @Scheduled(initialDelayString = "${scheduling.initial-delay-get}", fixedDelayString = "${scheduling.fixed-delay-get}")
    public void updateAltinnApplications() {
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

        downloadQueueItems.forEach(downloadQueuItem -> {
            String archiveReference = downloadQueuItem.getArchiveReference().getValue();

            if (altinnApplicationRepository.existsById(archiveReference)) {
                return;
            }

            Optional<ArchivedFormTaskDQBE> archivedFormTask;

            try {
                archivedFormTask = downloadQueueClient.getArchivedFormTask(archiveReference);

                archivedFormTask.ifPresent(formTask -> {
                    AltinnApplication altinnApplication = altinnApplicationFactory.of(downloadQueuItem, formTask);

                    altinnApplicationRepository.save(altinnApplication);

                    log.info("New document created from archive reference: {}", archiveReference);
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
