package com.wealthpro.productcatalog.repository;

import com.wealthpro.productcatalog.entity.ProductTerm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ProductTermRepository extends JpaRepository<ProductTerm, Long> {

    List<ProductTerm> findBySecuritySecurityId(Long securityId);

    List<ProductTerm> findByEffectiveFromBefore(LocalDate date);

    List<ProductTerm> findByEffectiveToIsNull();

    List<ProductTerm> findByEffectiveFromLessThanEqualAndEffectiveToGreaterThanEqual(
            LocalDate from, LocalDate to);
}