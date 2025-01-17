package com.example.FinanceDemoApi.financeDemo.Repository;
import com.example.FinanceDemoApi.financeDemo.Model.ContactSchema;
import com.example.FinanceDemoApi.financeDemo.Utility.ContactType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
@Repository
public interface ContactRepository extends JpaRepository<ContactSchema, Long> {
    Optional<ContactSchema> findByContactInfo(String contactInfo);

    Optional<ContactSchema> findByUserSchema_PrimaryKeyAndContactType(Long userSchemaId, ContactType contactType);


}
