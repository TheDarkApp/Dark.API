package lab.maxb.dark_api.model.pojo

import org.springframework.beans.factory.annotation.Value
import java.util.*

interface RecognitionTaskListView {
    var image: String?
        @Value("#{target.images[0]}") get

    val owner_id: UUID
        @Value("#{target.owner.id}") get

    val reviewed: Boolean
    var id: UUID
}

interface RecognitionTaskFullView {
    var names: Set<String>?
    var images: List<String>?

    val owner_id: UUID
        @Value("#{target.owner.id}") get

    val reviewed: Boolean
    var id: UUID
}

interface RecognitionTaskImages {
    var images: List<String>?
}

class RecognitionTaskCreationDTO(
    var names: Set<String>
)