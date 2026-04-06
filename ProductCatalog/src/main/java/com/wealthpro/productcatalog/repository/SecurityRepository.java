package com.wealthpro.productcatalog.repository;

import com.wealthpro.productcatalog.entity.Security;
import com.wealthpro.productcatalog.enums.AssetClass;
import com.wealthpro.productcatalog.enums.SecurityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SecurityRepository extends JpaRepository<Security, Long> {

    Optional<Security> findBySymbol(String symbol);

    boolean existsBySymbol(String symbol);

    List<Security> findByAssetClass(AssetClass assetClass);

    List<Security> findByStatus(SecurityStatus status);

    List<Security> findByCountry(String country);

    List<Security> findByAssetClassAndStatus(AssetClass assetClass, SecurityStatus status);
}