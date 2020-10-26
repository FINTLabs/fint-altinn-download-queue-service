package no.fint.downloadqueue.factory

import no.fint.downloadqueue.model.AltinnApplicationFactory
import no.fint.downloadqueue.model.AltinnApplicationStatus
import no.fint.downloadqueue.util.DownloadQueueObjectFactory
import org.springframework.http.MediaType
import spock.lang.Specification

import java.time.LocalDateTime

class AltinnApplicationFactorySpec extends Specification {
    AltinnApplicationFactory altinnApplicationFactory = new AltinnApplicationFactory()

    def "of() returns AltinnApplication given metadata, form and attachments"() {
        given:
        def item = DownloadQueueObjectFactory.newDownloadQueueItem()
        def task = DownloadQueueObjectFactory.newArchivedFormTask()

        when:
        def application = altinnApplicationFactory.of(item, task)

        then:
        application.archiveReference == 'archive-reference'
        application.archivedDate == LocalDateTime.parse('2020-01-01T00:00:30')
        application.requestor == '921693230'
        application.requestorName == 'Viken'
        application.subject == 'reportee-id'
        application.subjectName == 'Taxi AS'
        application.serviceCode == 'service-code'
        application.languageCode == 1044
        application.status == AltinnApplicationStatus.NEW
        application.form.formData == '<xml>'
        application.attachments.values().first().attachmentId == 0
        application.attachments.values().first().attachmentType == MediaType.APPLICATION_PDF
        application.attachments.values().first().attachmentTypeName == 'attachment-type-name'
        application.attachments.values().first().attachmentTypeNameLanguage == 'attachment-type-name-language'
        application.attachments.values().first().fileName == 'attachment-type-name.pdf'
    }
}
