package cz.f0lik.zoumi.model

interface CommentDTO {
    fun getCommentId(): Long
    fun getCommentText(): String
    fun getCommentArticleId(): Long
}