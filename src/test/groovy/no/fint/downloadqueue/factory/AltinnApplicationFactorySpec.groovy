package no.fint.downloadqueue.factory

import no.altinn.downloadqueue.wsdl.ApproverDQBE
import no.altinn.downloadqueue.wsdl.ApproverListDQBE
import no.altinn.downloadqueue.wsdl.ApproverSecurityLevel
import no.altinn.downloadqueue.wsdl.ArchivedAttachmentDQBE
import no.altinn.downloadqueue.wsdl.ArchivedAttachmentExternalListDQBE
import no.altinn.downloadqueue.wsdl.ArchivedFormDQBE
import no.altinn.downloadqueue.wsdl.ArchivedFormListDQBE
import no.altinn.downloadqueue.wsdl.ArchivedFormTaskDQBE
import no.altinn.downloadqueue.wsdl.ArchivedShipmentMetadata
import no.altinn.downloadqueue.wsdl.ArchivedShipmentMetadataList
import no.altinn.downloadqueue.wsdl.DownloadQueueItemBE
import no.altinn.downloadqueue.wsdl.DownloadQueueReporteeType
import no.altinn.downloadqueue.wsdl.ObjectFactory
import no.fint.downloadqueue.model.AltinnApplicationFactory
import no.fint.downloadqueue.model.AltinnApplicationStatus
import no.fint.downloadqueue.util.DownloadQueueObjectFactory
import spock.lang.Specification

import javax.xml.datatype.DatatypeFactory
import java.time.LocalDateTime
import java.time.ZonedDateTime

class AltinnApplicationFactorySpec extends Specification {
    ObjectFactory objectFactory = new ObjectFactory()

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
        application.attachments.first().attachmentId == 0
        application.attachments.first().attachmentType == 'attachment-type'
        application.attachments.first().attachmentTypeName == 'attachment-type-name'
        application.attachments.first().attachmentTypeNameLanguage == 'attachment-type-name-language'
        application.attachments.first().fileName == 'filename'
    }
}
