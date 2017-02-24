package kubed.demo


import javafx.application.Application
import javafx.geometry.Insets
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.paint.Color
import javafx.stage.Stage
import kubed.axis.axisBottom
import kubed.axis.axisLeft
import kubed.interpolate.interpolateRound
import kubed.scale.LinearScale
import kubed.scale.OrdinalScale
import kubed.scale.scaleBand
import kubed.selection.selectAll
import kubed.shape.*

class StackedBarChartDemo: Application() {
    override fun start(primaryStage: Stage?) {
        val margin = Insets(20.0, 20.0, 30.0, 40.0)

        val outerWidth = 960.0
        val outerHeight = 500.0
        val innerWidth = outerWidth - margin.left - margin.right
        val innerHeight = outerHeight - margin.top - margin.bottom

        val root = Group()
        root.prefWidth(outerWidth)
        root.prefHeight(outerHeight)
        root.translateX = margin.left + 30.0
        root.translateY = margin.top

        val data = parseData().sortedBy { 0 - (it["Total"]?.toInt() as Int) }

        val xScale = scaleBand<String> {
            rangeRound(listOf(0.0, innerWidth))
            domain(data.map { it["State"] }.distinct() as List<String>)
            paddingInner = 0.05
            align = 0.1
        }

        val yScale = LinearScale<Double>(::interpolateRound).range(listOf(innerHeight, 0.0))
                .domain(listOf(0.0, (data.maxBy { it["Total"]?.toInt() ?: 0 })?.get("Total")?.toDouble() ?: throw IllegalStateException()))
        (yScale as LinearScale<*>).nice()

        val keys = listOf("Under 5 Years", "5 to 13 Years", "14 to 17 Years", "18 to 24 Years", "25 to 44 Years",
                "45 to 64 Years", "65 Years and Over")
        val zScale = OrdinalScale<String, Color>()
                .range(listOf(Color.web("#98abc5"), Color.web("#8a89a6"), Color.web("#7b6888"),
                        Color.web("#6b486b"), Color.web("#a05d56"), Color.web("#d0743c"), Color.web("#ff8c00")))
                .domain(keys)

        val bar = rect<Point<Map<String, String>, String>> {
            fill { d -> zScale(d.key) }
            translateX { d -> xScale(d.data["State"] as String) }
            translateY { d -> yScale(d.y1) }
            height { d -> yScale(d.y0) - yScale(d.y1) }
            width(xScale.bandwidth)
        }

        root.selectAll<Node>()
                .data(listOf(stack({ keys }, { d: Map<String, String>, k: String -> d[k]?.toDouble() as Double}, data)))
                .enter().append(fun (): Node { return Group() })
                .selectAll("rect")
                .data({ d, _, _ -> d as Series<*, *> })
                .enter()
                .append { d, _, _ -> bar(d as Point<Map<String, String>, String>) }

        val xAxis = axisBottom(xScale)
        xAxis(root.selectAll(".xAxis").append(fun(): Node { return Group() })
                .classed("axis", "xAxis")
                .translateY(innerHeight))

        val yAxis = axisLeft(yScale) {
            formatter { d -> (d / 1_000_000).toInt().toString() + "M" }
        }
        yAxis(root.selectAll(".yAxis").append(fun(): Node { return Group() })
                .classed("axis", "yAxis"))

        val scene = Scene(root)
        primaryStage?.scene = scene
        primaryStage?.show()
    }

    fun parseData(): List<Map<String, String>> {
        val list = ArrayList<Map<String, String>>()

        val lines = csv.reader().readLines()
        val cols = lines[0].split(",")
        for(i in 1 until lines.size) {
            val row = HashMap<String, String>()
            val line = lines[i]
            val values = line.split(",")

            var total = 0
            for(j in values.indices) {
                if(j > 0) total += values[j].toInt()
                row[cols[j]] = values[j]
            }
            row["Total"] = total.toString()
            list += row
        }

        return list
    }

    val csv = """State,Under 5 Years,5 to 13 Years,14 to 17 Years,18 to 24 Years,25 to 44 Years,45 to 64 Years,65 Years and Over
    AL,310504,552339,259034,450818,1231572,1215966,641667
    AK,52083,85640,42153,74257,198724,183159,50277
    AZ,515910,828669,362642,601943,1804762,1523681,862573
    AR,202070,343207,157204,264160,754420,727124,407205
    CA,2704659,4499890,2159981,3853788,10604510,8819342,4114496
    CO,358280,587154,261701,466194,1464939,1290094,511094
    CT,211637,403658,196918,325110,916955,968967,478007
    DE,59319,99496,47414,84464,230183,230528,121688
    DC,36352,50439,25225,75569,193557,140043,70648
    FL,1140516,1938695,925060,1607297,4782119,4746856,3187797
    GA,740521,1250460,557860,919876,2846985,2389018,981024
    HI,87207,134025,64011,124834,356237,331817,190067
    ID,121746,201192,89702,147606,406247,375173,182150
    IL,894368,1558919,725973,1311479,3596343,3239173,1575308
    IN,443089,780199,361393,605863,1724528,1647881,813839
    IA,201321,345409,165883,306398,750505,788485,444554
    KS,202529,342134,155822,293114,728166,713663,366706
    KY,284601,493536,229927,381394,1179637,1134283,565867
    LA,310716,542341,254916,471275,1162463,1128771,540314
    ME,71459,133656,69752,112682,331809,397911,199187
    MD,371787,651923,316873,543470,1556225,1513754,679565
    MA,383568,701752,341713,665879,1782449,1751508,871098
    MI,625526,1179503,585169,974480,2628322,2706100,1304322
    MN,358471,606802,289371,507289,1416063,1391878,650519
    MS,220813,371502,174405,305964,764203,730133,371598
    MO,399450,690476,331543,560463,1569626,1554812,805235
    MT,61114,106088,53156,95232,236297,278241,137312
    NE,132092,215265,99638,186657,457177,451756,240847
    NV,199175,325650,142976,212379,769913,653357,296717
    NH,75297,144235,73826,119114,345109,388250,169978
    NJ,557421,1011656,478505,769321,2379649,2335168,1150941
    NM,148323,241326,112801,203097,517154,501604,260051
    NY,1208495,2141490,1058031,1999120,5355235,5120254,2607672
    NC,652823,1097890,492964,883397,2575603,2380685,1139052
    ND,41896,67358,33794,82629,154913,166615,94276
    OH,743750,1340492,646135,1081734,3019147,3083815,1570837
    OK,266547,438926,200562,369916,957085,918688,490637
    OR,243483,424167,199925,338162,1044056,1036269,503998
    PA,737462,1345341,679201,1203944,3157759,3414001,1910571
    RI,60934,111408,56198,114502,277779,282321,147646
    SC,303024,517803,245400,438147,1193112,1186019,596295
    SD,58566,94438,45305,82869,196738,210178,116100
    TN,416334,725948,336312,550612,1719433,1646623,819626
    TX,2027307,3277946,1420518,2454721,7017731,5656528,2472223
    UT,268916,413034,167685,329585,772024,538978,246202
    VT,32635,62538,33757,61679,155419,188593,86649
    VA,522672,887525,413004,768475,2203286,2033550,940577
    WA,433119,750274,357782,610378,1850983,1762811,783877
    WV,105435,189649,91074,157989,470749,514505,285067
    WI,362277,640286,311849,553914,1487457,1522038,750146
    WY,38253,60890,29314,53980,137338,147279,65614"""

    companion object {
        @JvmStatic
        fun main(vararg args: String) {
            launch(StackedBarChartDemo::class.java, *args)
        }
    }
}