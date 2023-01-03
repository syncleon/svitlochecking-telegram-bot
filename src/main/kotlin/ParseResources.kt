import org.yaml.snakeyaml.Yaml
import java.io.InputStream

class ParseResources {

    private val fileContent: InputStream? = object {}.javaClass
        .classLoader
        .getResourceAsStream("application.yml")

    fun parse(): MutableMap<String, Any> {
        val yaml = Yaml()
        return yaml.load(fileContent)
    }
}