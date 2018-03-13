package cz.f0lik.zoumi.services

import cz.f0lik.zoumi.model.Article
import cz.f0lik.zoumi.model.Comment
import cz.f0lik.zoumi.repository.ArticleRepository
import cz.f0lik.zoumi.repository.CommentRepository
import cz.f0lik.zoumi.repository.SimilarCommentRepository
import org.thymeleaf.util.SetUtils
import spock.lang.Specification

import java.time.LocalDateTime

class TextAnalysisServiceSpec extends Specification {
    SimilarCommentRepository similarCommentRepository = Mock(SimilarCommentRepository)
    ArticleRepository articleRepository = Mock(ArticleRepository)
    CommentRepository commentRepository = Mock(CommentRepository)
    TextAnalysisService textAnalysisService =
            new TextAnalysisService(articleRepository: articleRepository,
                    similarCommentRepository: similarCommentRepository, commentRepository: commentRepository)

    def "CompareArticles should return false if exactly comments are likely to be similar but they have been alredy compared"() {

        when:
        def article1 = new Article()
        article1.id = 1
        def firstComment = new Comment(id: 1, commentText: "Ahoj, jsem uplne stejny", isNew: false, article: article1)
        article1.comments = SetUtils.singletonSet(firstComment)

        def article2 = new Article()
        article2.id = 2
        def secondComment = new Comment(id: 2, commentText: "Hoj, jsem uplne stejny", isNew: false, article: article2)
        article2.comments = SetUtils.singletonSet(secondComment)
        articleRepository.findOne(1) >> article1
        articleRepository.findOne(2) >> article2
        commentRepository.getNewComments(article1.id) >> Optional.empty()

        similarCommentRepository.findAll() >> []
        similarCommentRepository.findByFirstSecondCommentId(1, 2) >> Optional.empty()

        def result = textAnalysisService.compareArticles(article1, article2)

        then:
        !result
    }

    def "CompareArticles should return true if exactly comments are likely to be similar"() {

        when:
        def article1 = new Article()
        article1.id = 1
        article1.comments = SetUtils.singletonSet(new Comment(id: 1, commentText: "Ahoj, jsem uplne stejny",
                created: LocalDateTime.now(), isNew: true, article: article1))

        def article2 = new Article()
        article2.id = 2
        article2.comments = SetUtils.singletonSet(new Comment(id: 2, commentText: "Hoj, jsem uplne stejny", isNew: true, article: article2))
        articleRepository.findOne(1) >> article1
        articleRepository.findOne(2) >> article2
        similarCommentRepository.findAll() >> []
        similarCommentRepository.findByFirstSecondCommentId(1, 2) >> Optional.empty()
        commentRepository.getNewComments(article1.id) >> Optional.of(article1.comments)

        def result = textAnalysisService.compareArticles(article1, article2)

        then:
        result
    }

    def "CompareArticles should return false if comments are totally different"() {

        when:
        def article1 = new Article()
        article1.id = 1
        article1.comments = SetUtils.singletonSet(new Comment(id: 1, commentText: "Ahoj, jsem uplne stejny",
                created: LocalDateTime.now(), isNew: true, article: article1))

        def article2 = new Article()
        article2.id = 2
        article2.comments = SetUtils.singletonSet(new Comment(id: 2, commentText: "Java je po Kotlinu nejlepsi jazyk",
                isNew: true, article: article2))
        articleRepository.findOne(1) >> article1
        articleRepository.findOne(2) >> article2
        similarCommentRepository.findAll() >> []
        similarCommentRepository.findByFirstSecondCommentId(1, 2) >> Optional.empty()
        commentRepository.getNewComments(article1.id) >> Optional.of(article1.comments)

        def result = textAnalysisService.compareArticles(article1, article2)

        then:
        !result
    }
}

