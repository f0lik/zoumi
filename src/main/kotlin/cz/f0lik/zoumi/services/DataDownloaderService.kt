package cz.f0lik.zoumi.services

import cz.f0lik.zoumi.model.Article
import cz.f0lik.zoumi.model.Comment
import cz.f0lik.zoumi.repository.ArticleRepository
import cz.f0lik.zoumi.repository.CommentRepository
import cz.f0lik.zoumi.utils.DbConnector
import org.hibernate.SessionFactory
import org.springframework.stereotype.Service
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime

import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import javax.persistence.EntityManagerFactory

@Service("dataDownloaderService")
class DataDownloaderService {
    @Autowired
    lateinit var articleRepository: ArticleRepository

    @Autowired
    lateinit var commentRepository: CommentRepository

    @Autowired
    lateinit var entityManagerFactory: EntityManagerFactory

    fun fetchData() {
        val dbConnector = DbConnector().getConnection() ?: throw IllegalAccessException("Connector is not initialized")

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

        val newFixedThreadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1)
        val articlesCreatedTime = articleRepository.getArticlesCreatedTime()
        var localMaxCreatedDate = LocalDateTime.of(2017, 12, 12, 0, 0, 0)

        if (articlesCreatedTime.isPresent) {
            Collections.sort(articlesCreatedTime.get(), Collections.reverseOrder())
            localMaxCreatedDate = articlesCreatedTime.get()[0]
        }

        val queryAllNewerArticles = "SELECT id_article, created, description, last_collection, name, url, keywords" +
                " FROM article WHERE created > '$localMaxCreatedDate'"

        val resultSet1 = dbConnector.createStatement().executeQuery(queryAllNewerArticles)
        val callableUpdateArticleTasks = ArrayList<Callable<Unit>>()

        while (resultSet1.next()) {
            val article = Article()
            article.id = resultSet1.getInt("id_article").toLong()
            article.createdDate = resultSet1.getTimestamp("created").toLocalDateTime()
            article.anotation = resultSet1.getString("description")
            article.lastFetchedDate = resultSet1.getTimestamp("last_collection").toLocalDateTime()
            article.title = resultSet1.getString("name")
            article.keyWords = resultSet1.getString("keywords").toLowerCase()
            article.url = resultSet1.getString("url")
            articleRepository.save(article)
            val callableSimilarityTask = Callable {
                updateArticleComments(article.id)
            }
            callableUpdateArticleTasks.add(callableSimilarityTask)
        }
        newFixedThreadPool.invokeAll(callableUpdateArticleTasks)
        newFixedThreadPool.shutdown()
        dbConnector.close()
    }

    fun updateCurrentArticles() {
        val dbConnector = DbConnector().getConnection() ?: throw IllegalAccessException("Connector not initialized")
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

        val queryAllNewerComments = "SELECT id_comment, text, created, author FROM comment" +
                " where id_article_article=$articleId " +
                "and created > '$localMaxCreatedDate'"

        val threadDbConnection = DbConnector().getConnection()
        val statement = threadDbConnection!!.createStatement()
        val resultSet = statement.executeQuery(queryAllNewerComments)

        val session = entityManagerFactory.unwrap(SessionFactory::class.java).openSession()
        val transaction = session.beginTransaction()
        var i = 0
        while (resultSet.next()) {
            val comment = Comment()
            comment.id = resultSet.getInt("id_comment").toLong()
            comment.author = resultSet.getString("author")
            comment.commentText = resultSet.getString("text")
            comment.created = resultSet.getTimestamp("created").toLocalDateTime()
            comment.article = articleRepository.findOne(articleId)
            comment.isNew = true
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
        threadDbConnection.close()
    }
}