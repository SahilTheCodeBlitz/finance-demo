package com.example.FinanceDemoApi.financeDemo.Repository;
import com.example.FinanceDemoApi.financeDemo.Model.OtpEntity;
import com.example.FinanceDemoApi.financeDemo.Model.UserSchema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface OtpEntityRepository extends JpaRepository<OtpEntity, Long> {
    Optional<OtpEntity> findByUserId(UserSchema userId);

}
