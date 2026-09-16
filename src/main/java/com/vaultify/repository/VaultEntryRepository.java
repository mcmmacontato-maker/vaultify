package com.vaultify.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.vaultify.model.VaultEntry;

public interface VaultEntryRepository extends JpaRepository<VaultEntry, Long> {
        @Query("select entry from VaultEntry entry where entry.owner.id = :ownerId "
            + "and (lower(entry.site) like lower(concat('%', :search, '%')) "
            + "or lower(entry.category) like lower(concat('%', :search, '%')))")
        List<VaultEntry> search(@Param("ownerId") Long ownerId, @Param("search") String search);
    List<VaultEntry> findByOwnerId(Long ownerId);
}