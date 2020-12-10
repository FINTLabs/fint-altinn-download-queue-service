package no.fint.downloadqueue.factory

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import no.fint.altinn.model.AltinnApplicationStatus
import no.fint.altinn.model.AltinnForm
import no.fint.downloadqueue.util.DownloadQueueObjectFactory
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
        application.subject == '123456789'
        application.subjectName == 'Taxi AS'
        application.serviceCode == 'service-code'
        application.languageCode == 1044
        application.phone == 'phone'
        application.email == 'email'
        application.businessAddress.address == 'address'
        application.businessAddress.postCode == 'post-code'
        application.businessAddress.postalArea == 'postal-area'
        application.status == AltinnApplicationStatus.NEW
        application.form.formData == DownloadQueueObjectFactory.newFormData()
        application.attachments.values().first().attachmentId == 0
        application.attachments.values().first().attachmentType == 'application/pdf'
        application.attachments.values().first().attachmentTypeName == 'attachment-type-name'
        application.attachments.values().first().attachmentTypeNameLanguage == 'attachment-type-name-language'
        application.attachments.values().first().fileName == 'attachment-type-name.pdf'
    }

    def "XmlMapper deserializes form"() {
        when:
        def form = new XmlMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .readValue(DownloadQueueObjectFactory.newFormData(), AltinnForm.class)

        then:
        form.submitter.organisation.name == 'Taxi AS'
        form.submitter.organisation.businessAddress.address == 'address'
        form.submitter.organisation.businessAddress.postCode == 'post-code'
        form.submitter.organisation.businessAddress.postalArea == 'postal-area'
        form.submitter.organisation.phone == 'phone'
        form.submitter.organisation.email == 'email'
        form.submitter.organisation.county == 'Viken'
        form.submitter.organisation.countyNumber == '30'
        form.submitter.language == '1044'
    }
}
