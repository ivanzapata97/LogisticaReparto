package com.example.logisticareparto.features.route.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.logisticareparto.BuildConfig
import com.example.logisticareparto.notifications.RouteNotificationHelper
import com.example.logisticareparto.R
import com.example.logisticareparto.data.models.Client
import com.example.logisticareparto.data.models.RouteStop
import com.example.logisticareparto.features.clients.ui.ClientItem
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import com.example.logisticareparto.features.clients.viewmodel.ClientsUiState
import com.example.logisticareparto.features.clients.viewmodel.ActiveRouteUiState
import com.example.logisticareparto.features.clients.viewmodel.ClientsViewModel
import com.example.logisticareparto.features.clients.viewmodel.RouteSaveUiState
import com.example.logisticareparto.features.clients.viewmodel.RouteUiState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState

private data class MapPresentation(
    val title: String,
    val subtitle: String,
    val clients: List<Client>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteScreen(viewModel: ClientsViewModel, onClientClick: (String) -> Unit) {
    val context = LocalContext.current
    val primaryColor = MaterialTheme.colorScheme.primary
    val routeState = viewModel.routeUiState
    val routeSaveState = viewModel.routeSaveUiState
    val routeDraft = viewModel.routeDraft
    val activeRouteState = viewModel.activeRouteUiState
    var mapPresentation by remember { mutableStateOf<MapPresentation?>(null) }

    if (mapPresentation != null) {
        val currentMap = mapPresentation!!
        RouteMapScreen(
            title = currentMap.title,
            subtitle = currentMap.subtitle,
            clients = currentMap.clients,
            onBack = { mapPresentation = null }
        )
        return
    }

    fun uriToBitmap(uri: Uri): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }
        } catch (_: Exception) {
            null
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let { viewModel.processRouteImage(it) }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            uriToBitmap(it)?.let { bitmap ->
                viewModel.processRouteImage(bitmap)
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch()
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !RouteNotificationHelper.canPostNotifications(context)
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    if (routeSaveState is RouteSaveUiState.Success) {
        AlertDialog(
            onDismissRequest = viewModel::clearRouteSaveState,
            title = { Text(stringResource(R.string.msg_route_started_title)) },
            text = { Text(stringResource(R.string.msg_route_started_text)) },
            confirmButton = {
                TextButton(onClick = viewModel::clearRouteSaveState) {
                    Text(stringResource(R.string.btn_accept))
                }
            }
        )
    }

    if (routeSaveState is RouteSaveUiState.Error) {
        AlertDialog(
            onDismissRequest = viewModel::clearRouteSaveState,
            title = { Text(stringResource(R.string.msg_route_error_title)) },
            text = { Text(routeSaveState.message) },
            confirmButton = {
                TextButton(onClick = viewModel::clearRouteSaveState) {
                    Text(stringResource(R.string.btn_accept))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        when {
                            activeRouteState is ActiveRouteUiState.Success -> stringResource(R.string.title_active_route)
                            else -> stringResource(R.string.title_route_setup)
                        },
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                actions = {
                    if (routeDraft.isNotEmpty() && activeRouteState !is ActiveRouteUiState.Success) {
                        IconButton(onClick = viewModel::resetRouteState) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.text_reset_draft),
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = primaryColor)
            )
        },
        bottomBar = {
            when {
                activeRouteState is ActiveRouteUiState.Success -> {
                    RouteBottomBar(
                        label = stringResource(R.string.btn_finish_route),
                        containerColor = Color(0xFF212121),
                        onClick = viewModel::finishActiveRoute
                    )
                }

                routeDraft.isNotEmpty() -> {
                    RouteBottomBar(
                        label = stringResource(R.string.btn_start_route),
                        containerColor = primaryColor,
                        contentColor = Color.White,
                        textSize = 18.sp,
                        trailingIcon = Icons.Default.Map,
                        onTrailingClick = {
                            val clientsWithCoordinates = routeDraft.filter(::hasValidCoordinates)
                            if (clientsWithCoordinates.isNotEmpty()) {
                                mapPresentation = MapPresentation(
                                    title = context.getString(R.string.label_map_stops),
                                    subtitle = context.getString(R.string.label_map_points, clientsWithCoordinates.size),
                                    clients = clientsWithCoordinates
                                )
                            }
                        },
                        onClick = {
                            viewModel.startCurrentRoute()
                        }
                    )
                }
            }
        }
    ) { padding ->
        when {
            routeState is RouteUiState.Processing -> {
                LoadingRouteState(modifier = Modifier.padding(padding), redColor = primaryColor)
            }

            activeRouteState is ActiveRouteUiState.Loading -> {
                LoadingRouteState(modifier = Modifier.padding(padding), redColor = primaryColor)
            }

            activeRouteState is ActiveRouteUiState.Success -> {
                ActiveRouteContent(
                    modifier = Modifier.padding(padding),
                    stops = viewModel.getActiveRouteStops(),
                    selectedTruck = viewModel.selectedTruck,
                    redColor = primaryColor,
                    onClientClick = onClientClick,
                    stopToClient = viewModel::getRouteStopClient,
                    onOpenMap = { clients ->
                        mapPresentation = MapPresentation(
                            title = context.getString(R.string.label_map_active),
                            subtitle = context.getString(R.string.label_map_points, clients.size),
                            clients = clients
                        )
                    },
                    onMarkVisited = { stop ->
                        viewModel.markStopVisited(stop) { nextStop, truckId ->
                            RouteNotificationHelper.sendNotification(
                                context = context,
                                notificationId = 1002 + nextStop.order,
                                title = context.getString(R.string.notification_next_stop_title),
                                message = context.getString(R.string.notification_next_stop_msg, truckId)
                            )
                        }
                    }
                )
                
                // Efecto para monitorear cierres proximos
                LaunchedEffect(activeRouteState) {
                    if (activeRouteState is ActiveRouteUiState.Success) {
                        val route = activeRouteState.route
                        val allClients = (viewModel.uiState as? ClientsUiState.Success)?.clients ?: emptyList()
                        
                        // Solo revisamos si hay paradas pendientes
                        val pendingStops = route.stops.filter { !it.isVisited }
                        if (pendingStops.isNotEmpty()) {
                            val now = LocalTime.now()
                            val formatter = DateTimeFormatter.ofPattern("HH:mm")
                            
                            pendingStops.forEach { stop ->
                                val client = allClients.find { it.id == stop.clientId }
                                if (client != null && !client.es24 && client.cierre.isNotEmpty()) {
                                    try {
                                        val closeTime = LocalTime.parse(client.cierre, formatter)
                                        val diff = java.time.Duration.between(now, closeTime).toMinutes()
                                        
                                        // Si falta media hora o menos para cerrar
                                        if (diff in 1..30) {
                                            RouteNotificationHelper.sendNotification(
                                                context = context,
                                                notificationId = 2000 + stop.order,
                                                title = context.getString(R.string.notification_closing_title),
                                                message = context.getString(R.string.notification_closing_msg, client.cliente, client.cierre)
                                            )
                                        }
                                    } catch (_: Exception) {}
                                }
                            }
                        }
                    }
                }
            }

            routeDraft.isNotEmpty() -> {
                UnifiedRouteDraftContent(
                    modifier = Modifier.padding(padding),
                    routeDraft = routeDraft,
                    redColor = primaryColor,
                    onClientClick = onClientClick,
                    onMoveUp = viewModel::moveClientUp,
                    onMoveDown = viewModel::moveClientDown,
                    onMoveToFirst = viewModel::moveClientToFirst,
                    onMoveToLast = viewModel::moveClientToLast,
                    onRemove = viewModel::removeClientFromRoute,
                    onReset = viewModel::resetRouteState
                )
            }

            activeRouteState is ActiveRouteUiState.Error -> {
                ErrorRouteState(
                    modifier = Modifier.padding(padding),
                    message = activeRouteState.message,
                    onRetry = viewModel::loadActiveRoute,
                    redColor = primaryColor
                )
            }

            routeState is RouteUiState.Error -> {
                ErrorRouteState(
                    modifier = Modifier.padding(padding),
                    message = routeState.message,
                    onRetry = viewModel::resetRouteState,
                    redColor = primaryColor
                )
            }

            else -> {
                EmptyRouteState(
                    modifier = Modifier.padding(padding),
                    redColor = primaryColor,
                    onScan = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                    onPickImage = { galleryLauncher.launch("image/*") }
                )
            }
        }
    }
}

@Composable
private fun UnifiedRouteDraftContent(
    modifier: Modifier,
    routeDraft: List<Client>,
    redColor: Color,
    onClientClick: (String) -> Unit,
    onMoveUp: (String) -> Unit,
    onMoveDown: (String) -> Unit,
    onMoveToFirst: (String) -> Unit,
    onMoveToLast: (String) -> Unit,
    onRemove: (String) -> Unit,
    onReset: () -> Unit
) {
    Column(modifier = modifier.fillMaxSize()) {
        val clientsWithCoordinates = routeDraft.filter(::hasValidCoordinates)
        if (clientsWithCoordinates.isNotEmpty()) {
            val firstClient = clientsWithCoordinates.first()
            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(
                    LatLng(firstClient.latitud, firstClient.longitud),
                    12f
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState
                ) {
                    clientsWithCoordinates.forEach { client ->
                        Marker(
                            state = rememberMarkerState(position = LatLng(client.latitud, client.longitud)),
                            title = client.cliente,
                            snippet = "Cod: ${client.codigoCliente}"
                        )
                    }
                }
            }
        }

        Text(
            text = stringResource(R.string.label_route_stops, routeDraft.size),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontWeight = FontWeight.Bold,
            color = Color.DarkGray
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(routeDraft, key = { _, client -> client.id }) { index, client ->
                var showMenu by remember { mutableStateOf(false) }

                ClientItem(
                    client = client,
                    onClick = { onClientClick(client.id) },
                    actionContent = {
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Opciones",
                                    tint = Color.Gray
                                )
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Subir") },
                                    leadingIcon = {
                                        Icon(Icons.Default.ArrowUpward, contentDescription = null)
                                    },
                                    onClick = {
                                        showMenu = false
                                        onMoveUp(client.id)
                                    },
                                    enabled = index > 0
                                )
                                DropdownMenuItem(
                                    text = { Text("Bajar") },
                                    leadingIcon = {
                                        Icon(Icons.Default.ArrowDownward, contentDescription = null)
                                    },
                                    onClick = {
                                        showMenu = false
                                        onMoveDown(client.id)
                                    },
                                    enabled = index < routeDraft.lastIndex
                                )
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = { Text("Mover al inicio") },
                                    leadingIcon = {
                                        Icon(Icons.Default.ArrowUpward, contentDescription = null)
                                    },
                                    onClick = {
                                        showMenu = false
                                        onMoveToFirst(client.id)
                                    },
                                    enabled = index > 0
                                )
                                DropdownMenuItem(
                                    text = { Text("Mover al final") },
                                    leadingIcon = {
                                        Icon(Icons.Default.ArrowDownward, contentDescription = null)
                                    },
                                    onClick = {
                                        showMenu = false
                                        onMoveToLast(client.id)
                                    },
                                    enabled = index < routeDraft.lastIndex
                                )
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = { Text("Eliminar", color = redColor) },
                                    leadingIcon = {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = redColor)
                                    },
                                    onClick = {
                                        showMenu = false
                                        onRemove(client.id)
                                    }
                                )
                            }
                        }
                    }
                )
            }

            item {
                TextButton(
                    onClick = onReset,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.text_reset_scan), color = Color.Gray)
                }
            }
        }
    }
}

