package no.fint.downloadqueue.service

import no.altinn.downloadqueue.wsdl.DownloadQueueItemBE
import no.altinn.downloadqueue.wsdl.ObjectFactory
import no.fint.downloadqueue.client.DownloadQueueClient
import no.fint.downloadqueue.model.AltinnApplication
import no.fint.downloadqueue.model.AltinnApplicationFactory
import no.fint.downloadqueue.repository.AltinnApplicationRepository
import spock.lang.Specification

class AltinnApplicationServiceSpec extends Specification {
    AltinnApplicationRepository repository = Mock()
    DownloadQueueClient client = Mock()
    AltinnApplicationFactory factory = Mock()

    ObjectFactory objectFactory = new ObjectFactory()

    AltinnApplicationService service = new AltinnApplicationService(client, repository, factory)

    def "create altinn application if application does not exist"() {
        given:
        def item = new DownloadQueueItemBE(
                archiveReference: objectFactory.createDownloadQueueItemBEArchiveReference('archive-reference'))
        def task = objectFactory.createArchivedFormTaskDQBE()

        when:
        service.create()

        then:
        1 * client.getDownloadQueueItems() >> [item]
        1 * repository.existsById(_ as String) >> false
        1 * client.getArchivedFormTask(_ as String) >> Optional.of(task)
        1 * factory.of(_, _) >> new AltinnApplication(requestor: 'requestor')
        1 * repository.save(_)
    }

    def "create altinn application returns if application exists"() {
        given:
        def item = new DownloadQueueItemBE(
                archiveReference: objectFactory.createDownloadQueueItemBEArchiveReference('archive-reference'))

        when:
        service.create()

        then:
        1 * client.getDownloadQueueItems() >> [item]
        1 * repository.existsById(_ as String) >> true
    }

    def "purge altinn application if application has status archived"() {
        when:
        service.purge()

        then:
        1 * repository.findByStatus(_) >> [new AltinnApplication()]
        1 * repository.findById(_) >> Optional.of(new AltinnApplication())
        1 * client.purgeItem(_) >> Optional.of('purged')
        1 * repository.save(_)
    }

    def "purge altinn application returns if no documents with application has status archived"() {
        when:
        service.purge()

        then:
        1 * repository.findByStatus(_) >> []
    }
}
