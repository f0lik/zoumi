package cz.f0lik.zoumi.model

import javax.persistence.*

@Entity
@Table(name = "comments")
class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "comment_id")
    var id: Long? = null

    @Column(name = "author")
    var author: String? = null

    @Column(name = "commentText")
    var commentText: String? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    var article: Article? = null
}