@Composable
private fun LoadingRouteState(modifier: Modifier, redColor: Color) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(color = redColor)
        Spacer(modifier = Modifier.height(16.dp))
        Text(stringResource(R.string.text_processing_route), fontWeight = FontWeight.Bold)
        Text(stringResource(R.string.text_processing_desc), color = Color.Gray, fontSize = 12.sp)
    }
}

@Composable
private fun EmptyRouteState(
    modifier: Modifier,
    redColor: Color,
    onScan: () -> Unit,
    onPickImage: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Map,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = Color.LightGray
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            stringResource(R.string.text_no_route_yet),
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray
        )
        Text(
            stringResource(R.string.text_no_route_desc),
            fontSize = 14.sp,
            color = Color.LightGray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onScan,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            colors = ButtonDefaults.buttonColors(containerColor = redColor)
        ) {
            Icon(Icons.Default.CameraAlt, contentDescription = null)
            Spacer(modifier = Modifier.size(8.dp))
            Text(stringResource(R.string.btn_scan_sheet))
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = onPickImage,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            border = BorderStroke(1.dp, redColor),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = redColor)
        ) {
            Icon(Icons.Default.Image, contentDescription = null)
            Spacer(modifier = Modifier.size(8.dp))
            Text(stringResource(R.string.btn_import_gallery))
        }
    }
}

