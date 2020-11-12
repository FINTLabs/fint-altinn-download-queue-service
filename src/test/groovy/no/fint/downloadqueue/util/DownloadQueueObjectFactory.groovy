package no.fint.downloadqueue.util

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

import javax.xml.datatype.DatatypeFactory
import java.time.ZonedDateTime

class DownloadQueueObjectFactory {

    static ObjectFactory objectFactory = new ObjectFactory()

    static newDownloadQueueItem() {
        return new DownloadQueueItemBE(
                archiveReference: objectFactory.createDownloadQueueItemBEArchiveReference('archive-reference'),
                archivedDate: newArchivedDate(),
                reporteeID: objectFactory.createDownloadQueueItemBEReporteeID('123456789'),
                reporteeType: DownloadQueueReporteeType.ORGANISATION,
                serviceCode: objectFactory.createDownloadQueueItemBEServiceCode('service-code'),
                serviceEditionCode: 0,
                shipmentMetadataList: objectFactory.createArchivedShipmentMetadataList(newShipmentMetadataList())
        )
    }

    static newShipmentMetadataList() {
        return new ArchivedShipmentMetadataList(
                archivedShipmentMetadata: [
                        newShipmentMetadata('language', '1044'),
                        newShipmentMetadata('fylke', 'Viken'),
                        newShipmentMetadata('fylkesnummer', '30'),
                        newShipmentMetadata('innsender', 'Taxi AS')
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

    static newArchivedFormList() {
        return new ArchivedFormListDQBE(
                archivedFormDQBE: [
                        new ArchivedFormDQBE(
                                reference: objectFactory.createArchivedFormDQBEReference('reference'),
                                formData: objectFactory.createArchivedFormDQBEFormData(newFormData()),
                        )
                ]
        )
    }

    static newArchivedAttachmentList() {
        return new ArchivedAttachmentExternalListDQBE(
                archivedAttachmentDQBE: [
                        new ArchivedAttachmentDQBE(
                                attachmentId: 0,
                                attachmentData: objectFactory.createArchivedAttachmentDQBEAttachmentData([0, 1, 2] as byte[]),
                                attachmentType: objectFactory.createArchivedAttachmentDQBEAttachmentType('application_pdf'),
                                attachmentTypeName: objectFactory.createArchivedAttachmentDQBEAttachmentTypeName('attachment-type-name'),
                                attachmentTypeNameLanguage: objectFactory.createArchivedAttachmentDQBEAttachmentTypeNameLanguage('attachment-type-name-language'),
                                fileName: objectFactory.createArchivedAttachmentDQBEFileName('filename.pdf')
                        )
                ]
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

    static newFormData() {
        return '<melding xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"\n' +
                '    xmlns:ns2="http://schemas.microsoft.com/2003/10/Serialization/" dataFormatProvider="SERES" dataFormatId="1" dataFormatVersion="2">\n' +
                '    <Innsender>\n' +
                '        <Organisasjon>\n' +
                '            <organisasjonsnummer>123456789</organisasjonsnummer>\n' +
                '            <navn>Taxi AS</navn>\n' +
                '            <opprettet>2016-10-18T00:00:00</opprettet>\n' +
                '            <Forretningsadresse>\n' +
                '                <adresse>address</adresse>\n' +
                '                <postnummer>post-code</postnummer>\n' +
                '                <poststed>postal-area</poststed>\n' +
                '            </Forretningsadresse>\n' +
                '            <Postadresse></Postadresse>\n' +
                '            <telefonnummer>phone</telefonnummer>\n' +
                '            <epost>email</epost>\n' +
                '            <kommunenummer>kommunenummer</kommunenummer>\n' +
                '            <kommunenavn>kommunenavn</kommunenavn>\n' +
                '            <fylke>Viken</fylke>\n' +
                '            <fylkenummer>30</fylkenummer>\n' +
                '        </Organisasjon>\n' +
                '        <antalldrosjeloeyver>1</antalldrosjeloeyver>\n' +
                '        <language>1044</language>\n' +
                '    </Innsender>\n' +
                '    <Innhold>\n' +
                '        <DagligLeder>\n' +
                '            <foedselnummer>foedselnummer</foedselnummer>\n' +
                '            <fornavn>fornavn</fornavn>\n' +
                '            <etternavn>etternavn</etternavn>\n' +
                '            <epostadresse>epostadresse</epostadresse>\n' +
                '            <telefonnummer>telefonnummer</telefonnummer>\n' +
                '        </DagligLeder>\n' +
                '        <annenTransportleder>Nei</annenTransportleder>\n' +
                '        <Transportleder></Transportleder>\n' +
                '        <bekreftelseForTransportleder>Ja</bekreftelseForTransportleder>\n' +
                '        <beskrivTransportledersTilknytning>Ja</beskrivTransportledersTilknytning>\n' +
                '        <bekreftSamtykkeForetak>Ja</bekreftSamtykkeForetak>\n' +
                '        <bekreftBehandlingForetak>Ja</bekreftBehandlingForetak>\n' +
                '    </Innhold>\n' +
                '</melding>'
    }
}