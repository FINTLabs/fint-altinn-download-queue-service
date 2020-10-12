package no.fint.downloadqueue.client

import no.fint.downloadqueue.configuration.AltinnConfiguration
import no.fint.downloadqueue.exception.AltinnFaultException
import org.springframework.xml.transform.StringSource
import spock.lang.Specification
import javax.xml.transform.Source
import org.springframework.ws.test.client.MockWebServiceServer

import static org.springframework.ws.test.client.RequestMatchers.*
import static org.springframework.ws.test.client.ResponseCreators.*

class DownloadQueueClientSpec extends Specification {
    AltinnConfiguration altinnConfiguration = new AltinnConfiguration(
            defaultUri: 'http://localhost',
            systemUsername: 'username',
            systemPassword: 'password',
            serviceCode: 'code'
    )

    DownloadQueueClient client = new DownloadQueueClient(altinnConfiguration)

    MockWebServiceServer mockServer

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
        response.isPresent()
        response.get().downloadQueueItemBE.size() == 1
        response.get().downloadQueueItemBE.first().archiveReference.value == 'reference'
    }

    def "purgeItem return expected result"() {
        given:
        mockServer.expect(payload(purgeItemRequestPayload())).andRespond(withPayload(purgeItemResponsePayload()))

        when:
        def response = client.purgeItem('reference')

        then:
        mockServer.verify()
        response.isPresent()
        response.get() == 'purged'
    }

    def "getArchivedFormTaskDQ return expected result"() {
        given:
        mockServer.expect(payload(archivedFormTaskDQRequestPayload)).andRespond(withPayload(archivedFormTaskDQResponsePayload))

        when:
        def response = client.getArchivedFormTask('reference')

        then:
        mockServer.verify()
        response.isPresent()
        response.get().archiveReference.value == 'reference'
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
                '<x:GetDownloadQueueItems xmlns:x="http://www.altinn.no/services/Archive/DownloadQueue/2012/08" ' +
                        'xmlns="http://schemas.microsoft.com/2003/10/Serialization/">' +
                        '<string>username</string>' +
                        '<string>password</string>' +
                        '<string>code</string>' +
                        '</x:GetDownloadQueueItems>'
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
                '<x:PurgeItem xmlns:x="http://www.altinn.no/services/Archive/DownloadQueue/2012/08" ' +
                        'xmlns="http://schemas.microsoft.com/2003/10/Serialization/">' +
                        '<string>username</string>' +
                        '<string>password</string>' +
                        '<string>reference</string>' +
                        '</x:PurgeItem>'
        )
    }

    Source purgeItemResponsePayload() {
        return new StringSource(
                '<PurgeItemResponse xmlns="http://www.altinn.no/services/Archive/DownloadQueue/2012/08">' +
                        '<PurgeItemResult>purged</PurgeItemResult>' +
                        '</PurgeItemResponse>'
        )
    }

    Source getArchivedFormTaskDQRequestPayload() {
        return new StringSource(
                '<x:GetArchivedFormTaskBasicDQ xmlns:x="http://www.altinn.no/services/Archive/DownloadQueue/2012/08" ' +
                        'xmlns="http://schemas.microsoft.com/2003/10/Serialization/">' +
                        '<string>username</string>' +
                        '<string>password</string>' +
                        '<string>reference</string>' +
                        '</x:GetArchivedFormTaskBasicDQ>'
        )
    }

    Source getArchivedFormTaskDQResponsePayload() {
        return new StringSource(
                '<x:GetArchivedFormTaskBasicDQResponse xmlns:x="http://www.altinn.no/services/Archive/DownloadQueue/2012/08" ' +
                        'xmlns="http://schemas.altinn.no/services/Archive/ReporteeArchive/2012/08">' +
                        '<x:GetArchivedFormTaskBasicDQResult>' +
                        '<ArchiveReference>reference</ArchiveReference>' +
                        '</x:GetArchivedFormTaskBasicDQResult>' +
                        '</x:GetArchivedFormTaskBasicDQResponse>'
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
