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
import no.fint.downloadqueue.client.AttachmentDataStreamedClient
import no.fint.downloadqueue.client.DownloadQueueClient
import no.fint.downloadqueue.model.AltinnApplicationFactory
import no.fint.downloadqueue.model.AltinnApplicationStatus
import spock.lang.Specification

import javax.xml.datatype.DatatypeFactory
import java.time.ZonedDateTime

class AltinnApplicationFactorySpec extends Specification {
    ObjectFactory objectFactory = new ObjectFactory()

    DownloadQueueClient downloadQueueClient = Mock()
    AttachmentDataStreamedClient attachmentDataStreamedClient = Mock()

    AltinnApplicationFactory altinnApplicationFactory = new AltinnApplicationFactory(downloadQueueClient, attachmentDataStreamedClient)

    def "of() returns AltinnApplication given metadata, form and attachments"() {
        given:
        def item = newDownloadQueueItem()
        def task = newArchivedFormTask()

        when:
        def application = altinnApplicationFactory.of(item, task)

        then:
        1 * downloadQueueClient.getFormSetPdf('archive-reference', 1044) >> ([0, 1, 2] as byte[])
        application.archiveReference == 'archive-reference'
        application.requestor == '921693230'
        application.requestorName == 'Viken'
        application.subject == 'reportee-id'
        application.subjectName == 'Taxi AS'
        application.serviceCode == 'service-code'
        application.languageCode == 1044
        application.status == AltinnApplicationStatus.NEW
        application.form.formData == '<xml>'
        application.form.formDataPdf == [0, 1, 2] as byte[]
        application.attachments.first().attachmentId == 0
        application.attachments.first().attachmentData == [0, 1, 2] as byte[]
        application.attachments.first().attachmentType == 'attachment-type'
        application.attachments.first().attachmentTypeName == 'attachment-type-name'
        application.attachments.first().attachmentTypeNameLanguage == 'attachment-type-name-language'
    }

    def newDownloadQueueItem() {
        return new DownloadQueueItemBE(
                archiveReference: objectFactory.createDownloadQueueItemBEArchiveReference('archive-reference'),
                archivedDate: newArchivedDate(),
                reporteeID: objectFactory.createDownloadQueueItemBEReporteeID('reportee-id'),
                reporteeType: DownloadQueueReporteeType.ORGANISATION,
                serviceCode: objectFactory.createDownloadQueueItemBEServiceCode('service-code'),
                serviceEditionCode: 0,
                shipmentMetadataList: objectFactory.createArchivedShipmentMetadataList(newShipmentMetadataList())
        )
    }

    def newShipmentMetadataList() {
        return new ArchivedShipmentMetadataList(
                archivedShipmentMetadata: [
                        newShipmentMetadata('language', '1044'),
                        newShipmentMetadata('fylke', 'Viken'),
                        newShipmentMetadata('fylkesnummer', '30'),
                        newShipmentMetadata('innsender', 'Taxi AS')
                ]
        )
    }

    def newShipmentMetadata(key, value) {
        new ArchivedShipmentMetadata(
                key: objectFactory.createArchivedShipmentMetadataKey(key),
                value: objectFactory.createArchivedShipmentMetadataValue(value)
        )
    }

    def newArchivedFormTask() {
        return new ArchivedFormTaskDQBE(
                approvers: objectFactory.createApproverListDQBE(newApproverList()),
                archiveReference: objectFactory.createArchivedFormTaskDQBEArchiveReference('archive-reference'),
                archiveTimeStamp: newArchivedDate(),
                attachments: objectFactory.createArchivedAttachmentExternalListDQBE(newArchivedAttachmentList()),
                attachmentsInResponse: 1,
                caseID: 0,
                correlationReference: 0,
                forms: objectFactory.createArchivedFormListDQBE(newArchivedFormList()),
                formsInResponse: 1,
                reportee: objectFactory.createArchivedFormTaskDQBEReportee('reportee'),
                soEncryptedSymmetricKey: null,
                serviceCode: objectFactory.createArchivedFormTaskDQBEServiceCode('service-code'),
                serviceEditionCode: objectFactory.createArchivedFormTaskDQBEServiceEditionCode('service-edition-code')
        )
    }

    def newArchivedFormList() {
        return new ArchivedFormListDQBE(
                archivedFormDQBE: [
                        new ArchivedFormDQBE(
                                reference: objectFactory.createArchivedFormDQBEReference('reference'),
                                formData: objectFactory.createArchivedFormDQBEFormData('<xml>'),
                        )
                ]
        )
    }

    def newArchivedAttachmentList() {
        return new ArchivedAttachmentExternalListDQBE(
                archivedAttachmentDQBE: [
                        new ArchivedAttachmentDQBE(
                                attachmentId: 0,
                                attachmentData: objectFactory.createArchivedAttachmentDQBEAttachmentData([0, 1, 2] as byte[]),
                                attachmentType: objectFactory.createArchivedAttachmentDQBEAttachmentType('attachment-type'),
                                attachmentTypeName: objectFactory.createArchivedAttachmentDQBEAttachmentTypeName('attachment-type-name'),
                                attachmentTypeNameLanguage: objectFactory.createArchivedAttachmentDQBEAttachmentTypeNameLanguage('attachment-type-name-language')
                        )
                ]
        )
    }

    def newApproverList() {
        return new ApproverListDQBE(
                approverDQBE: [newApprover()]
        )
    }

    def newApprover() {
        return new ApproverDQBE(
                approvedTimeStamp: newArchivedDate(),
                approverID: objectFactory.createApproverDQBEApproverID('approver-id'),
                securityLevel: ApproverSecurityLevel.NOT_SENSITIVE
        )
    }


    def newArchivedDate() {
        ZonedDateTime dateTime = ZonedDateTime.parse('2020-01-01T00:00:30Z')
        GregorianCalendar calendar = GregorianCalendar.from(dateTime)
        return DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar)
    }
}
