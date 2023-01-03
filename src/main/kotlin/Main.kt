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

private var globalTimer: Long = 0L

const val WELCOME_TEXT: String = "\nВітаю! Цей бот створенний для перевірки наявності електроенергії "
const val LIGHT_ON_TEXT: String = "\nСвітло є "
const val LIGHT_OFF_TEXT: String = "\nСвітла нема "
const val TIME_ENABLED_TEXT: String = "\nЧас включення "
const val TIME_ENABLED_PERIOD_TEXT: String = "\nСвітла не було протягом\n"
const val TIME_DISABLED_TEXT: String = "\nЧас відключення "
const val TIME_DISABLED_PERIOD_TEXT: String = "\nСвітло було протягом\n"
const val LIGHT_ON_EMOJI = 0x1F389
const val LIGHT_OFF_EMOJI = 0x1F614

fun main() {
    globalTimer = System.currentTimeMillis()
    val bot = bot {
        token = botToken
    }
    var state = getConnectionState(ip)
    bot.sendMessage(channelId, sendWelcomeMessage(state))
    while (true) {
        state = checkStateUpdated(state, bot)
        Thread.sleep(1000)
    }
}


private fun checkStateUpdated(previousState: Boolean, bot: Bot): Boolean {
    val newState = getConnectionState(ip)
    val newTime: Long
    if (previousState != newState) {
        newTime = System.currentTimeMillis()
        bot.sendMessage(channelId, sendFormattedText(newState, newTime))
        globalTimer = newTime
    }
    return newState
}

private fun getConnectionState(ip: String): Boolean {
    var s = ""
    for (retry in 1..5) {
        val p: Process = Runtime.getRuntime().exec("ping $ip")
        val inputStream = BufferedReader(InputStreamReader(p.inputStream))
        for (i in 0..1) {
            s = inputStream.readLine()
        }
        p.destroy()
        Thread.sleep(100)
    }
    return !s.contains("timeout")
}

private fun emoji(unicode: Int): String {
    return buildString { append(String(Character.toChars(unicode))) }
}

private fun sendFormattedText(state: Boolean, newTime: Long): String {
    val s: String = if (state) {
        buildString {
            append(LIGHT_ON_TEXT)
            append(emoji(LIGHT_ON_EMOJI))
            append(TIME_ENABLED_TEXT)
            append(formatOutputDate(newTime))
            append(TIME_ENABLED_PERIOD_TEXT)
            append(formatOutputDiff(newTime,globalTimer))
        }
    } else {
        buildString {
            append(LIGHT_OFF_TEXT)
            append(emoji(LIGHT_OFF_EMOJI))
            append(TIME_DISABLED_TEXT)
            append(formatOutputDate(newTime))
            append(TIME_DISABLED_PERIOD_TEXT)
            append(formatOutputDiff(newTime,globalTimer))
        }
    }
    return(s)
}

private fun sendWelcomeMessage(state: Boolean): String {
    val s: String = if (state) {
        buildString {
            append(WELCOME_TEXT)
            append(LIGHT_ON_TEXT)
            append(emoji(LIGHT_ON_EMOJI))
        }
    } else {
        buildString {
            append(WELCOME_TEXT)
            append(LIGHT_OFF_TEXT)
            append(emoji(LIGHT_OFF_EMOJI))
        }
    }
    return s
}

private fun formatOutputDiff(updated: Long, previous: Long): String {
    val milliseconds = updated - previous
    val minutes = milliseconds / (1000 * 60) % 60
    val hours = milliseconds / (1000 * 60 * 60)
    return buildString {
        append(hours)
        append(" годин і ")
        append(minutes)
        append(" хвилин")
    }
}

private fun formatOutputDate(milliseconds: Long): String? {
    val sdf = SimpleDateFormat("HH:mm dd/MM/yy")
    val resultDate = Date(milliseconds)
    return sdf.format(resultDate)
}


