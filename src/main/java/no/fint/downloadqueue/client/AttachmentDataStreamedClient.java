package no.fint.downloadqueue.client;

import no.altinn.attachmentdatastreamed.wsdl.GetAttachmentDataStreamedBasic;
import no.altinn.attachmentdatastreamed.wsdl.GetAttachmentDataStreamedBasicResponse;
import no.fint.downloadqueue.configuration.AltinnProperties;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Component;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.soap.client.core.SoapActionCallback;

import java.util.Optional;

@Component
public class AttachmentDataStreamedClient extends WebServiceGatewaySupport {
    private final AltinnProperties altinnProperties;

    public AttachmentDataStreamedClient(AltinnProperties altinnProperties) {
        this.altinnProperties = altinnProperties;

        setDefaultUri(altinnProperties.getAttachmentDataStreamedUri());
        setMarshaller(marshaller());
        setUnmarshaller(marshaller());

        ClientInterceptor[] interceptors = {new AltinnClientInterceptor()};
        setInterceptors(interceptors);
    }

    public Optional<byte[]> getAttachmentDataStreamed(int attachmentId) {
        GetAttachmentDataStreamedBasic request = new GetAttachmentDataStreamedBasic();

        request.setSystemUserName(altinnProperties.getSystemUsername());
        request.setSystemPassword(altinnProperties.getSystemPassword());
        request.setAttachmentId(attachmentId);

        GetAttachmentDataStreamedBasicResponse response = (GetAttachmentDataStreamedBasicResponse)
                getWebServiceTemplate().marshalSendAndReceive(request, new SoapActionCallback(AltinnSoapAction.GET_ATTACHMENT_DATA_STREAMED));

        return Optional.ofNullable(response)
                .map(GetAttachmentDataStreamedBasicResponse::getGetAttachmentDataStreamedBasicResult);
    }

    public static Jaxb2Marshaller marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath("no.altinn.attachmentdatastreamed.wsdl");
        return marshaller;
    }
}
