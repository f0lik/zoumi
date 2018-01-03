package cz.f0lik.zoumi.repository

import cz.f0lik.zoumi.model.Comment
import org.springframework.data.jpa.repository.JpaRepository

interface CommentRepository : JpaRepository<Comment, Long>