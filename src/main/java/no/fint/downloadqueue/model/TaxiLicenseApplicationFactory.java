package no.fint.downloadqueue.model;

import no.altinn.downloadqueue.wsdl.ArchivedFormTaskDQBE;
import no.altinn.downloadqueue.wsdl.DownloadQueueItemBE;

import java.util.Optional;

public final class TaxiLicenseApplicationFactory {

    private TaxiLicenseApplicationFactory() {
    }

    public static Optional<TaxiLicenseApplication> of(DownloadQueueItemBE downloadQueueItem, ArchivedFormTaskDQBE task) {
        TaxiLicenseApplication taxiLicenseApplication = new TaxiLicenseApplication();

        taxiLicenseApplication.setArchiveReference(downloadQueueItem.getArchiveReference().getValue());

        /*
        TODO
         */

        return Optional.of(taxiLicenseApplication);
    }
}