@Composable
private fun ErrorRouteState(
    modifier: Modifier,
    message: String,
    onRetry: () -> Unit,
    redColor: Color
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.msg_route_error_title), fontWeight = FontWeight.Bold, color = redColor)
        Text(message, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Text(stringResource(R.string.btn_accept))
        }
    }
}

@Composable
private fun ActiveRouteContent(
    modifier: Modifier,
    stops: List<RouteStop>,
    selectedTruck: Int,
    redColor: Color,
    onClientClick: (String) -> Unit,
    stopToClient: (RouteStop) -> Client,
    onOpenMap: (List<Client>) -> Unit,
    onMarkVisited: (RouteStop) -> Unit
) {
    var showNoCoordinatesInfo by remember { mutableStateOf(false) }
    val clients = stops.map(stopToClient)
    val clientsWithCoordinates = clients.filter(::hasValidCoordinates)
    val visitedCount = stops.count { it.isVisited }
    
    val sortedStops = stops.sortedBy { it.order }
    val pendingStops = sortedStops.filter { !it.isVisited }
    val visitedStops = sortedStops.filter { it.isVisited }

    Column(modifier = modifier.fillMaxSize()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE9F7EE))
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.title_active_route),
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B5E20),
                            fontSize = 16.sp
                        )
                        Text(
                            text = stringResource(R.string.label_visited_summary, visitedCount, stops.size),
                            color = Color.DarkGray,
                            fontSize = 13.sp
                        )
                    }
                    OutlinedButton(
                        onClick = {
                            if (clientsWithCoordinates.isNotEmpty()) onOpenMap(clientsWithCoordinates)
                            else showNoCoordinatesInfo = true
                        }
                    ) {
                        Icon(Icons.Default.Map, contentDescription = null)
                        Spacer(modifier = Modifier.size(6.dp))
                        Text(stringResource(R.string.btn_view_map))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(pendingStops, key = { _, stop -> stop.clientId }) { index, stop ->
                val client = stopToClient(stop)
                
                val bgColor = if (index == 0) Color(0xFFE8F0FE) else Color.White
                
                ClientItem(
                    client = client,
                    onClick = { onClientClick(client.id) },
                    containerColor = bgColor,
                    contentColor = Color.Black,
                    actionContent = {
                        Button(
                            onClick = { onMarkVisited(stop) },
                            colors = ButtonDefaults.buttonColors(containerColor = redColor)
                        ) {
                            Text(stringResource(R.string.btn_mark_visited))
                        }
                    }
                )
            }

            if (visitedStops.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.label_visited_header),
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(visitedStops, key = { it.clientId }) { stop ->
                    val client = stopToClient(stop)
                    ClientItem(
                        client = client,
                        onClick = { onClientClick(client.id) },
                        containerColor = Color(0xFFF5F5F5),
                        contentColor = Color.Black,
                        actionContent = {
                            Button(
                                onClick = {},
                                enabled = false,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF4CAF50),
                                    disabledContainerColor = Color(0xFF4CAF50),
                                    disabledContentColor = Color.White
                                )
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null)
                                Spacer(modifier = Modifier.size(6.dp))
                                Text(stringResource(R.string.btn_visited))
                            }
                        }
                    )
                }
            }
        }
    }

    if (showNoCoordinatesInfo) {
        AlertDialog(
            onDismissRequest = { showNoCoordinatesInfo = false },
            title = { Text(stringResource(R.string.dialog_no_coords_title)) },
            text = { Text(stringResource(R.string.dialog_no_coords_text)) },
            confirmButton = {
                TextButton(onClick = { showNoCoordinatesInfo = false }) {
                    Text(stringResource(R.string.btn_accept))
                }
            }
        )
    }
}

