package no.fint.downloadqueue.client

import no.fint.downloadqueue.configuration.AltinnProperties
import no.fint.downloadqueue.exception.AltinnFaultException
import org.springframework.ws.test.client.MockWebServiceServer
import org.springframework.xml.transform.StringSource
import spock.lang.Specification

import javax.xml.transform.Source

import static org.springframework.ws.test.client.RequestMatchers.payload
import static org.springframework.ws.test.client.ResponseCreators.withPayload

class AttachmentDataStreamedClientSpec extends Specification {
    MockWebServiceServer mockServer

    AltinnProperties altinnProperties = Stub(AltinnProperties) {
        getAttachmentDataStreamedUri() >> 'http://localhost'
        getSystemUsername() >> 'username'
        getSystemPassword() >> 'password'
    }

    AttachmentDataStreamedClient client = new AttachmentDataStreamedClient(altinnProperties)

    void setup() {
        mockServer = MockWebServiceServer.createServer(client)
    }

    def "getAttachmentDataStreamed return expected result"() {
        given:
        mockServer.expect(payload(attachmentDataStreamedRequestPayload())).andRespond(withPayload(attachmentDataStreamedResponsePayload()))

        when:
        def response = client.getAttachmentDataStreamed(0)

        then:
        mockServer.verify()
        response.isPresent()
    }

    def "AltinnFault throws exception"() {
        given:
        mockServer.expect(payload(attachmentDataStreamedRequestPayload())).andRespond(withPayload(faultPayload()))

        when:
        client.getAttachmentDataStreamed(0)

        then:
        mockServer.verify()
        AltinnFaultException altinnFaultException = thrown()
        altinnFaultException.altinnFault.altinnErrorMessage.value == 'message'
    }

    Source attachmentDataStreamedRequestPayload() {
        return new StringSource(
                '<x:GetAttachmentDataStreamedBasic xmlns:x="http://www.altinn.no/services/Archive/ServiceOwnerArchive/2013/06">' +
                        '<x:systemUserName>username</x:systemUserName>' +
                        '<x:systemPassword>password</x:systemPassword>' +
                        '<x:attachmentId>0</x:attachmentId>' +
                        '</x:GetAttachmentDataStreamedBasic>'
        )
    }

    Source attachmentDataStreamedResponsePayload() {
        return new StringSource(
                '<x:GetAttachmentDataStreamedBasicResponse xmlns:x="http://www.altinn.no/services/Archive/ServiceOwnerArchive/2013/06">' +
                        '<x:GetAttachmentDataStreamedBasicResult>' +
                        '</x:GetAttachmentDataStreamedBasicResult>' +
                        '</x:GetAttachmentDataStreamedBasicResponse>'
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
