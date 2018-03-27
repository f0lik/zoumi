package cz.f0lik.zoumi.services

import cz.f0lik.zoumi.model.Article
import cz.f0lik.zoumi.model.Comment
import cz.f0lik.zoumi.model.Portal
import cz.f0lik.zoumi.repository.ArticleRepository
import cz.f0lik.zoumi.repository.CommentRepository
import cz.f0lik.zoumi.repository.PortalRepository
import cz.f0lik.zoumi.utils.DbConnector
import org.hibernate.SessionFactory
import org.springframework.stereotype.Service
import org.springframework.beans.factory.annotation.Autowired
import java.sql.Timestamp
import java.time.LocalDateTime

import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.stream.Collectors
import javax.persistence.EntityManagerFactory
import kotlin.collections.ArrayList

@Service("dataDownloaderService")
class DataDownloaderService {
    @Autowired
    lateinit var articleRepository: ArticleRepository

    @Autowired
    lateinit var portalRepository: PortalRepository

    @Autowired
    lateinit var commentRepository: CommentRepository

    @Autowired
    lateinit var entityManagerFactory: EntityManagerFactory

    fun fetchData() {
        updatePortals()
        val dbConnector = DbConnector.getInstance().getConnection() ?: throw IllegalAccessException("Connector is not initialized")

        val queryArticleCount = "SELECT COUNT(*) AS article_count FROM ARTICLE"
        val resultSet = dbConnector.createStatement().executeQuery(queryArticleCount)
        if (!resultSet.next()) {
            throw IllegalStateException("Missing data")
        }
        val remoteArticleCount = resultSet.getInt("article_count")
        val localArticleCount = articleRepository.count()

        if (remoteArticleCount <= localArticleCount) {
            return
        }

        val articlesCreatedTime = articleRepository.getArticlesCreatedTime()
        var localMaxCreatedDate = LocalDateTime.of(2017, 12, 12, 0, 0, 0)

        if (articlesCreatedTime.isPresent) {
            Collections.sort(articlesCreatedTime.get(), Collections.reverseOrder())
            localMaxCreatedDate = articlesCreatedTime.get()[0]
        }

        val newerArticlesStatement = dbConnector.prepareStatement("SELECT id_article, created," +
                " description, id_portal_pkey, last_collection, name, url, keywords FROM article WHERE created > ?")

        newerArticlesStatement.setTimestamp(1, Timestamp.valueOf(localMaxCreatedDate))

        val newArticles = newerArticlesStatement.executeQuery()

        while (newArticles.next()) {
            val article = Article()
            article.id = newArticles.getLong("id_article")
            article.createdDate = newArticles.getTimestamp("created").toLocalDateTime()
            article.anotation = newArticles.getString("description")
            article.lastFetchedDate = newArticles.getTimestamp("last_collection").toLocalDateTime()
            article.title = newArticles.getString("name")
            article.keyWords = newArticles.getString("keywords").toLowerCase()
            article.url = newArticles.getString("url")
            val articlePortal = portalRepository.findOne(newArticles.getLong("id_portal_pkey"))
            article.portal = articlePortal
            articleRepository.save(article)
            updateArticleComments(article.id)
        }
        dbConnector.close()
    }

