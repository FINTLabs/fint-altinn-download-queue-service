package no.fint.downloadqueue.service;

import lombok.extern.slf4j.Slf4j;
import no.altinn.downloadqueue.wsdl.AltinnFault;
import no.altinn.downloadqueue.wsdl.ArchivedFormTaskDQBE;
import no.altinn.downloadqueue.wsdl.DownloadQueueItemBE;
import no.fint.downloadqueue.client.DownloadQueueClient;
import no.fint.downloadqueue.exception.AltinnFaultException;
import no.fint.downloadqueue.model.TaxiLicenseApplicationFactory;
import no.fint.downloadqueue.model.TaxiLicenseApplication;
import no.fint.downloadqueue.repository.TaxiLicenseApplicationRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.ws.client.WebServiceClientException;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class TaxiLicenseApplicationService {
    private final DownloadQueueClient downloadQueueClient;
    private final TaxiLicenseApplicationRepository taxiLicenseApplicationRepository;

    public TaxiLicenseApplicationService(DownloadQueueClient downloadQueueClient, TaxiLicenseApplicationRepository taxiLicenseApplicationRepository) {
        this.downloadQueueClient = downloadQueueClient;
        this.taxiLicenseApplicationRepository = taxiLicenseApplicationRepository;
    }

    @Scheduled(initialDelayString = "${scheduling.initial-delay}", fixedDelayString = "${scheduling.fixed-delay}")
    public void updateTaxiLicenseApplications() {
        List<DownloadQueueItemBE> downloadQueueItems;

        try {
            downloadQueueItems = downloadQueueClient.getDownloadQueueItems();
        } catch (AltinnFaultException ex) {
            log.error(altinnFaultToString(ex.getAltinnFault()));
            return;
        }  catch (WebServiceClientException ex) {
            log.error(ex.getMessage(), ex);
            return;
        }

        downloadQueueItems.forEach(item -> {
            String archiveReference = item.getArchiveReference().getValue();

            if (taxiLicenseApplicationRepository.existsById(archiveReference)) {
                return;
            }

            Optional<ArchivedFormTaskDQBE> archivedFormTask;

            try {
                archivedFormTask = downloadQueueClient.getArchivedFormTask(archiveReference);
            } catch (AltinnFaultException ex) {
                log.error(archiveReference + " - " + altinnFaultToString(ex.getAltinnFault()));
                return;
            }  catch (WebServiceClientException ex) {
                log.error(archiveReference + " - " + ex.getMessage(), ex);
                return;
            }

            archivedFormTask.ifPresent(task -> {
                Optional<TaxiLicenseApplication> taxiLicenseApplication = TaxiLicenseApplicationFactory.of(item, task);

                taxiLicenseApplication.ifPresent(taxiLicenseApplicationRepository::save);
            });
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
                .orElse("AltinnFault not present in response");
    }
}
