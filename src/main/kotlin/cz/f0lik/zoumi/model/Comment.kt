package cz.f0lik.zoumi.model

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import javax.persistence.*

@Entity
@Table(name = "comments")
@JsonIgnoreProperties(value = ["article"], allowGetters = true)
class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "comment_id")
    var id: Long? = null

    @Column(name = "author")
    var author: String? = null

    @Column(name = "commentText", columnDefinition = "text")
    var commentText: String? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    @JsonBackReference
    var article: Article? = null
}
