package no.fint.downloadqueue.exception;

import no.altinn.downloadqueue.wsdl.AltinnFault;

public class AltinnFaultException extends RuntimeException {

    private AltinnFault altinnFault;

    public AltinnFaultException() {
    }

    public AltinnFaultException(AltinnFault altinnFault) {
        this.altinnFault = altinnFault;
    }

    public AltinnFault getAltinnFault() {
        return altinnFault;
    }
}
