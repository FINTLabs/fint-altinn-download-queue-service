package no.fint.downloadqueue.client

import no.fint.downloadqueue.configuration.AltinnProperties
import no.fint.downloadqueue.exception.AltinnFaultException
import org.springframework.xml.transform.StringSource
import spock.lang.Specification
import javax.xml.transform.Source
import org.springframework.ws.test.client.MockWebServiceServer

import static org.springframework.ws.test.client.RequestMatchers.*
import static org.springframework.ws.test.client.ResponseCreators.*

class DownloadQueueClientSpec extends Specification {
    MockWebServiceServer mockServer

    AltinnProperties altinnProperties = Stub(AltinnProperties) {
        getDownloadQueueUri() >> 'http://localhost'
        getSystemUsername() >> 'username'
        getSystemPassword() >> 'password'
        getServiceCode() >> 'service-code'
    }

    DownloadQueueClient client = new DownloadQueueClient(altinnProperties)

    void setup() {
        mockServer = MockWebServiceServer.createServer(client)
    }

    def "getDownloadQueueItems return expected result"() {
        given:
        mockServer.expect(payload(downloadQueueRequestPayload())).andRespond(withPayload(downloadQueueResponsePayload()))

        when:
        def response = client.getDownloadQueueItems()

        then:
        mockServer.verify()
        response.size() == 1
        response.first().archiveReference.value == 'reference'
    }

    def "purgeItem return expected result"() {
        given:
        mockServer.expect(payload(purgeItemRequestPayload())).andRespond(withPayload(purgeItemResponsePayload()))

        when:
        def response = client.purgeItem('archive-reference')

        then:
        mockServer.verify()
        response.isPresent()
        response.get() == 'purged'
    }

    def "getArchivedFormTask return expected result"() {
        given:
        mockServer.expect(payload(archivedFormTaskRequestPayload())).andRespond(withPayload(archivedFormTaskResponsePayload()))

        when:
        def response = client.getArchivedFormTask('archive-reference')

        then:
        mockServer.verify()
        response.isPresent()
        response.get().archiveReference.value == 'archive-reference'
    }

    def "getFormSetPdf return expected result"() {
        given:
        mockServer.expect(payload(formSetPdfRequestPayload())).andRespond(withPayload(formSetPdfResponsePayload()))

        when:
        def response = client.getFormSetPdf('archive-reference', 0)

        then:
        mockServer.verify()
        response.length == 0
    }

    def "AltinnFault throws exception"() {
        given:
        mockServer.expect(payload(downloadQueueRequestPayload())).andRespond(withPayload(faultPayload()))

        when:
        client.getDownloadQueueItems()

        then:
        mockServer.verify()
        AltinnFaultException altinnFaultException = thrown()
        altinnFaultException.altinnFault.altinnErrorMessage.value == 'message'
    }

    Source downloadQueueRequestPayload() {
        return new StringSource(
                '<GetDownloadQueueItems xmlns="http://www.altinn.no/services/Archive/DownloadQueue/2012/08">' +
                        '<systemUserName>username</systemUserName>' +
                        '<systemPassword>password</systemPassword>' +
                        '<serviceCode>service-code</serviceCode>' +
                        '</GetDownloadQueueItems>'
        )
    }

    Source downloadQueueResponsePayload() {
        return new StringSource(
                '<x:GetDownloadQueueItemsResponse xmlns:x="http://www.altinn.no/services/Archive/DownloadQueue/2012/08" ' +
                        'xmlns="http://schemas.altinn.no/services/Archive/DownloadQueue/2012/08">' +
                        '<x:GetDownloadQueueItemsResult>' +
                        '<DownloadQueueItemBE>' +
                        '<ArchiveReference>reference</ArchiveReference>' +
                        '</DownloadQueueItemBE>' +
                        '</x:GetDownloadQueueItemsResult>' +
                        '</x:GetDownloadQueueItemsResponse>'
        )
    }

    Source purgeItemRequestPayload() {
        return new StringSource(
                '<PurgeItem xmlns="http://www.altinn.no/services/Archive/DownloadQueue/2012/08">' +
                        '<systemUserName>username</systemUserName>' +
                        '<systemPassword>password</systemPassword>' +
                        '<archiveReference>archive-reference</archiveReference>' +
                        '</PurgeItem>'
        )
    }

    Source purgeItemResponsePayload() {
        return new StringSource(
                '<PurgeItemResponse xmlns="http://www.altinn.no/services/Archive/DownloadQueue/2012/08">' +
                        '<PurgeItemResult>purged</PurgeItemResult>' +
                        '</PurgeItemResponse>'
        )
    }

    Source archivedFormTaskRequestPayload() {
        return new StringSource(
                '<GetArchivedFormTaskBasicDQ xmlns="http://www.altinn.no/services/Archive/DownloadQueue/2012/08">' +
                        '<systemUserName>username</systemUserName>' +
                        '<systemPassword>password</systemPassword>' +
                        '<archiveReference>archive-reference</archiveReference>' +
                        '</GetArchivedFormTaskBasicDQ>'
        )
    }

    Source archivedFormTaskResponsePayload() {
        return new StringSource(
                '<x:GetArchivedFormTaskBasicDQResponse xmlns:x="http://www.altinn.no/services/Archive/DownloadQueue/2012/08" ' +
                        'xmlns="http://schemas.altinn.no/services/Archive/ReporteeArchive/2012/08">' +
                        '<x:GetArchivedFormTaskBasicDQResult>' +
                        '<ArchiveReference>archive-reference</ArchiveReference>' +
                        '</x:GetArchivedFormTaskBasicDQResult>' +
                        '</x:GetArchivedFormTaskBasicDQResponse>'
        )
    }

    Source formSetPdfRequestPayload() {
        return new StringSource(
                '<GetFormSetPdfBasic xmlns="http://www.altinn.no/services/Archive/DownloadQueue/2012/08">' +
                        '<systemName>username</systemName>' +
                        '<systemPassword>password</systemPassword>' +
                        '<archiveReference>archive-reference</archiveReference>' +
                        '<languageId>0</languageId>' +
                        '</GetFormSetPdfBasic>'
        )
    }

    Source formSetPdfResponsePayload() {
        return new StringSource(
                '<x:GetFormSetPdfBasicResponse xmlns:x="http://www.altinn.no/services/Archive/DownloadQueue/2012/08"> ' +
                        '<x:GetFormSetPdfBasicResult>' +
                        '</x:GetFormSetPdfBasicResult>' +
                        '</x:GetFormSetPdfBasicResponse>'
        )
    }

    Source faultPayload() {
        return new StringSource(
                '<x:Fault xmlns:x="http://schemas.xmlsoap.org/soap/envelope/">' +
                        '<detail>' +
                        '<AltinnFault xmlns="http://www.altinn.no/services/common/fault/2009/10">' +
                        '<AltinnErrorMessage>message</AltinnErrorMessage>' +
                        '</AltinnFault>' +
                        '</detail>' +
                        '</x:Fault>'
        )
    }
}
