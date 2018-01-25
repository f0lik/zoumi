package cz.f0lik.zoumi.repository

import cz.f0lik.zoumi.model.Portal
import org.springframework.data.jpa.repository.JpaRepository

interface PortalRepository : JpaRepository<Portal, Long>