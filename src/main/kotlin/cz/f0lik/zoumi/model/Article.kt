package cz.f0lik.zoumi.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonManagedReference
import org.hibernate.validator.constraints.NotBlank
import javax.persistence.*

@Entity
@Table(name = "articles")
@JsonIgnoreProperties(value = ["comments"], allowGetters = true)
class Article {

    constructor(title: String, anotation: String, url: String): this() {
        this.title = title
        this.anotation = anotation
        this.url = url
    }

    constructor()

    @Id
    @Column(name = "article_id")
    @GeneratedValue
    var id: Long? = null

    @NotBlank
    @Column(columnDefinition = "text")
    var title: String? = null

    @NotBlank
    @Column(columnDefinition = "text")
    var anotation: String? = null

    @NotBlank
    var url: String? = null

    @OneToMany(cascade = arrayOf(CascadeType.ALL), fetch = FetchType.LAZY, mappedBy = "article")
    @JsonManagedReference
    var comments: MutableSet<Comment>? = null
}