package cz.f0lik.zoumi.model

import javax.persistence.*

@Entity
@Table(name = "similar_comments")
class SimilarComment {
    @Id
    @Column(name = "similar_comment_id")
    @GeneratedValue
    var id: Long? = null

    var firstCommentId: Long? = null

    var firstCommentArticleId: Long? = null

    var secondCommentId: Long? = null

    var secondCommentArticleId: Long? = null

    var similarity: Int? = null
}