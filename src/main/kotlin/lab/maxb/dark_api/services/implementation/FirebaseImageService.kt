package lab.maxb.dark_api.services.implementation

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.cloud.StorageClient
import lab.maxb.dark_api.model.randomUUID
import lab.maxb.dark_api.services.ImageService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.context.event.EventListener
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import org.springframework.util.StringUtils
import org.springframework.web.multipart.MultipartFile
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.channels.Channels
import javax.imageio.ImageIO


@Service
class FirebaseImageService @Autowired constructor(
    private var properties: Properties
) : ImageService {
    private val bucket get() = StorageClient.getInstance().bucket()

    @EventListener
    fun init(event: ApplicationReadyEvent?) {
        val serviceAccount = ClassPathResource("FirebaseAccountKey.json")
        val options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount.inputStream))
            .setStorageBucket(properties.bucketName)
            .build()
        FirebaseApp.initializeApp(options)
    }

    override fun getUrl(name: String) =
        properties.imageUrl.format(name)

    @Throws(IOException::class)
    override fun save(file: MultipartFile): String {
        val name: String = file.originalFilename!!.randomFileName
        bucket.create(name, file.bytes, file.contentType)
        return name
    }

    @Throws(IOException::class)
    override fun save(bufferedImage: BufferedImage, originalFileName: String): String {
        val bytes: ByteArray = bufferedImage.toByteArray(originalFileName.extension)
        val name: String = originalFileName.randomFileName
        bucket.create(name, bytes)
        return name
    }

    override fun get(name: String): InputStream? {
        val reader = bucket.get(name)?.reader() ?: return null
        return Channels.newInputStream(reader)
    }

    @Throws(IOException::class)
    override fun delete(name: String) {
        if (name.isBlank())
            throw IOException("invalid file name")
        val blob = bucket[name] ?: throw IOException("file not found")
        blob.delete()
    }

    @ConfigurationProperties(prefix = "firebase")
    @ConstructorBinding
    data class Properties(
        val bucketName: String,
        val imageUrl: String
    )

    val String.extension get() = StringUtils.getFilenameExtension(this) ?: ""

    val String.randomFileName get()
    = "$randomUUID.$extension"

    @Throws(IOException::class)
    fun BufferedImage.toByteArray(format: String): ByteArray = ByteArrayOutputStream().use {
        ImageIO.write(this, format, it)
        it.flush()
        it.toByteArray()
    }
}