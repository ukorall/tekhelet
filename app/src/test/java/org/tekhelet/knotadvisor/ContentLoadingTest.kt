package org.tekhelet.knotadvisor

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.tekhelet.knotadvisor.logic.GadilBuilder
import org.tekhelet.knotadvisor.logic.ScoringEngine
import org.tekhelet.knotadvisor.logic.ThreadLength
import org.tekhelet.knotadvisor.logic.TyingInstructions
import org.tekhelet.knotadvisor.model.*

/**
 * בדיקות שקוראות את קבצי ה-assets **האמיתיים** ומריצות עליהם את כל הלוגיקה.
 *
 * למה זה קיים: האפליקציה קרסה בכל מסך שנגע בשיטות, כי שתי שיטות נשארו עם
 * שמות KnotScheme ישנים אחרי שינוי שמות ב-enum. הקומפילציה עברה - JSON הוא
 * טקסט, הקומפיילר לא מסתכל עליו - והתקלה התגלתה רק על המכשיר.
 *
 * הבדיקות כאן היו תופסות את זה מיד, כי הן מפענחות את הקבצים עם אותם
 * serializers שהאפליקציה משתמשת בהם בפועל.
 */
class ContentLoadingTest {

    @Serializable private data class MethodsFile(val methods: List<KnotMethod>)
    @Serializable private data class QuestionsFile(val questions: List<Question>)

    private val json = Json { ignoreUnknownKeys = true }

    private fun readAsset(name: String): String =
        javaClass.classLoader!!.getResourceAsStream(name)?.bufferedReader()?.readText()
            ?: error("לא נמצא קובץ ה-asset: $name")

    private val methods: List<KnotMethod> by lazy {
        json.decodeFromString(MethodsFile.serializer(), readAsset("methods.json")).methods
    }

    private val questions: List<Question> by lazy {
        json.decodeFromString(QuestionsFile.serializer(), readAsset("questions.json")).questions
    }

    @Test
    fun `methods json decodes with the real serializers`() {
        assertTrue("אמורות להיות שיטות", methods.isNotEmpty())
    }

    @Test
    fun `questions json decodes with the real serializers`() {
        assertTrue("אמורות להיות שאלות", questions.isNotEmpty())
    }

    @Test
    fun `every method has a unique id`() {
        assertEquals(methods.size, methods.map { it.id }.toSet().size)
    }

    @Test
    fun `every method renders a gadil without throwing`() {
        methods.forEach { m ->
            val plan = GadilBuilder.plan(m.composition)
            assertTrue("${m.id}: הגדיל יצא ריק", plan.elements.isNotEmpty())
            assertTrue("${m.id}: אין כריכות בכלל", plan.totalWinds > 0)
        }
    }

    /**
     * המקרה שאוריאל תיאר במפורש, וזה הבוחן החשוב ביותר של המודל: בשיטה
     * החסידית הקשרים נופלים **בתוך** חוליות ולא ביניהן, ולכן חוליה מתפצלת
     * למקטע של 1 ומקטע של 2. אם הבדיקה הזו נופלת - המודל חזר להיות שגוי.
     */
    @Test
    fun `chassidic method matches the exact spec`() {
        val plan = GadilBuilder.plan(methods.first { it.id == "chassidic-arizal" }.composition)
        val actual = plan.elements.mapNotNull {
            when (it) {
                is GadilBuilder.Element.Winds ->
                    (if (it.piece.tekhelet) "תכלת" else "לבן") + it.piece.length
                GadilBuilder.Element.Knot -> "קשר"
                else -> null
            }
        }
        val expected = listOf(
            "קשר", "לבן1", "תכלת2", "תכלת3", "תכלת1",
            "קשר", "תכלת2", "תכלת3", "תכלת3",
            "קשר", "תכלת3", "תכלת3", "תכלת3", "תכלת2",
            "קשר", "תכלת1", "תכלת3", "תכלת3", "תכלת3", "תכלת2", "לבן1",
            "קשר"
        )
        assertEquals("השיטה החסידית לא תואמת את המפרט", expected, actual)
        assertEquals("סה\"כ כריכות", 39, plan.totalWinds)
        assertEquals("מספר קשרים", 5, plan.doubleKnots)
    }

    /** הרב נגן: שבע חוליות תכלת מול שש לבן, כפי שהוא כותב במאמרו. */
    @Test
    fun `rav nagen has seven tekhelet chulyot and six white`() {
        val c = methods.first { it.id == "rav-nagen" }.composition
        val plan = GadilBuilder.plan(c)
        val byChulya = plan.elements.filterIsInstance<GadilBuilder.Element.Winds>()
            .groupBy { it.piece.chulyaIndex }
        val tekheletChulyot = byChulya.count { (_, pieces) -> pieces.any { it.piece.tekhelet } }
        val whiteChulyot = byChulya.count { (_, pieces) -> pieces.none { it.piece.tekhelet } }
        assertEquals("חוליות תכלת", 7, tekheletChulyot)
        assertEquals("חוליות לבן", 6, whiteChulyot)
    }

    /**
     * הכלל היחיד שנאכף בכל הרכב, בלי יוצא מן הכלל: הכריכה הראשונה והאחרונה
     * בלבן. זו גמרא מפורשת (מנחות ל"ט ע"א), וזה בדיוק המקום שבו אנשים טועים.
     */
    @Test
    fun `first and last winding are white in every method`() {
        methods.forEach { m ->
            val pieces = GadilBuilder.plan(m.composition).elements
                .filterIsInstance<GadilBuilder.Element.Winds>()
            if (pieces.isEmpty()) return@forEach
            assertTrue("${m.id}: הכריכה הראשונה אינה בלבן", !pieces.first().piece.tekhelet)
            assertTrue("${m.id}: הכריכה האחרונה אינה בלבן", !pieces.last().piece.tekhelet)
        }
    }

