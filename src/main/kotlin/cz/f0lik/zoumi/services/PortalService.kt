package cz.f0lik.zoumi.services

import cz.f0lik.zoumi.model.Portal
import cz.f0lik.zoumi.repository.PortalRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.stream.Collectors

@Service
class PortalService {
    @Autowired
    lateinit var portalRepository: PortalRepository
    
    fun getPortalIdNameMap(): MutableMap<Long?, String>? {
        return portalRepository.findAll().stream().collect(Collectors.toMap(Portal::id, Portal::name))
    }
}