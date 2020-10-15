package no.fint.downloadqueue.client;

import no.fint.downloadqueue.configuration.AltinnProperties;
import no.altinn.downloadqueue.wsdl.*;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Component;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.soap.client.core.SoapActionCallback;

import javax.xml.bind.JAXBElement;
import java.util.Optional;

@Component
public class DownloadQueueClient extends WebServiceGatewaySupport {
    private final ObjectFactory objectFactory = new ObjectFactory();
    private final AltinnProperties altinnProperties;

    public DownloadQueueClient(AltinnProperties altinnProperties) {
        this.altinnProperties = altinnProperties;

        setDefaultUri(altinnProperties.getDefaultUri());
        setMarshaller(marshaller());
        setUnmarshaller(marshaller());

        ClientInterceptor[] interceptors = {new DownloadQueueClientInterceptor()};
        setInterceptors(interceptors);
    }

    public Optional<DownloadQueueItemBEList> getDownloadQueueItems() {
        GetDownloadQueueItems request = new GetDownloadQueueItems();

        request.setServiceCode(objectFactory.createString(altinnProperties.getServiceCode()));
        request.setSystemUserName(objectFactory.createString(altinnProperties.getSystemUsername()));
        request.setSystemPassword(objectFactory.createString(altinnProperties.getSystemPassword()));

        GetDownloadQueueItemsResponse response = (GetDownloadQueueItemsResponse) getResponse(request, DownloadQueueSoapAction.GET_DOWNLOAD_QUEUE_ITEMS);

        return Optional.ofNullable(response)
                .map(GetDownloadQueueItemsResponse::getGetDownloadQueueItemsResult)
                .map(JAXBElement::getValue);
    }

    public Optional<String> purgeItem(String archiveReference) {
        PurgeItem request = new PurgeItem();

        request.setArchiveReference(objectFactory.createString(archiveReference));
        request.setSystemUserName(objectFactory.createString(altinnProperties.getSystemUsername()));
        request.setSystemPassword(objectFactory.createString(altinnProperties.getSystemPassword()));

        PurgeItemResponse response = (PurgeItemResponse) getResponse(request, DownloadQueueSoapAction.PURGE_ITEM);

        return Optional.ofNullable(response)
                .map(PurgeItemResponse::getPurgeItemResult)
                .map(JAXBElement::getValue);
    }

    public Optional<ArchivedFormTaskDQBE> getArchivedFormTask(String archiveReference) {
        GetArchivedFormTaskBasicDQ request = new GetArchivedFormTaskBasicDQ();

        request.setArchiveReference(objectFactory.createString(archiveReference));
        request.setSystemUserName(objectFactory.createString(altinnProperties.getSystemUsername()));
        request.setSystemPassword(objectFactory.createString(altinnProperties.getSystemPassword()));

        GetArchivedFormTaskBasicDQResponse response = (GetArchivedFormTaskBasicDQResponse) getResponse(request, DownloadQueueSoapAction.GET_ARCHIVED_FORM_TASK);

        return Optional.ofNullable(response)
                .map(GetArchivedFormTaskBasicDQResponse::getGetArchivedFormTaskBasicDQResult)
                .map(JAXBElement::getValue);
    }

    public Optional<byte[]> getFormSetPdf(String archiveReference) {
        GetFormSetPdfBasic request = new GetFormSetPdfBasic();

        request.setArchiveReference(archiveReference);
        request.setSystemName(altinnProperties.getSystemUsername());
        request.setSystemPassword(altinnProperties.getSystemPassword());

        /*
        request.setLanguageId();
        request.setDataFormatId();
        request.setDataFormatVersion();
         */

        GetFormSetPdfBasicResponse response = (GetFormSetPdfBasicResponse) getResponse(request, DownloadQueueSoapAction.GET_FORM_SET_PDF);

        return Optional.ofNullable(response)
                .map(GetFormSetPdfBasicResponse::getGetFormSetPdfBasicResult)
                .map(JAXBElement::getValue);
    }

    private Object getResponse(Object request, String soapAction) {
        return getWebServiceTemplate().marshalSendAndReceive(request, new SoapActionCallback(soapAction));
    }

    public static Jaxb2Marshaller marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath("no.altinn.downloadqueue.wsdl");
        return marshaller;
    }
}
