package no.fint.downloadqueue.util

import no.altinn.downloadqueue.wsdl.ApproverDQBE
import no.altinn.downloadqueue.wsdl.ApproverListDQBE
import no.altinn.downloadqueue.wsdl.ApproverSecurityLevel
import no.altinn.downloadqueue.wsdl.ArchivedFormTaskDQBE
import no.altinn.downloadqueue.wsdl.ArchivedShipmentMetadata
import no.altinn.downloadqueue.wsdl.ArchivedShipmentMetadataList
import no.altinn.downloadqueue.wsdl.DownloadQueueItemBE
import no.altinn.downloadqueue.wsdl.DownloadQueueReporteeType
import no.altinn.downloadqueue.wsdl.ObjectFactory

import javax.xml.datatype.DatatypeFactory
import java.time.ZonedDateTime

class DownloadQueueFactory {
    static ObjectFactory objectFactory = new ObjectFactory()

    static newDownloadQueueItem() {
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

    static newShipmentMetadataList() {
        return new ArchivedShipmentMetadataList(
                archivedShipmentMetadata: [
                        newShipmentMetadata('language', '0'),
                        newShipmentMetadata('fylke', 'fylke')
                ]
        )
    }

    static newShipmentMetadata(key, value) {
        new ArchivedShipmentMetadata(
                key: objectFactory.createArchivedShipmentMetadataKey(key),
                value: objectFactory.createArchivedShipmentMetadataValue(value)
        )
    }

    static newArchivedFormTask() {
        return new ArchivedFormTaskDQBE(
                approvers: objectFactory.createApproverListDQBE(newApproverList()),
                archiveReference: objectFactory.createArchivedFormTaskDQBEArchiveReference('archive-reference'),
                archiveTimeStamp: newArchivedDate(),
                attachments: null,
                attachmentsInResponse: 1,
                caseID: 0,
                correlationReference: 0,
                forms: null,
                formsInResponse: 1,
                reportee: objectFactory.createArchivedFormTaskDQBEReportee('reportee'),
                soEncryptedSymmetricKey: null,
                serviceCode: objectFactory.createArchivedFormTaskDQBEServiceCode('service-code'),
                serviceEditionCode: objectFactory.createArchivedFormTaskDQBEServiceEditionCode('service-edition-code')
        )
    }

    static newApproverList() {
        return new ApproverListDQBE(
                approverDQBE: [newApprover()]
        )
    }

    static newApprover() {
        return new ApproverDQBE(
                approvedTimeStamp: newArchivedDate(),
                approverID: objectFactory.createApproverDQBEApproverID('approver-id'),
                securityLevel: ApproverSecurityLevel.NOT_SENSITIVE
        )
    }

    static newArchivedDate() {
        ZonedDateTime dateTime = ZonedDateTime.parse('2020-01-01T00:00:30Z')
        GregorianCalendar calendar = GregorianCalendar.from(dateTime)
        return DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar)
    }
}