package cz.f0lik.zoumi.model

import org.hibernate.validator.constraints.NotBlank
import javax.persistence.*

@Entity
@Table(name = "similar_comments")
class SimilarComment {
    @Id
    @Column(name = "similar_comment_id")
    @GeneratedValue
    var id: Long? = null

    @NotBlank
    var firstCommentId: Long? = null

    @NotBlank
    var secondCommentId: Long? = null
}