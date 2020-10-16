package no.fint.downloadqueue.service

import no.altinn.downloadqueue.wsdl.DownloadQueueItemBE
import no.altinn.downloadqueue.wsdl.ObjectFactory
import no.fint.downloadqueue.client.DownloadQueueClient
import no.fint.downloadqueue.model.AltinnApplication
import no.fint.downloadqueue.repository.AltinnApplicationRepository
import spock.lang.Specification

class AltinnApplicationServiceSpec extends Specification {
    AltinnApplicationRepository repository = Mock()
    DownloadQueueClient client = Mock()

    ObjectFactory objectFactory = new ObjectFactory()

    AltinnApplicationService service = new AltinnApplicationService(client, repository)

    def "update taxi license applications saves if application does not exist"() {
        given:
        def item = new DownloadQueueItemBE(
                archiveReference: objectFactory.createDownloadQueueItemBEArchiveReference('archive-reference'))

        when:
        service.updateTaxiLicenseApplications()

        then:
        1 * client.getDownloadQueueItems() >> [item]
        1 * repository.existsById(_ as String) >> false
        1 * client.getArchivedFormTask(_ as String) >> Optional.of(objectFactory.createArchivedFormTaskDQBE())
        1 * repository.save(new AltinnApplication(archiveReference: 'archive-reference'))
    }

    def "update taxi license applications returns if application exists"() {
        given:
        def item = new DownloadQueueItemBE(
                archiveReference: objectFactory.createDownloadQueueItemBEArchiveReference('archive-reference'))

        when:
        service.updateTaxiLicenseApplications()

        then:
        1 * client.getDownloadQueueItems() >> [item]
        1 * repository.existsById(_ as String) >> true
    }
}
