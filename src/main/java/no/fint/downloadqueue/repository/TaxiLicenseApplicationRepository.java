package no.fint.downloadqueue.repository;

import no.fint.downloadqueue.model.TaxiLicenseApplication;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaxiLicenseApplicationRepository extends MongoRepository<TaxiLicenseApplication, String> {
}
