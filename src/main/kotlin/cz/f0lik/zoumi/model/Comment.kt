package cz.f0lik.zoumi.model

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.springframework.data.annotation.CreatedDate
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "comments")
@JsonIgnoreProperties(value = ["article"], allowGetters = true)
class Comment {
    @Id
    @Column(name = "comment_id")
    var id: Long? = null

    @Column(name = "author")
    var author: String = ""

    @Column(name = "commentText", columnDefinition = "text")
    var commentText: String = ""

    @Column(nullable = false, updatable = false)
    @CreatedDate
    var created: LocalDateTime = LocalDateTime.of(2017, 12, 12,0,0)

    @Column
    var isNew: Boolean = true

    @Column
    var isCounted: Boolean = false

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    @JsonBackReference
    var article: Article? = null
}
