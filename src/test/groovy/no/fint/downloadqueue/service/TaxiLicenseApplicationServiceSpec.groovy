package no.fint.downloadqueue.service

import no.altinn.downloadqueue.wsdl.DownloadQueueItemBE
import no.altinn.downloadqueue.wsdl.ObjectFactory
import no.fint.downloadqueue.client.DownloadQueueClient
import no.fint.downloadqueue.model.TaxiLicenseApplication
import no.fint.downloadqueue.repository.TaxiLicenseApplicationRepository
import spock.lang.Specification

class TaxiLicenseApplicationServiceSpec extends Specification {
    TaxiLicenseApplicationRepository repository = Mock()
    DownloadQueueClient client = Mock()

    ObjectFactory objectFactory = new ObjectFactory()

    TaxiLicenseApplicationService service = new TaxiLicenseApplicationService(client, repository)

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
        1 * repository.save(new TaxiLicenseApplication(archiveReference: 'archive-reference'))
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
