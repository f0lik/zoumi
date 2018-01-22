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
    private val entityManager: TestEntityManager? = null

    @Autowired
    private val repository: ArticleRepository? = null

    @Test
    fun should_find_no_articles_if_repository_is_empty() {
        val customers = repository!!.findAll()
        assertThat(customers.size, `is`(equalTo(0)))
    }

    @Test
    fun should_store_article() {
        val article = repository!!.save(Article("Title", "Anotation", "www.seznam.cz"))

        assertEquals("Title", article.title)
        assertEquals("Anotation", article.anotation)
        assertEquals("www.seznam.cz", article.url)
    }

    @Test
    fun should_find_article_by_id() {
        val article1 = Article("Title", "Anotation", "www.seznam.cz")
        entityManager!!.persist(article1)

        val article2 = Article("None", "My text", "www.idnes.cz")
        entityManager.persist(article2)

        val foundArticle = repository!!.findOne(article2.id)

        assertThat(foundArticle, `is`(equalTo(article2)))
    }
}