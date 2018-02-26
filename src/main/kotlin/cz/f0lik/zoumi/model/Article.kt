package cz.f0lik.zoumi.model

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonManagedReference
import org.hibernate.validator.constraints.NotBlank
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDateTime
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
    var id: Long? = null

    @NotBlank
    @Column(columnDefinition = "text")
    var title: String? = null

    @NotBlank
    @Column(columnDefinition = "text")
    var anotation: String? = null

    @NotBlank
    var url: String? = null

    @OneToMany(cascade = arrayOf(CascadeType.ALL), fetch = FetchType.EAGER, mappedBy = "article")
    @JsonManagedReference
    var comments: MutableSet<Comment>? = null

    var commentCount: Int? = null

    @Column(nullable = false, updatable = false)
    @CreatedDate
    var createdDate: LocalDateTime? = LocalDateTime.of(2017, 12, 12,0,0)

    @Column(nullable = false, updatable = false)
    @LastModifiedDate
    var lastFetchedDate: LocalDateTime? = LocalDateTime.of(2017, 12, 12,0,0)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portal_id")
    @JsonBackReference
    var portal: Portal? = null
}