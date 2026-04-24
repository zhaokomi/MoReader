package com.mochen.reader.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import fi.iki.elonen.NanoHTTPD
import java.io.File
import java.io.FileOutputStream

class WifiTransferService : Service() {

    private var server: UploadServer? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val port = intent?.getIntExtra("port", 8080) ?: 8080

        server = UploadServer(port) { fileName ->
            // File uploaded callback
            sendBroadcast(Intent("com.mochen.reader.FILE_UPLOADED").apply {
                putExtra("fileName", fileName)
            })
        }
        server?.start()

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        server?.stop()
        super.onDestroy()
    }
}

class UploadServer(
    port: Int,
    private val onFileUploaded: (String) -> Unit
) : NanoHTTPD(port) {

    private val uploadDir: File by lazy {
        File(System.getProperty("java.io.tmpdir"), "uploads").also { it.mkdirs() }
    }

    override fun serve(session: IHTTPSession): Response {
        return when (session.method) {
            Method.GET -> serveHtml()
            Method.POST -> handleUpload(session)
            else -> newFixedLengthResponse("Method not allowed")
        }
    }

    private fun serveHtml(): Response {
        val html = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>墨阅 WiFi 传书</title>
                <style>
                    * { box-sizing: border-box; }
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                        max-width: 600px;
                        margin: 0 auto;
                        padding: 20px;
                        background: #f5f5f5;
                    }
                    .header {
                        text-align: center;
                        padding: 20px 0;
                    }
                    .header h1 {
                        color: #6750A4;
                        margin: 0;
                    }
                    .upload-area {
                        background: white;
                        border: 2px dashed #6750A4;
                        border-radius: 12px;
                        padding: 40px;
                        text-align: center;
                        margin: 20px 0;
                        transition: all 0.3s;
                    }
                    .upload-area:hover {
                        background: #f3f0ff;
                    }
                    .upload-area.dragover {
                        background: #e8def8;
                        border-color: #4f378b;
                    }
                    input[type="file"] {
                        display: none;
                    }
                    .upload-btn {
                        background: #6750A4;
                        color: white;
                        padding: 15px 30px;
                        border-radius: 8px;
                        cursor: pointer;
                        display: inline-block;
                        font-size: 16px;
                        transition: background 0.3s;
                    }
                    .upload-btn:hover {
                        background: #4f378b;
                    }
                    .formats {
                        margin-top: 20px;
                        color: #666;
                        font-size: 14px;
                    }
                    .success {
                        color: #4caf50;
                        margin-top: 15px;
                        font-weight: bold;
                    }
                    .file-list {
                        margin-top: 20px;
                    }
                    .file-item {
                        background: white;
                        padding: 15px;
                        border-radius: 8px;
                        margin-bottom: 10px;
                        display: flex;
                        justify-content: space-between;
                        align-items: center;
                    }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>📚 墨阅 WiFi 传书</h1>
                    <p>在手机上打开墨阅 App，扫描二维码连接</p>
                </div>

                <div class="upload-area" id="dropZone">
                    <form method="post" enctype="multipart/form-data" id="uploadForm">
                        <input type="file" name="file" id="fileInput" accept=".txt,.epub,.mobi,.azw3,.pdf">
                        <label for="fileInput" class="upload-btn">选择文件上传</label>
                        <p id="fileName" style="margin-top: 15px; color: #666;"></p>
                        <div id="successMsg" class="success" style="display: none;">上传成功！</div>
                    </form>
                    <div class="formats">
                        支持格式: TXT, EPUB, MOBI, AZW3, PDF
                    </div>
                </div>

                <script>
                    const dropZone = document.getElementById('dropZone');
                    const fileInput = document.getElementById('fileInput');
                    const fileName = document.getElementById('fileName');
                    const successMsg = document.getElementById('successMsg');

                    fileInput.addEventListener('change', function() {
                        if (this.files.length > 0) {
                            fileName.textContent = this.files[0].name;
                            uploadFile();
                        }
                    });

                    dropZone.addEventListener('dragover', function(e) {
                        e.preventDefault();
                        dropZone.classList.add('dragover');
                    });

                    dropZone.addEventListener('dragleave', function() {
                        dropZone.classList.remove('dragover');
                    });

                    dropZone.addEventListener('drop', function(e) {
                        e.preventDefault();
                        dropZone.classList.remove('dragover');
                        if (e.dataTransfer.files.length > 0) {
                            fileInput.files = e.dataTransfer.files;
                            fileName.textContent = e.dataTransfer.files[0].name;
                            uploadFile();
                        }
                    });

                    function uploadFile() {
                        const formData = new FormData(document.getElementById('uploadForm'));
                        fetch('/', {
                            method: 'POST',
                            body: formData
                        })
                        .then(response => {
                            if (response.ok) {
                                successMsg.style.display = 'block';
                                fileName.textContent = '';
                                fileInput.value = '';
                                setTimeout(() => {
                                    successMsg.style.display = 'none';
                                }, 3000);
                            }
                        })
                        .catch(err => {
                            console.error('Upload failed:', err);
                            alert('上传失败');
                        });
                    }
                </script>
            </body>
            </html>
        """.trimIndent()

        return newFixedLengthResponse(html).apply {
            mimeType = "text/html; charset=utf-8"
        }
    }

    private fun handleUpload(session: IHTTPSession): Response {
        val files = HashMap<String, String>()
        session.parseBody(files)

        val uploadedFile = files["file"]
        if (uploadedFile != null) {
            onFileUploaded(uploadedFile)
        }

        return newFixedLengthResponse("Upload successful")
    }
}
