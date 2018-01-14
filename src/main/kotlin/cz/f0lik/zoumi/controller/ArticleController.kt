package cz.f0lik.zoumi.controller

import cz.f0lik.zoumi.model.Article
import cz.f0lik.zoumi.model.Comment
import cz.f0lik.zoumi.repository.ArticleRepository
import cz.f0lik.zoumi.services.TextAnalysisService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/api")
class ArticleController {

    @Autowired
    var articleRepository: ArticleRepository? = null

    @Autowired
    private val textAnalysisService: TextAnalysisService? = null

    @PostMapping("/articles")
    fun createNote(@Valid @RequestBody note: Article): Article {
        return articleRepository!!.save(note)
    }

    @PostMapping("/articles/{id}")
    fun createComment(@PathVariable(value = "id") articleId: Long, @Valid @RequestBody comment: Comment): Comment {
        val article = articleRepository!!.findOne(articleId)

        val newComment = Comment()
        newComment.commentText = comment.commentText
        newComment.author = comment.author
        newComment.article = article

        val articleComments = article.comments
        articleComments!!.add(newComment)

        articleRepository!!.save(article)

        return newComment
    }

    @GetMapping("/articles")
    fun getAllNotes(): List<Article> {
        return articleRepository!!.findAll()
    }

    @GetMapping("/articles/similarity/{id1}/{id2}")
    fun compareArticles(@PathVariable(value = "id1") articleId1: Long, @PathVariable(value = "id2") articleId2: Long): Boolean {
        return textAnalysisService!!.compareArticles(articleId1, articleId2)
    }
}