    /** ההוראות חייבות להתקפל, אחרת 13 חוליות מייצרות עשרים שורות זהות. */
    @Test
    fun `repeated instruction steps collapse`() {
        val raw = listOf("א", "ב", "ג", "ב", "ג", "ב", "ג", "ד")
        val collapsed = TyingInstructions.collapse(raw)
        assertTrue(
            "לא התקפל: $collapsed",
            collapsed.size < raw.size && collapsed.any { it.contains("חוזרים") }
        )
    }

    @Test
    fun `instructions stay a reasonable length for every method`() {
        methods.forEach { m ->
            val steps = TyingInstructions.generate(m.composition)
            assertTrue(
                "${m.id}: ${steps.size} שלבים - ארוך מדי, הקיפול לא עבד.\n" +
                    steps.joinToString("\n") { "  ${it.number}. ${it.text}" },
                steps.size <= 40
            )
        }
    }

    /**
     * הבדיקה החדה יותר: בראב"ד הכריכות מתחלפות בכל כריכה, ולכן הציור מפרק
     * אותן אחת-אחת. ההוראות חייבות לאחד אותן חזרה לחוליה אחת - אחרת יוצאות
     * ארבעים שורות של "כריכה בלבן, כריכה בתכלת". זה היה באג אמיתי.
     */
    @Test
    fun `alternating winds are described per chulya not per wind`() {
        val raavad = methods.first { it.id == "raavad" }
        val steps = TyingInstructions.generate(raavad.composition)
        assertTrue(
            "ראב\"ד: ${steps.size} שלבים - הכריכות לא אוחדו לחוליות.\n" +
                steps.joinToString("\n") { "  ${it.number}. ${it.text}" },
            steps.size <= 16
        )
        assertTrue(
            "ראב\"ד: אין שורה שמתארת חוליה שלמה לסירוגין",
            steps.any { it.text.contains("לסירוגין") }
        )
    }

    @Test
    fun `every method produces tying instructions`() {
        methods.forEach { m ->
            val steps = TyingInstructions.generate(m.composition)
            assertTrue("${m.id}: לא נוצרו הוראות", steps.size > 2)
        }
    }

    @Test
    fun `every method produces a thread length estimate`() {
        methods.forEach { m ->
            val e = ThreadLength.estimate(m.composition)
            assertTrue("${m.id}: אורך חוט לא חיובי", e.tekheletNeededCm > 0)
        }
    }

    @Test
    fun `scoring engine ranks every method without throwing`() {
        val answers = questions
            .filter { it.type == QuestionType.SLIDER }
            .map { Answer(questionId = it.id, sliderValue = 7) }
        val ranked = ScoringEngine.score(methods, questions, answers)
        assertEquals(methods.size, ranked.size)
        ranked.forEach {
            assertTrue("${it.method.id}: ציון מחוץ לטווח - ${it.score}", it.score in 0.0..100.0)
        }
    }

    /**
     * הנירמול הוא הלב של המנוע: שני אנשים שמתכוונים לאותה עדיפות יחסית אבל
     * משתמשים בסולם אחרת צריכים לקבל את אותו דירוג.
     */
    @Test
    fun `normalisation makes equivalent answers rank the same`() {
        val sliders = questions.filter { it.type == QuestionType.SLIDER }
        val loud = sliders.mapIndexed { i, q ->
            Answer(questionId = q.id, sliderValue = if (i % 2 == 0) 10 else 8)
        }
        val quiet = sliders.mapIndexed { i, q ->
            Answer(questionId = q.id, sliderValue = if (i % 2 == 0) 5 else 4)
        }
        val loudRanking = ScoringEngine.score(methods, questions, loud).map { it.method.id }
        val quietRanking = ScoringEngine.score(methods, questions, quiet).map { it.method.id }
        assertEquals(
            "אותה עדיפות יחסית בסולם אחר נתנה דירוג שונה - הנירמול לא עשה את שלו.\n" +
                "סולם גבוה (10/8): $loudRanking\nסולם נמוך (5/4): $quietRanking",
            loudRanking,
            quietRanking
        )
    }

    @Test
    fun `look preference lifts a matching method`() {
        val answers = questions
            .filter { it.type == QuestionType.SLIDER }
            .map { Answer(questionId = it.id, sliderValue = 7) }

        val neutral = ScoringEngine.evaluate(methods, questions, answers).ranked
        val preferring = ScoringEngine.evaluate(
            methods, questions, answers,
            lookPreference = LookPreference.ALTERNATING_CHULYOT
        ).ranked

        val target = methods.first { it.composition.windingColor == WindingColor.ALTERNATING_CHULYOT }
        val before = neutral.first { it.method.id == target.id }.score
        val after = preferring.first { it.method.id == target.id }.score
        assertTrue("ההעדפה החזותית לא העלתה את הציון", after > before)
    }

    @Test
    fun `questions reference axes that exist`() {
        questions.forEach { q ->
            if (q.type == QuestionType.SLIDER && q.id != "q_personal_clarity") {
                assertNotNull("${q.id}: שאלת סליידר בלי ציר", q.axis)
            }
        }
    }
}
