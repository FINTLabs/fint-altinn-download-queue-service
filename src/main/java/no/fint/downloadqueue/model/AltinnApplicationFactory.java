package no.fint.downloadqueue.model;

import no.altinn.downloadqueue.wsdl.ArchivedFormTaskDQBE;
import no.altinn.downloadqueue.wsdl.DownloadQueueItemBE;

import java.util.Optional;

public final class AltinnApplicationFactory {

    private AltinnApplicationFactory() {
    }

    public static Optional<AltinnApplication> of(DownloadQueueItemBE downloadQueueItem, ArchivedFormTaskDQBE task) {
        AltinnApplication altinnApplication = new AltinnApplication();

        altinnApplication.setArchiveReference(downloadQueueItem.getArchiveReference().getValue());

        /*
        TODO
         */

        return Optional.of(altinnApplication);
    }
}
