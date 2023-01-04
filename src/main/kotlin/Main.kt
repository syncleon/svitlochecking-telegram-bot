import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.entities.ChatId
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*


private val parseResources = ParseResources()
private val map = parseResources.parse()
private val channelId: ChatId = ChatId.ChannelUsername(map["channelId"].toString())
private val ip: String = map["ip"].toString()
private val botToken: String = map["token"].toString()

private val command = arrayOf("ping", "-w", "5", ip)

private var bufferTime: Long = 0L

const val WELCOME_TEXT: String = "\nВітаю! Цей бот створенний для перевірки наявності електроенергії "
const val ENABLED_TEXT: String = "\nСвітло є \uD83E\uDD73"
const val DISABLED_TEXT: String = "\nСвітла нема \uD83D\uDE14"
const val TIME_ENABLED_TEXT: String = "\nЧас включення: "
const val TIME_ENABLED_PERIOD_TEXT: String = "\nСвітла не було протягом: "
const val TIME_DISABLED_TEXT: String = "\nЧас відключення: "
const val TIME_DISABLED_PERIOD_TEXT: String = "\nСвітло було протягом: "

fun main() {
    bufferTime = System.currentTimeMillis()
    val bot = bot {
        token = botToken
    }
    var state = getConnectionState()
    bot.sendMessage(channelId, buildWelcomeMessage(state))
    while (true) {
        state = checkStateAndSendMessage(state, bot)
        Thread.sleep(60000)
    }
}

private fun checkStateAndSendMessage(previousState: Boolean, bot: Bot): Boolean {
    val newState = getConnectionState()
    if (previousState != newState) {
        bot.sendMessage(channelId, buildOutputMessage(newState))
    }
    return newState
}

private fun buildOutputMessage(state: Boolean): String {
    val currentTime = System.currentTimeMillis()
    val s: String = if (state) {
        buildString {
            append(ENABLED_TEXT)
            append(TIME_ENABLED_TEXT)
            append(formatOutputDate(currentTime))
            append(TIME_ENABLED_PERIOD_TEXT)
            append(formatOutputDiff(currentTime, bufferTime))
        }
    } else {
        buildString {
            append(DISABLED_TEXT)
            append(TIME_DISABLED_TEXT)
            append(formatOutputDate(currentTime))
            append(TIME_DISABLED_PERIOD_TEXT)
            append(formatOutputDiff(currentTime, bufferTime))
        }
    }
    bufferTime = currentTime
    return s
}

private fun buildWelcomeMessage(state: Boolean): String {
    val s: String = if (state) {
        buildString {
            append(WELCOME_TEXT)
            append(ENABLED_TEXT)
        }
    } else {
        buildString {
            append(WELCOME_TEXT)
            append(DISABLED_TEXT)
        }
    }
    return s
}

private fun formatOutputDiff(updated: Long, previous: Long): String {
    val milliseconds = updated - previous
    val minutes = milliseconds / (1000 * 60) % 60
    val hours = milliseconds / (1000 * 60 * 60)
    return buildString {
        if (hours > 0) {
            append(hours)
            append(" годин і ")
        }
        append(minutes)
        append(" хвилин")
    }
}

private fun formatOutputDate(milliseconds: Long): String? {
    val sdf = SimpleDateFormat("HH:mm dd/MM/yy")
    val resultDate = Date(milliseconds)
    return sdf.format(resultDate)
}

private fun getConnectionState(): Boolean {
    var retry = 0
    var state = false
    while (retry < 5) {
        state = getStatus()
        if (state) break
        retry++
        Thread.sleep(5000)
    }
    return state
}

private fun getStatus(): Boolean {
    var s = ""
    val p: Process = Runtime.getRuntime().exec(command)
    val inputStream = BufferedReader(InputStreamReader(p.inputStream))
    for (i in 0..1) {
        s = inputStream.readLine()
    }
    p.destroy()
    return s.contains("time")
}

