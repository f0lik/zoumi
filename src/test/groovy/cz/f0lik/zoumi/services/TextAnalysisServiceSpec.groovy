package cz.f0lik.zoumi.services

import cz.f0lik.zoumi.model.Article
import cz.f0lik.zoumi.model.Comment
import cz.f0lik.zoumi.repository.ArticleRepository
import cz.f0lik.zoumi.repository.SimilarCommentRepository
import org.thymeleaf.util.SetUtils
import spock.lang.Specification

class TextAnalysisServiceSpec extends Specification {
    SimilarCommentRepository similarCommentRepository = Mock(SimilarCommentRepository)
    ArticleRepository articleRepository = Mock(ArticleRepository)
    TextAnalysisService textAnalysisService =
            new TextAnalysisService(articleRepository: articleRepository,
                    similarCommentRepository: similarCommentRepository)

    def "CompareArticles should return true if exactly comments are likely to be similar"() {

        when:
        def article1 = new Article()
        article1.id = 1
        article1.comments = SetUtils.singletonSet(new Comment(id: 1, commentText: "Ahoj, jsem uplne stejny", article: article1))

        def article2 = new Article()
        article2.id = 2
        article2.comments = SetUtils.singletonSet(new Comment(id: 2, commentText: "Hoj, jsem uplne stejny", article: article2))
        articleRepository.findOne(1) >> article1
        articleRepository.findOne(2) >> article2
        similarCommentRepository.findAll() >> []
        similarCommentRepository.findByFirstSecondCommentId(1, 2) >> Optional.empty()
        def result = textAnalysisService.compareArticles(article1.id, article2.id)

        then:
        result
    }
}

