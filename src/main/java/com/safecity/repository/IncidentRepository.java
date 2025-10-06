package com.safecity.repository;

import com.safecity.model.Incident;
import com.safecity.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {
    
    List<Incident> findByUserOrderByCreatedAtDesc(User user);
    
    List<Incident> findByStatusOrderByCreatedAtDesc(Incident.Status status);
    
    @Query("SELECT i FROM Incident i WHERE " +
           "(:latitude - i.latitude) * (:latitude - i.latitude) + " +
           "(:longitude - i.longitude) * (:longitude - i.longitude) <= :radiusSquared " +
           "ORDER BY i.createdAt DESC")
    List<Incident> findIncidentsNearLocation(@Param("latitude") Double latitude, 
                                           @Param("longitude") Double longitude, 
                                           @Param("radiusSquared") Double radiusSquared);
    
    long countByStatus(Incident.Status status);
    
}

