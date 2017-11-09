package net.skaverat.haxlernode

import com.amazonaws.services.s3.model.GetObjectRequest
import com.amazonaws.services.sqs.model.ReceiveMessageRequest
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import net.skaverat.haxlernode.amazon.AmazonClient
import net.skaverat.haxlernode.util.Archiver
import org.apache.commons.io.FileUtils
import org.apache.commons.lang.StringUtils
import org.apache.log4j.LogManager
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.lang.String.format
import java.util.*

class Renderer {
    private val archiver = Archiver()
    private val jsonMapper = jacksonObjectMapper()

    @Throws(InterruptedException::class)
    fun fetchMessage() {

        val client = amazonClient.getAmazonSQSClient()
        val queueUrl = client.listQueues("haxler").getQueueUrls().get(0)


        val req = ReceiveMessageRequest(queueUrl)
        req.maxNumberOfMessages = 1
        val messages = client.receiveMessage(req).getMessages()

        if (messages.size > 0) {
            val message = messages.get(0)
            val body = message.getBody()
            logger.info("Received message. Rendering...")
            logger.info(body)
            val renderData = jsonMapper.readValue<RenderData>(body)

            client.deleteMessage(queueUrl, message.receiptHandle)

            logger.info("Generating render script...")
            generateRenderScript()
            logger.info("Fetching project file...")
//            getProjectFile(renderData)
            logger.info("Unpacking project file...")
//            decompressFile(renderData)
            logger.info("Start render")
            render(renderData)
            logger.info("Uploading finished file")
            uploadFrame(renderData)
        } else {
            logger.warn("No messages found. Waiting 5 seconds...")
            Thread.sleep(5000)
        }
    }

    private fun generateRenderScript() {
        val file = File("render.py")
        val resource = this.javaClass.getResource("/render.py")
        try {
            FileUtils.copyURLToFile(resource, file)
        } catch (e: IOException) {
            logger.error(e)
        }

    }

    private fun decompressFile(renderData: RenderData) {
        val file = File(format("%s.blend", renderData.projectName))
        if (!file.isFile) {
            Archiver.decompressFile(File(format("%s.tar.gz", renderData.projectName)))
        }
    }

    private fun getProjectFile(renderData: RenderData) {
        val file = File(format("%s.tar.gz", renderData.projectName))
        if (!file.isFile) {
            val amazonS3Client = amazonClient.getAmazonS3Client()
            val request = GetObjectRequest("haxler", format("projects/%s.tar.gz", renderData.projectName))
            amazonS3Client.getObject(request, file)
        }
    }

    private fun uploadFrame(renderData: RenderData) {
        val amazonS3Client = amazonClient.getAmazonS3Client()

        val renderedFile = File(renderData.projectName + "_" + StringUtils.leftPad(renderData.frame, 5, "0") + ".png")

        logger.info("Uploading frame...")
        amazonS3Client.putObject("haxler", renderData.projectName + "/" + renderedFile.name, renderedFile)
    }

    private fun render(renderData: RenderData) {
        var start: Process? = null
        try {
            val process = ProcessBuilder().command(getBlenderCommand(renderData.projectName))
            val environment = process.environment()
//            val environment = HashMap<String, String>()
            if (renderData.resolutionX.isNotEmpty()) {
                environment.put("HAXLER_RES_X", renderData.resolutionX)
            }
            if (renderData.resolutionY.isNotEmpty()) {
                environment.put("HAXLER_RES_Y", renderData.resolutionY)
            }
            if (renderData.samples.isNotEmpty()) {
                environment.put("HAXLER_SAMPLES", renderData.samples)
            }
            if (renderData.useParts) {
                environment.put("HAXLER_USE_BORDER", renderData.useParts.toString())
                environment.put("HAXLER_BORDER_PARTS_NUM", renderData.partsNum.toString())
                environment.put("HAXLER_BORDER_PARTS_MINX", renderData.partsMinX.toString())
                environment.put("HAXLER_BORDER_PARTS_MAXX", renderData.partsMaxX.toString())
                environment.put("HAXLER_BORDER_PARTS_MINY", renderData.partsMinY.toString())
                environment.put("HAXLER_BORDER_PARTS_MAXY", renderData.partsMaxY.toString())
            }
            if(renderData.use_stereo) {
                environment.put("HAXLER_STEREO", "True")
            }



            environment.put("HAXLER_PROJECTNAME", renderData.projectName)
            environment.put("HAXLER_FRAME", renderData.frame)



            start = process.start()
            logProcessOutput(start)
        } catch (e: IOException) {
            logger.error(e)
        }

    }

    @Throws(IOException::class)
    private fun logProcessOutput(start: Process) {
        val isr = InputStreamReader(start.inputStream)
        val errorStream = InputStreamReader(start.errorStream)
        val stdOutBuffer = BufferedReader(isr)
        val stdErrorBuffer = BufferedReader(errorStream)
        var line = ""
        var errLine = ""

        while ({line = stdOutBuffer.readLine()?:""; line }().isNotEmpty()) {
            logger.info(line)
        }
        while ({errLine = stdErrorBuffer.readLine()?:""; errLine }().isNotEmpty()) {
            logger.error(errLine)
        }
    }



    private fun getBlenderCommand(projectName: String): List<String> {
        val c = ArrayList<String>()
        c.add("blender")
        c.add("-b")
        c.add(format("%s.blend", projectName))
        c.add("-P")
        c.add("render.py")
        return c
    }
        private val logger = LogManager.getLogger(Renderer::class.java)
        private val amazonClient = AmazonClient()

}
