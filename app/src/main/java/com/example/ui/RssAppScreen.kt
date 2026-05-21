package com.example.ui

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RssAppScreen(viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    var hasOverlayPermission by remember { mutableStateOf(Settings.canDrawOverlays(context)) }
    var rssUrl by remember { mutableStateOf("") }
    
    val feeds by viewModel.feeds.collectAsStateWithLifecycle()
    val news by viewModel.news.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("RSS Balloon Reader", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            
            if (!hasOverlayPermission) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.width(8.dp))
                            Text("Permissão Necessária", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("O app precisa de permissão para desenhar sobre outros apps para mostrar os balões flutuantes.")
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = {
                            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
                            context.startActivity(intent)
                        }) {
                            Text("Conceder Permissão")
                        }
                        Button(onClick = { hasOverlayPermission = Settings.canDrawOverlays(context) }) {
                            Text("Atualizar")
                        }
                    }
                }
            }

            Text("Adicionar Novo RSS", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = rssUrl,
                    onValueChange = { rssUrl = it },
                    placeholder = { Text("Ex: https://g1.globo.com/rss/g1/tecnologia/") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (rssUrl.isNotBlank()) {
                            viewModel.addFeed(rssUrl)
                            rssUrl = ""
                            viewModel.syncNow()
                        }
                    },
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Adicionar Feeds")
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Sugestões Famosas (Brasil e Mundo)", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            val suggestedFeeds = remember {
                listOf(
                    Pair("G1 Tech (BR)", "https://g1.globo.com/rss/g1/tecnologia/"),
                    Pair("UOL (BR)", "http://rss.uol.com.br/feed/noticias.xml"),
                    Pair("Jovem Nerd (BR)", "https://jovemnerd.com.br/feed/"),
                    Pair("BBC World (EN)", "http://feeds.bbci.co.uk/news/world/rss.xml"),
                    Pair("NYT Tech (EN)", "https://rss.nytimes.com/services/xml/rss/nyt/Technology.xml"),
                    Pair("The Verge (EN)", "https://www.theverge.com/rss/index.xml")
                )
            }
            androidx.compose.foundation.lazy.LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(suggestedFeeds) { suggestion ->
                    AssistChip(
                        onClick = {
                            viewModel.addFeed(suggestion.second, suggestion.first)
                            viewModel.syncNow()
                        },
                        label = { Text(suggestion.first) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Add",
                                modifier = Modifier.size(AssistChipDefaults.IconSize)
                            )
                        }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Meus Feeds", style = MaterialTheme.typography.titleMedium)
                Row {
                    Button(onClick = { viewModel.syncNow() }) {
                        Text("Sincronizar")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { 
                        if (Settings.canDrawOverlays(context)) {
                            val latestNews = news.firstOrNull()
                            if (latestNews != null) {
                                com.example.service.BubbleManager.showBubble(context, latestNews.title, latestNews.summary ?: "Sem resumo.")
                            } else {
                                com.example.service.BubbleManager.showBubble(context, "Notícia de Teste", "Esta é uma demonstração de como as notícias aparecerão na tela sobre outros aplicativos usando este balão flutuante!")
                            }
                        } else {
                            hasOverlayPermission = false
                        }
                    }) {
                        Text("Testar")
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(feeds) { feed ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(feed.url, style = MaterialTheme.typography.bodyMedium)
                            }
                            IconButton(onClick = { viewModel.deleteFeed(feed.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Deletar", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }

                item {
                    Spacer(Modifier.height(16.dp))
                    Text("Histórico de Notícias", style = MaterialTheme.typography.titleMedium)
                }
                
                items(news) { item ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(item.title, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            Text(item.summary ?: "Sem resumo disponível.", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}
