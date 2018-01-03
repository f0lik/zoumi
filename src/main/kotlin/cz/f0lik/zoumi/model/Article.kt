package cz.f0lik.zoumi.model

import org.hibernate.validator.constraints.NotBlank
import javax.persistence.*

@Entity
@Table(name = "articles")
class Article {
    @Id
    @Column(name = "article_id")
    @GeneratedValue
    var id: Long? = null

    @NotBlank
    var title: String? = null

    @NotBlank
    var anotation: String? = null

    @NotBlank
    var url: String? = null

    @OneToMany(cascade = arrayOf(CascadeType.ALL), fetch = FetchType.LAZY, mappedBy = "article")
    var comments: MutableSet<Comment>? = null
}