    fun updatePortals() {
        val dbConnector = DbConnector.getInstance().getConnection() ?: throw IllegalAccessException("Connector is not initialized")
        val queryPortalCount = "SELECT COUNT(*) AS portal_count FROM PORTAL"
        val remotePortalCountResult = dbConnector.createStatement().executeQuery(queryPortalCount)
        if (!remotePortalCountResult.next()) {
            throw IllegalStateException("Missing data")
        }
        val remotePortalCount = remotePortalCountResult.getInt("portal_count")
        val localPortalCount = portalRepository.count()

        if (remotePortalCount <= localPortalCount) {
            return
        }

        val queryPortalIDs = "SELECT id_portal FROM PORTAL"
        val portalIds = dbConnector.createStatement().executeQuery(queryPortalIDs)
        val remoteIds = ArrayList<Long>()
        while (portalIds.next()) {
            remoteIds.add(portalIds.getLong("id_portal"))
        }

        val localPortals = portalRepository.findAll().orEmpty()
        val localPortalIds = localPortals.stream().map(Portal::id).collect(Collectors.toList())

        remoteIds.removeAll(localPortalIds)

        if(remoteIds.size == 0) {
            return
        }
        val queryNotLocallyKnownPortals = "SELECT id_portal, last_collection, name, url_portal FROM PORTAL"
        val resultNewPortals = dbConnector.createStatement().executeQuery(queryNotLocallyKnownPortals)

        while (resultNewPortals.next()) {
            val portal = Portal()
            portal.id = resultNewPortals.getLong("id_portal")
            portal.lastChecked = resultNewPortals.getTimestamp("last_collection").toLocalDateTime()
            portal.name = resultNewPortals.getString("name")
            portal.url = resultNewPortals.getString("url_portal")
            portalRepository.save(portal)
        }
    }

    fun updateCurrentArticles() {
        val dbConnector = DbConnector.getInstance().getConnection() ?: throw IllegalAccessException("Connector not initialized")
        val articles = articleRepository.findAll()
        val idToDateMap = articles.map { it.id to it.lastFetchedDate }

        val newFixedThreadPool = Executors.newFixedThreadPool(Runtime.getRuntime()
                .availableProcessors() - 1)
        val callableSimilarityTasks = ArrayList<Callable<Unit>>()

        idToDateMap.forEach { (articleId, lastFetchedDate) ->
            val statement = dbConnector.createStatement()
            val query = "SELECT last_collection as lct from article where id_article=$articleId"
            val resultSet = statement.executeQuery(query)
            if (!resultSet.next()) {
                throw IllegalStateException("Missing data")
            }
            val lastDateSource = resultSet.getTimestamp("lct").toLocalDateTime()
            if (lastDateSource > lastFetchedDate) {
                val callableSimilarityTask = Callable {
                    updateArticleComments(articleId)
                }
                callableSimilarityTasks.add(callableSimilarityTask)
            }
        }
        newFixedThreadPool.invokeAll(callableSimilarityTasks)
        newFixedThreadPool.shutdown()
        dbConnector.close()
    }

    private fun updateArticleComments(articleId: Long?) {
        val commentsCreatedTime = commentRepository.getCommentsCreatedTime(articleId!!)
        var localMaxCreatedDate = LocalDateTime.of(2017, 12, 12, 0, 0, 0)

        if (commentsCreatedTime.isPresent) {
            Collections.sort(commentsCreatedTime.get(), Collections.reverseOrder())
            localMaxCreatedDate = commentsCreatedTime.get()[0]
        }

        val threadDbConnection = DbConnector.getInstance().getConnection()

        val prepareStatement = threadDbConnection!!.prepareStatement("SELECT id_comment, text, created, author FROM comment" +
                " where id_article_article=?" +
                "and created > ?")

        prepareStatement.setLong(1, articleId)
        prepareStatement.setTimestamp(2, Timestamp.valueOf(localMaxCreatedDate))
        val newComments = prepareStatement.executeQuery()

        val session = entityManagerFactory.unwrap(SessionFactory::class.java).openSession()
        val transaction = session.beginTransaction()
        var i = 0
        while (newComments.next()) {
            val commentText = newComments.getString("text")
            if (commentText.isEmpty()) {
                continue
            }
            val comment = Comment()
            comment.id = newComments.getLong("id_comment")
            comment.author = newComments.getString("author")
            comment.commentText = commentText
            comment.created = newComments.getTimestamp("created").toLocalDateTime()
            comment.article = articleRepository.findOne(articleId)
            comment.isNew = true
            comment.isCounted = false
            session.save(comment)
            if ( i % 30 == 0 ) {
                session.flush()
                session.clear()
                i=0
            }
            i++
        }
        transaction.commit()
        session.close()
    }
}