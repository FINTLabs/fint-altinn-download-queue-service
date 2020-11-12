package no.fint.downloadqueue.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import no.altinn.downloadqueue.wsdl.AltinnFault;

@Data
@AllArgsConstructor
public class AltinnFaultPretty {
    private String altinnErrorMessage;
    private String altinnExtendedErrorMessage;
    private String altinnLocalizedErrorMessage;
    private String errorGuid;
    private Integer errorID;
    private String userGuid;
    private String userId;

    public static AltinnFaultPretty of(AltinnFault altinnFault) {
        return new AltinnFaultPretty(
                altinnFault.getAltinnErrorMessage().getValue(),
                altinnFault.getAltinnExtendedErrorMessage().getValue(),
                altinnFault.getAltinnLocalizedErrorMessage().getValue(),
                altinnFault.getErrorGuid().getValue(),
                altinnFault.getErrorID(),
                altinnFault.getUserGuid().getValue(),
                altinnFault.getUserId().getValue()
        );
    }
}
