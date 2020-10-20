package no.fint.downloadqueue.repository

import no.fint.downloadqueue.model.AltinnApplication
import no.fint.downloadqueue.model.AltinnApplicationStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import spock.lang.Specification

@DataMongoTest
class AltinnApplicationRepositorySpec extends Specification {

    @Autowired
    AltinnApplicationRepository repository

    def "findByStatus() returns documents given status"() {
        given:
        repository.saveAll(Arrays.asList(new AltinnApplication(status: AltinnApplicationStatus.NEW),
                new AltinnApplication(status: AltinnApplicationStatus.ARCHIVED),
                new AltinnApplication(status: AltinnApplicationStatus.ARCHIVED)))

        when:
        def documents = repository.findByStatus(AltinnApplicationStatus.ARCHIVED)

        then:
        documents.size() == 2
    }
}