@Composable
private fun RouteBottomBar(
    label: String,
    containerColor: Color,
    contentColor: Color = Color.White,
    textSize: androidx.compose.ui.unit.TextUnit = 16.sp,
    trailingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onTrailingClick: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onClick,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = containerColor,
                    contentColor = contentColor
                )
            ) {
                Text(label, fontSize = textSize, fontWeight = FontWeight.SemiBold)
            }

            if (trailingIcon != null && onTrailingClick != null) {
                OutlinedButton(
                    onClick = onTrailingClick,
                    modifier = Modifier.size(52.dp),
                    contentPadding = PaddingValues(0.dp),
                    border = BorderStroke(1.dp, Color(0xFF7A7A7A))
                ) {
                    Icon(
                        imageVector = trailingIcon,
                        contentDescription = null,
                        tint = Color(0xFF4A4A4A)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RouteMapScreen(
    title: String,
    subtitle: String,
    clients: List<Client>,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val mapPrimaryColor = MaterialTheme.colorScheme.primary
    var hasLocationPermission by remember { mutableStateOf(hasLocationPermission(context)) }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasLocationPermission = granted
    }

    val firstClient = clients.first()
    val initialPosition = LatLng(firstClient.latitud, firstClient.longitud)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialPosition, 11f)
    }
    val bounds = remember(clients) {
        LatLngBounds.builder().apply {
            clients.forEach { client ->
                include(LatLng(client.latitud, client.longitud))
            }
        }.build()
    }

    LaunchedEffect(clients) {
        cameraPositionState.move(CameraUpdateFactory.newLatLngBounds(bounds, 120))
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    val mapUiSettings = remember(hasLocationPermission) {
        MapUiSettings(
            zoomControlsEnabled = true,
            myLocationButtonEnabled = hasLocationPermission
        )
    }
    val mapProperties = remember(hasLocationPermission) {
        MapProperties(isMyLocationEnabled = hasLocationPermission)
    }
    val hasMapsKey = BuildConfig.MAPS_API_KEY.isNotBlank()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column {
                        Text(title, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(
                            text = subtitle,
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 12.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Clear, contentDescription = null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = mapPrimaryColor)
            )
        }
    ) { padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                if (!hasMapsKey) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD))
                    ) {
                        Text(
                            text = "Falta configurar maps.apiKey en local.properties. Sin eso el mapa puede verse vacio o sin calles.",
                            modifier = Modifier.padding(12.dp),
                            color = Color(0xFF6B4F00)
                        )
                    }
                }

                if (!hasLocationPermission) {
                    OutlinedButton(
                        onClick = { locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) },
                        modifier = Modifier.padding(bottom = 10.dp)
                    ) {
                        Text("Permitir mi ubicacion")
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        properties = mapProperties,
                        uiSettings = mapUiSettings
                    ) {
                        clients.forEachIndexed { index, client ->
                            Marker(
                                state = rememberMarkerState(position = LatLng(client.latitud, client.longitud)),
                                title = "${index + 1}. ${client.cliente}",
                                snippet = client.direccion
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun hasValidCoordinates(client: Client): Boolean {
    return client.latitud != 0.0 && client.longitud != 0.0
}

private fun hasLocationPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}
