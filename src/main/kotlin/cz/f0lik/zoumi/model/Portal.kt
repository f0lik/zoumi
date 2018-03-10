package cz.f0lik.zoumi.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonManagedReference
import org.hibernate.validator.constraints.NotBlank
import javax.persistence.*

@Entity
@Table(name = "portals")
@JsonIgnoreProperties(value = ["articles"], allowGetters = true)
class Portal {
    @Id
    @Column(name = "portal_id")
    @GeneratedValue
    var id: Long? = null

    @NotBlank
    @Column(columnDefinition = "text")
    var name: String = ""

    @NotBlank
    @Column(columnDefinition = "text")
    var url: String = ""

    @OneToMany(cascade = arrayOf(CascadeType.ALL), fetch = FetchType.EAGER, mappedBy = "portal")
    @JsonManagedReference
    var articles: MutableSet<Article>? = null
}