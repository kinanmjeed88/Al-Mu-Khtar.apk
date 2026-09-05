package com.mukhtari.app.ui.certificates

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.mukhtari.app.data.local.entity.ResidencyCertificateEntity
import org.koin.androidx.compose.koinViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResidencyCertificateScreen(
    personId: Long,
    personName: String,
    onNavigateBack: () -> Unit,
    viewModel: ResidencyCertificateViewModel = koinViewModel()
) {
    val certificates by viewModel.certificates.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(personId) {
        viewModel.loadCertificatesForPerson(personId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تأييدات سكن: $personName") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            Button(
                onClick = { viewModel.issueCertificate(personId) },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                enabled = !isLoading
            ) {
                Text(if (isLoading) "جاري الإصدار..." else "إصدار تأييد سكن جديد")
            }

            if (certificates.isEmpty() && !isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("لا توجد تأييدات سكن لهذا الفرد.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(certificates) { certificate ->
                        CertificateCard(
                            certificate = certificate,
                            onOpenPdf = {
                                certificate.pdfPath?.let { path ->
                                    val file = File(path)
                                    if (file.exists()) {
                                        val uri = FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.fileprovider",
                                            file
                                        )
                                        val intent = Intent(Intent.ACTION_VIEW).apply {
                                            setDataAndType(uri, "application/pdf")
                                            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                        }
                                        context.startActivity(Intent.createChooser(intent, "عرض الملف"))
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CertificateCard(certificate: ResidencyCertificateEntity, onOpenPdf: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
            val dateStr = sdf.format(Date(certificate.issuedAt))

            Text("تاريخ الإصدار: $dateStr", style = MaterialTheme.typography.titleMedium)
            Text("الاسم في التأييد: ${certificate.snapshotName}", style = MaterialTheme.typography.bodyMedium)
            Text("العائلة: ${certificate.snapshotFamily ?: "غير محدد"}", style = MaterialTheme.typography.bodyMedium)
            Text("العنوان: ${certificate.snapshotAddress ?: "غير محدد"}", style = MaterialTheme.typography.bodyMedium)

            if (certificate.pdfPath != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onOpenPdf) {
                    Text("عرض ملف PDF")
                }
            }
        }
    }
}
