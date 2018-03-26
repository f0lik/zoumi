package cz.f0lik.zoumi.services

import cz.f0lik.zoumi.model.Article
import cz.f0lik.zoumi.model.Comment
import cz.f0lik.zoumi.repository.ArticleRepository
import cz.f0lik.zoumi.repository.CommentRepository
import cz.f0lik.zoumi.repository.SimilarCommentRepository
import spock.lang.Specification

class TextAnalysisServiceSpec extends Specification {
    SimilarCommentRepository similarCommentRepository = Mock(SimilarCommentRepository)
    ArticleRepository articleRepository = Mock(ArticleRepository)
    CommentRepository commentRepository = Mock(CommentRepository)
    TextAnalysisService textAnalysisService =
            new TextAnalysisService(articleRepository: articleRepository,
                    similarCommentRepository: similarCommentRepository, commentRepository: commentRepository)

    def "two similar comments should be marked as similar"() {
        when:
        def article1 = new Article()
        article1.id = 1
        def firstComment = new Comment(id: 1, commentText: "Ahoj, jsem uplne stejny", isNew: false, article: article1)
        def secondComment = new Comment(id: 2, commentText: "Ahoj, jsem uplne stejny", isNew: false, article: article1)

        def similarity = textAnalysisService.checkCommentSimilarity(firstComment, secondComment)

        then:
        similarity == 1
    }

    def "two different comments should not be marked as similar"() {
        when:
        def article1 = new Article()
        article1.id = 1
        def firstComment = new Comment(id: 1, commentText: "Ahoj, jsem uplne stejny", isNew: false, article: article1)
        def secondComment = new Comment(id: 2, commentText: "This code is *******", isNew: false, article: article1)

        def similarity = textAnalysisService.checkCommentSimilarity(firstComment, secondComment)

        then:
        similarity < TextAnalysisService.SIMILARITY_LIMIT
    }

    def "two different comments should be marked as similar"() {
        when:
        def article1 = new Article()
        article1.id = 1
        def firstComment = new Comment(id: 1, commentText: "Ahoj, jsem uplne stejny", isNew: false, article: article1)
        def secondComment = new Comment(id: 2, commentText: "Hoj, jsem uplne stejny", isNew: false, article: article1)

        def similarity = textAnalysisService.checkCommentSimilarity(firstComment, secondComment)

        then:
        similarity > TextAnalysisService.SIMILARITY_LIMIT
    }
}

