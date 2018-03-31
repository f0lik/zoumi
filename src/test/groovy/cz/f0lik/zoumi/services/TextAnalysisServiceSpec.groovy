package cz.f0lik.zoumi.services

import cz.f0lik.zoumi.model.CommentDTO
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
        def comment1 = new TestComment(1, "Ahoj, jsem uplne stejny", 1)
        def comment2 = new TestComment(2, "Ahoj, jsem uplne stejny", 1)
        def similarity = textAnalysisService.checkCommentSimilarity(comment1, comment2)

        then:
        similarity == 1
    }

    def "two different comments should not be marked as similar"() {
        when:
        def comment1 = new TestComment(1, "Ahoj, jsem uplne stejny", 1)
        def comment2 = new TestComment(2, "This code is *******", 1)
        def similarity = textAnalysisService.checkCommentSimilarity(comment1, comment2)

        then:
        similarity < TextAnalysisService.SIMILARITY_LIMIT
    }

    def "two different comments should be marked as similar"() {
        when:
        def comment1 = new TestComment(1, "Ahoj, jsem uplne stejny", 1)
        def comment2 = new TestComment(2, "Hoj, jsem uplne stejny", 1)
        def similarity = textAnalysisService.checkCommentSimilarity(comment1, comment2)

        then:
        similarity > TextAnalysisService.SIMILARITY_LIMIT
    }
}

class TestComment implements CommentDTO {
    private String text
    private long id
    private long articleId

    TestComment(long id, String text, long articleId) {
        this.id = id
        this.text = text
        this.articleId = articleId
    }

    @Override
    long getCommentId() {
        return id
    }

    @Override
    String getCommentText() {
        return text
    }

    @Override
    long getCommentArticleId() {
        return articleId
    }
}

