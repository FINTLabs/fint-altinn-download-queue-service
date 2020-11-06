package no.fint.downloadqueue.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

@Data
public class AltinnForm {
    @JacksonXmlProperty(localName = "Innsender")
    private Submitter submitter;

    @Data
    public static class Submitter {
        @JacksonXmlProperty(localName = "language")
        private String language;

        @JacksonXmlProperty(localName = "Organisasjon")
        private Organisation organisation;
    }

    @Data
    public static class Organisation {
        @JacksonXmlProperty(localName = "organisasjonsnummer")
        private String organisationNumber;

        @JacksonXmlProperty(localName = "navn")
        private String name;

        @JacksonXmlProperty(localName = "Forretningsadresse")
        private Address businessAddress;

        @JacksonXmlProperty(localName = "telefonnummer")
        private String phone;

        @JacksonXmlProperty(localName = "epost")
        private String email;

        @JacksonXmlProperty(localName = "fylke")
        private String county;

        @JacksonXmlProperty(localName = "fylkenummer")
        private String countyNumber;
    }

    @Data
    public static class Address {
        @JacksonXmlProperty(localName = "adresse")
        private String address;

        @JacksonXmlProperty(localName = "postnummer")
        private String postCode;

        @JacksonXmlProperty(localName = "poststed")
        private String postalArea;
    }
}
