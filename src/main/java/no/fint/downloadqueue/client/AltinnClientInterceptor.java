package no.fint.downloadqueue.client;

import no.fint.downloadqueue.exception.AltinnFaultException;
import no.altinn.downloadqueue.wsdl.AltinnFault;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.*;

import javax.xml.bind.JAXBElement;
import javax.xml.transform.Source;
import java.util.Optional;

public class AltinnClientInterceptor implements org.springframework.ws.client.support.interceptor.ClientInterceptor {

    @Override
    public boolean handleRequest(MessageContext messageContext) throws WebServiceClientException {
        return true;
    }

    @Override
    public boolean handleResponse(MessageContext messageContext) throws WebServiceClientException {
        return true;
    }

    @Override
    public boolean handleFault(MessageContext messageContext) throws WebServiceClientException {
        SoapMessage soapMessage = (SoapMessage) messageContext.getResponse();
        SoapBody soapBody = soapMessage.getSoapBody();

        AltinnFault altinnFault = Optional.ofNullable(soapBody)
                .map(SoapBody::getFault)
                .map(SoapFault::getFaultDetail)
                .map(SoapFaultDetail::getDetailEntries)
                .map(detailEntries -> {
                    SoapFaultDetailElement soapFaultDetailElement = detailEntries.next();
                    Source source = soapFaultDetailElement.getSource();
                    Object object = DownloadQueueClient.marshaller().unmarshal(source);
                    return (AltinnFault) ((JAXBElement<?>) object).getValue();
                }).orElseThrow(AltinnFaultException::new);

        throw new AltinnFaultException(altinnFault);
    }

    @Override
    public void afterCompletion(MessageContext messageContext, Exception ex) throws WebServiceClientException {
    }
}
