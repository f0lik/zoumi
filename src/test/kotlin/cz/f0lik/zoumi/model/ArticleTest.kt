package cz.f0lik.zoumi.model

import cz.f0lik.zoumi.repository.ArticleRepository
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@DataJpaTest
class ArticleTest {

    @Autowired
    lateinit var entityManager: TestEntityManager

    @Autowired
    lateinit var repository: ArticleRepository

    @Test
    fun shouldFindNoArticles() {
        repository.deleteAll()
        val customers = repository.findAll()
        assertThat(customers.size, `is`(equalTo(0)))
    }

    @Test
    fun shouldContainArticle() {
        val article1 = Article("Title", "Anotation", "www.seznam.cz")
        article1.comments = HashSet()
        article1.id = 3
        val article = repository.save(article1)

        assertEquals("Title", article.title)
        assertEquals("Anotation", article.anotation)
        assertEquals("www.seznam.cz", article.url)
    }

    @Test
    fun shouldFindArticleById() {
        val article1 = Article("Title", "Anotation", "www.seznam.cz")
        article1.id = 1
        article1.comments = HashSet()
        entityManager.persist(article1)

        val article2 = Article("None", "My text", "www.idnes.cz")
        article2.id = 2
        entityManager.persist(article2)

        val foundArticle = repository.findOne(article2.id)

        assertThat(foundArticle, `is`(equalTo(article2)))
    }
}