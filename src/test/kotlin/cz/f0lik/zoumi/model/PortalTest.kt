package cz.f0lik.zoumi.model

import cz.f0lik.zoumi.repository.PortalRepository
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@DataJpaTest
class PortalTest {

    @Autowired
    private val entityManager: TestEntityManager? = null

    @Autowired
    private val portalRepository: PortalRepository? = null

    private val PORTAL_NAME = "IDNES-CZ"

    private val PORTAL_URL = "http://www.idnes.cz"

    @Test
    fun shouldFindNoPortals() {
        val customers = portalRepository!!.findAll()
        Assert.assertThat(customers.size, CoreMatchers.`is`(CoreMatchers.equalTo(0)))
    }

    @Test
    fun shouldStorePortal() {
        val foundPortal = portalRepository!!.save(portalBuilder(PORTAL_NAME, PORTAL_URL))
        Assert.assertEquals(PORTAL_NAME, foundPortal.name)
        Assert.assertEquals(PORTAL_URL, foundPortal.url)
    }

    @Test
    fun shouldFindPortalById() {
        val firstPortal = portalBuilder(PORTAL_NAME, PORTAL_URL)
        entityManager!!.persist(firstPortal)
        val secondPortal = portalBuilder("Novinky", "http://www.novinky.cz")
        entityManager!!.persist(secondPortal)
        val foundArticle = portalRepository!!.findOne(secondPortal.id)

        Assert.assertThat(foundArticle, CoreMatchers.`is`(CoreMatchers.equalTo(secondPortal)))
    }

    @Test
    fun shouldContainComments() {
        val firstPortal = portalBuilder(PORTAL_NAME, PORTAL_URL)
        entityManager!!.persist(firstPortal)

        val newArticle = Article()
        newArticle.title = "Title"
        newArticle.anotation = "Super anotation"
        newArticle.url = "www.zoumi.cz"
        newArticle.portal = firstPortal

        val portalArticles = firstPortal.articles
        portalArticles!!.add(newArticle)

        Assert.assertEquals(1, firstPortal.articles!!.size)
    }

    private fun portalBuilder(name: String, url: String) : Portal {
        val portal = Portal()
        portal.name = name
        portal.url = url
        portal.articles = HashSet()
        return portal
    }
}