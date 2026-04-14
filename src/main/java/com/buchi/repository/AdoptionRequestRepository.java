package com.buchi.repository;

import com.buchi.entity.AdoptionRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface AdoptionRequestRepository extends JpaRepository<AdoptionRequest, Long> {

    // Used to prevent duplicate adoption requests
    boolean existsByCustomerIdAndPetId(Long customerId, Long petId);

    // Fetch with customer and pet eagerly to avoid N+1
    // Ordered ASC = oldest first (per spec)
    @Query("""
        SELECT a FROM AdoptionRequest a
        JOIN FETCH a.customer c
        JOIN FETCH a.pet p
        WHERE a.requestedAt BETWEEN :from AND :to
        ORDER BY a.requestedAt ASC
    """)
    List<AdoptionRequest> findByDateRange(
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to);

    // Report: pet type breakdown using JPQL (database-agnostic)
    @Query("""
        SELECT p.type, COUNT(a) FROM AdoptionRequest a
        JOIN a.pet p
        WHERE a.requestedAt BETWEEN :from AND :to
        GROUP BY p.type
        ORDER BY COUNT(a) DESC
    """)
    List<Object[]> countByPetTypeInRange(
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to);

    // Report: weekly breakdown using native PostgreSQL DATE_TRUNC
    @Query(value = """
        SELECT
            DATE_TRUNC('week', requested_at)::date AS week_start,
            COUNT(*) AS total
        FROM adoption_requests
        WHERE requested_at BETWEEN :from AND :to
        GROUP BY week_start
        ORDER BY week_start ASC
    """, nativeQuery = true)
    List<Object[]> countByWeekInRange(
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to);
}