package com.mochen.reader.parser

import android.content.Context
import android.net.Uri
import com.mochen.reader.domain.model.Chapter
import org.jsoup.Jsoup
import org.w3c.dom.Element
import java.io.InputStream
import java.util.zip.ZipInputStream
import javax.inject.Inject
import javax.inject.Singleton
import javax.xml.parsers.DocumentBuilderFactory

@Singleton
class EpubParser @Inject constructor() {

    private var cachedOpfPath: String? = null
    private var cachedSpineItems: List<String> = emptyList()
    private var cachedTocTitles: List<String> = emptyList()
    private var cachedCoverHref: String? = null

    suspend fun parseChapters(context: Context, uri: Uri, bookId: Long): List<Chapter> {
        val chapters = mutableListOf<Chapter>()

        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return chapters
            val zipStream = ZipInputStream(inputStream)

            // First pass: parse container.xml to find OPF path
            var opfPath: String? = null
            var opfContent: String? = null
            var tocContent: String? = null
            var ncxContent: String? = null

            zipStream.use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    val name = entry.name.lowercase()

                    when {
                        name == "meta-inf/container.xml" -> {
                            opfPath = parseContainerXml(zip)
                        }
                        name.endsWith(".opf") && opfContent == null -> {
                            opfPath = name
                            opfContent = zip.bufferedReader().readText()
                        }
                        name.endsWith(".ncx") && ncxContent == null -> {
                            ncxContent = zip.bufferedReader().readText()
                        }
                    }
                    entry = zip.nextEntry
                }
            }

            if (opfContent == null || opfPath == null) {
                return chapters
            }

            // Parse OPF to get spine items and cover
            val manifest = parseManifest(opfContent)
            cachedSpineItems = parseSpine(opfContent)
            cachedCoverHref = manifest["cover"]?.let { href ->
                manifest.entries.find { it.value == href }?.key
            }

            // Parse NCX for table of contents titles
            cachedTocTitles = if (ncxContent != null) {
                parseNcxToc(ncxContent)
            } else {
                parseOpfToc(opfContent, manifest)
            }

            // Create chapters from spine items
            cachedOpfPath = opfPath

            var chapterIndex = 0
            cachedSpineItems.forEachIndexed { index, _ ->
                chapters.add(
                    Chapter(
                        bookId = bookId,
                        chapterIndex = chapterIndex,
                        title = cachedTocTitles.getOrElse(index) { "第${index + 1}章" },
                        startPosition = index.toLong(),
                        endPosition = index.toLong(),
                        wordCount = 0
                    )
                )
                chapterIndex++
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return chapters
    }

    private fun parseContainerXml(inputStream: InputStream): String? {
        return try {
            val factory = DocumentBuilderFactory.newInstance()
            factory.isNamespaceAware = true
            val builder = factory.newDocumentBuilder()
            val doc = builder.parse(inputStream)

            val rootfiles = doc.getElementsByTagName("rootfile")
            if (rootfiles.length > 0) {
                (rootfiles.item(0) as? Element)?.getAttribute("full-path")
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun parseManifest(opfContent: String): Map<String, String> {
        val manifest = mutableMapOf<String, String>()
        try {
            val factory = DocumentBuilderFactory.newInstance()
            factory.isNamespaceAware = true
            val builder = factory.newDocumentBuilder()
            val doc = builder.parse(opfContent.byteInputStream())

            val items = doc.getElementsByTagName("item")
            for (i in 0 until items.length) {
                val item = items.item(i) as? Element
                item?.let {
                    val id = it.getAttribute("id")
                    val href = it.getAttribute("href")
                    val mediaType = it.getAttribute("media-type")
                    if (id.isNotEmpty() && href.isNotEmpty()) {
                        manifest[id] = href
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return manifest
    }

    private fun parseSpine(opfContent: String): List<String> {
        val spineItems = mutableListOf<String>()
        try {
            val factory = DocumentBuilderFactory.newInstance()
            factory.isNamespaceAware = true
            val builder = factory.newDocumentBuilder()
            val doc = builder.parse(opfContent.byteInputStream())

            // Get manifest for idref to href mapping
            val manifest = parseManifest(opfContent)

            val spineRefs = doc.getElementsByTagName("itemref")
            for (i in 0 until spineRefs.length) {
                val itemref = spineRefs.item(i) as? Element
                itemref?.let {
                    val idref = it.getAttribute("idref")
                    manifest[idref]?.let { href ->
                        spineItems.add(href)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return spineItems
    }

    private fun parseNcxToc(ncxContent: String): List<String> {
        val titles = mutableListOf<String>()
        try {
            val factory = DocumentBuilderFactory.newInstance()
            factory.isNamespaceAware = true
            val builder = factory.newDocumentBuilder()
            val doc = builder.parse(ncxContent.byteInputStream())

            val navPoints = doc.getElementsByTagName("navPoint")
            for (i in 0 until navPoints.length) {
                val navPoint = navPoints.item(i) as? Element
                val textElements = navPoint?.getElementsByTagName("text")
                if (textElements != null && textElements.length > 0) {
                    titles.add(textElements.item(0).textContent ?: "第${i + 1}章")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return titles
    }

    private fun parseOpfToc(opfContent: String, manifest: Map<String, String>): List<String> {
        // Fallback: use manifest IDs as titles
        return manifest.keys.toList()
    }

    suspend fun readChapterContent(context: Context, uri: Uri, chapter: Chapter): String {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return ""
            val zipStream = ZipInputStream(inputStream)

            var content = ""
            val opfDir = cachedOpfPath?.substringBeforeLast("/", "") ?: ""

            zipStream.use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    val entryName = entry.name
                    val spineItem = cachedSpineItems.getOrNull(chapter.chapterIndex.toInt())
                    val expectedPath = if (opfDir.isNotEmpty()) {
                        "$opfDir/$spineItem"
                    } else {
                        spineItem
                    }

                    if (entryName == expectedPath || entryName == spineItem) {
                        content = zip.bufferedReader().readText()

                        // Parse HTML and extract text
                        val doc = Jsoup.parse(content)
                        doc.select("script, style").remove()
                        content = doc.body().text()
                        break
                    }
                    entry = zip.nextEntry
                }
            }
            content
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    fun extractCover(context: Context, uri: Uri): Uri? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val zipStream = ZipInputStream(inputStream)

            var coverUri: Uri? = null
            val opfDir = cachedOpfPath?.substringBeforeLast("/", "") ?: ""

            zipStream.use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    val entryName = entry.name.lowercase()
                    val coverHref = cachedCoverHref
                    val expectedPath = if (opfDir.isNotEmpty() && coverHref != null) {
                        "$opfDir/$coverHref"
                    } else {
                        coverHref
                    }

                    if (expectedPath != null && (entryName == expectedPath.lowercase() || entryName.endsWith(".jpg") || entryName.endsWith(".png"))) {
                        // Save cover to cache
                        val coverFile = java.io.File(context.cacheDir, "cover_${uri.hashCode()}.jpg")
                        coverFile.outputStream().use { output ->
                            zip.copyTo(output)
                        }
                        coverUri = Uri.fromFile(coverFile)
                        break
                    } else if ((entryName.contains("cover") && (entryName.endsWith(".jpg") || entryName.endsWith(".png") || entryName.endsWith(".gif")))) {
                        val coverFile = java.io.File(context.cacheDir, "cover_${uri.hashCode()}.jpg")
                        coverFile.outputStream().use { output ->
                            zip.copyTo(output)
                        }
                        coverUri = Uri.fromFile(coverFile)
                        break
                    }
                    entry = zip.nextEntry
                }
            }
            coverUri
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getBookInfo(context: Context, uri: Uri): Pair<String, String>? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val zipStream = ZipInputStream(inputStream)

            var title: String? = null
            var author: String? = null

            zipStream.use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    val name = entry.name
                    if (name.endsWith(".opf")) {
                        val content = zip.bufferedReader().readText()
                        val factory = DocumentBuilderFactory.newInstance()
                        factory.isNamespaceAware = true
                        val builder = factory.newDocumentBuilder()
                        val doc = builder.parse(content.byteInputStream())

                        // Get title
                        val titles = doc.getElementsByTagName("dc:title")
                        if (titles.length > 0) {
                            title = titles.item(0).textContent
                        }

                        // Get author/creator
                        val creators = doc.getElementsByTagName("dc:creator")
                        if (creators.length > 0) {
                            author = creators.item(0).textContent
                        }
                        break
                    }
                    entry = zip.nextEntry
                }
            }

            if (title != null || author != null) {
                Pair(title ?: "Unknown", author ?: "Unknown")
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
