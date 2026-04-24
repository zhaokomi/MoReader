package com.mochen.reader.presentation.settings

import android.content.Context
import android.net.wifi.WifiManager
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mochen.reader.R
import com.mochen.reader.data.datastore.SettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.Inet4Address
import java.net.NetworkInterface
import javax.inject.Inject

data class WifiTransferUiState(
    val isRunning: Boolean = false,
    val ipAddress: String = "",
    val port: Int = 8080,
    val uploadedFiles: List<String> = emptyList()
)

@HiltViewModel
class WifiTransferViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(WifiTransferUiState())
    val uiState: StateFlow<WifiTransferUiState> = _uiState.asStateFlow()

    private var server: SimpleHttpServer? = null

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            settingsDataStore.wifiTransferPort.collect { port ->
                _uiState.update { it.copy(port = port) }
            }
        }
        viewModelScope.launch {
            settingsDataStore.wifiTransferEnabled.collect { enabled ->
                if (enabled && !_uiState.value.isRunning) {
                    startServer()
                } else if (!enabled && _uiState.value.isRunning) {
                    stopServer()
                }
            }
        }
    }

    fun getLocalIpAddress(): String {
        return try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    if (!address.isLoopbackAddress && address is Inet4Address) {
                        return address.hostAddress ?: ""
                    }
                }
            }
            ""
        } catch (e: Exception) {
            ""
        }
    }

    fun startServer() {
        viewModelScope.launch {
            val ip = getLocalIpAddress()
            if (ip.isEmpty()) {
                return@launch
            }

            withContext(Dispatchers.IO) {
                server = SimpleHttpServer(_uiState.value.port) { fileName ->
                    _uiState.update {
                        it.copy(uploadedFiles = it.uploadedFiles + fileName)
                    }
                }
                server?.start()
            }

            _uiState.update {
                it.copy(isRunning = true, ipAddress = ip)
            }
        }
    }

    fun stopServer() {
        viewModelScope.launch(Dispatchers.IO) {
            server?.stop()
            server = null
        }
        _uiState.update { it.copy(isRunning = false) }
    }

    override fun onCleared() {
        super.onCleared()
        stopServer()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WifiTransferScreen(
    onBackClick: () -> Unit,
    viewModel: WifiTransferViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.wifi_transfer_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Wifi,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = if (uiState.isRunning)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.outline
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (uiState.isRunning) "服务运行中" else "服务已停止",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.wifi_transfer_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center
            )

            if (uiState.isRunning) {
                Spacer(modifier = Modifier.height(32.dp))

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.wifi_transfer_address),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "http://${uiState.ipAddress}:${uiState.port}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = {
                        // Copy to clipboard or open browser
                    }
                ) {
                    Icon(Icons.Default.OpenInBrowser, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.wifi_transfer_open_browser))
                }

                Spacer(modifier = Modifier.height(32.dp))

                if (uiState.uploadedFiles.isNotEmpty()) {
                    Text(
                        text = "最近上传:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    uiState.uploadedFiles.takeLast(5).forEach { fileName ->
                        Text(
                            text = fileName,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (uiState.isRunning) {
                        viewModel.stopServer()
                    } else {
                        viewModel.startServer()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = if (uiState.isRunning) {
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                } else {
                    ButtonDefaults.buttonColors()
                }
            ) {
                Icon(
                    if (uiState.isRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (uiState.isRunning)
                        stringResource(R.string.wifi_transfer_stop)
                    else
                        "启动服务"
                )
            }
        }
    }
}

// Simple HTTP server for file upload
class SimpleHttpServer(
    private val port: Int,
    private val onFileUploaded: (String) -> Unit
) {
    private var serverSocket: java.net.ServerSocket? = null
    private var isRunning = false

    fun start() {
        isRunning = true
        Thread {
            try {
                serverSocket = java.net.ServerSocket(port)
                while (isRunning) {
                    val client = serverSocket?.accept() ?: break
                    handleRequest(client)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    private fun handleRequest(client: java.net.Socket) {
        try {
            val reader = client.getInputStream().bufferedReader()
            val requestLine = reader.readLine() ?: return

            val response = """
                HTTP/1.1 200 OK
                Content-Type: text/html; charset=utf-8

                <!DOCTYPE html>
                <html>
                <head>
                    <meta name="viewport" content="width=device-width, initial-scale=1">
                    <title>墨阅 WiFi 传书</title>
                    <style>
                        body { font-family: -apple-system, sans-serif; max-width: 500px; margin: 50px auto; padding: 20px; }
                        h1 { color: #6750A4; }
                        .upload-area { border: 2px dashed #6750A4; padding: 40px; text-align: center; border-radius: 8px; margin: 20px 0; }
                        input[type=file] { display: none; }
                        label { background: #6750A4; color: white; padding: 10px 20px; border-radius: 4px; cursor: pointer; }
                        .success { color: green; margin-top: 10px; }
                    </style>
                </head>
                <body>
                    <h1>墨阅 WiFi 传书</h1>
                    <div class="upload-area">
                        <form method="POST" enctype="multipart/form-data">
                            <input type="file" name="file" id="file" accept=".txt,.epub,.mobi,.azw3,.pdf">
                            <label for="file">选择文件</label>
                            <p id="filename"></p>
                        </form>
                        <p style="color: #666; margin-top: 20px;">支持: TXT, EPUB, MOBI, AZW3, PDF</p>
                    </div>
                    <script>
                        document.getElementById('file').addEventListener('change', function() {
                            document.getElementById('filename').textContent = this.files[0]?.name || '';
                        });
                    </script>
                </body>
                </html>
            """.trimIndent()

            client.getOutputStream().write(response.toByteArray())
            client.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stop() {
        isRunning = false
        serverSocket?.close()
    }
}
